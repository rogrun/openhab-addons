/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.aquatemp.internal.handler;

import static org.openhab.binding.aquatemp.internal.AquatempBindingConstants.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.aquatemp.internal.AquatempDiscoveryService;
import org.openhab.binding.aquatemp.internal.api.AquatempApi;
import org.openhab.binding.aquatemp.internal.api.AquatempApiType;
import org.openhab.binding.aquatemp.internal.api.AquatempCommunicationException;
import org.openhab.binding.aquatemp.internal.config.BridgeConfiguration;
import org.openhab.binding.aquatemp.internal.dto.device.DataDTO;
import org.openhab.binding.aquatemp.internal.dto.device.DataObjectResult;
import org.openhab.binding.aquatemp.internal.dto.device.DeviceDTO;
import org.openhab.binding.aquatemp.internal.dto.device.DeviceStatusDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ErrorDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ObjectResult;
import org.openhab.binding.aquatemp.internal.util.AquatempUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link AquatempBridgeHandler} is responsible for handling the api connection.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class AquatempBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Storage<String> stateStorage;

    private static final String STORED_API_CALLS = "apiCalls";

    private final HttpClient httpClient;

    private @NonNullByDefault({}) AquatempApi api;

    protected @Nullable AquatempDiscoveryService discoveryService;

    private int apiCalls;
    private int pollingInterval = 90;
    private boolean countReset = true;

    private @Nullable ScheduledFuture<?> aquatempBridgePollingJob;

    private @Nullable ScheduledFuture<?> initJob;
    private volatile boolean disposed = false;

    public @Nullable List<ObjectResult> devicesData;

    private DeviceDTO allDevices = new DeviceDTO();
    protected final List<String> devicesList = new ArrayList<>();
    protected final Map<String, String> deviceNameMap = new HashMap<>();

    private BridgeConfiguration config = new BridgeConfiguration();

    private final ItemChannelLinkRegistry linkRegistry;

    public AquatempBridgeHandler(Bridge bridge, Storage<String> stateStorage, HttpClient httpClient,
            ItemChannelLinkRegistry linkRegistry) {
        super(bridge);
        this.stateStorage = stateStorage;
        this.httpClient = httpClient;
        this.linkRegistry = linkRegistry;
    }

    /**
     * get the devices list (needed for discovery)
     *
     * @return a list of the all devices
     */
    public List<String> getDevicesList() {
        // return a copy of the list, so we don't run into concurrency problems
        return new ArrayList<>(devicesList);
    }

    /**
     * get device name map (needed for discovery)
     *
     * @return a map of the all devices names
     */
    public Map<String, String> getDeviceNameMap() {
        return new HashMap<>(deviceNameMap);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_RUN_QUERY_ONCE) && OnOffType.ON.equals(command)) {
            logger.debug("Received command: CHANNEL_RUN_QUERY_ONCE");
            pollingDevices();
            updateState(CHANNEL_RUN_QUERY_ONCE, OnOffType.OFF);
        }
    }

    @Override
    public void dispose() {
        disposed = true;

        ScheduledFuture<?> currentInitJob = initJob;
        if (currentInitJob != null) {
            currentInitJob.cancel(true);
            initJob = null;
        }

        stopAquatempBridgePolling();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(AquatempDiscoveryService.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Aquatemp Accountservice");

        this.config = getConfigAs(BridgeConfiguration.class);
        String storedApiCalls = this.stateStorage.get(STORED_API_CALLS);
        if (storedApiCalls != null) {
            apiCalls = Integer.parseInt(storedApiCalls);
        } else {
            apiCalls = 0;
        }
        disposed = false;

        int thingTypeVersion = AquatempUtil.getThingTypeVersion(thing);
        if (thingTypeVersion < BRIDGE_THING_TYPE_VERSION) {
            migrateChannelIds();
        }

        AquatempApiType apiType;
        try {
            apiType = AquatempApiType.fromConfig(this.config.cloudApi);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }
        api = new AquatempApi(this, httpClient, this.config.user, this.config.password, apiType,
                this.config.productIds);
        initJob = scheduler.schedule(() -> {
            if (disposed) {
                return;
            }
            try {
                getAllDevices();
                if (!devicesList.isEmpty()) {
                    updateBridgeStatus(ThingStatus.ONLINE);
                    startAquatempBridgePolling(getPollingInterval(), 1);
                }
            } catch (Exception e) {
                if (!disposed) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }
        }, 0, TimeUnit.SECONDS);
    }

    private void migrateChannelIds() {
        List<Channel> oldChannels = thing.getChannels();
        List<Channel> newChannels = new ArrayList<>(oldChannels.size());

        Map<ChannelUID, ChannelUID> renameMap = new LinkedHashMap<>();

        for (Channel channel : oldChannels) {
            String oldId = channel.getUID().getId();
            String newId = AquatempUtil.camelToHyphen(oldId);
            boolean updateChannelType = false;
            String channelLabel = channel.getLabel();
            String channelType = AquatempUtil
                    .camelToHyphen(Objects.requireNonNull(channel.getChannelTypeUID()).toString());
            channelType = channelType.replace(BINDING_ID + ":", "");

            if (channelType.contains("type-")) {
                channelType = channelType.replace("type-", "");
                updateChannelType = true;
            }

            if (!newId.equals(oldId) || updateChannelType) {
                logger.info("Migrating channel '{}' -> '{}'", oldId, newId);

                ChannelUID oldUid = channel.getUID();
                ChannelUID newUid = new ChannelUID(thing.getUID(), newId);

                ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelType);
                if (channelLabel != null) {
                    Channel newChannel = ChannelBuilder.create(newUid, channel.getAcceptedItemType())
                            .withLabel(channelLabel).withType(channelTypeUID).withProperties(channel.getProperties())
                            .build();

                    newChannels.add(newChannel);
                    renameMap.put(oldUid, newUid);
                }
            }
        }

        if (renameMap.isEmpty()) {
            return;
        }

        updateThing(editThing().withChannels(newChannels).build());

        for (Map.Entry<ChannelUID, ChannelUID> e : renameMap.entrySet()) {
            ChannelUID oldUid = e.getKey();
            ChannelUID newUid = e.getValue();

            Collection<ItemChannelLink> links = new ArrayList<>(linkRegistry.getLinks(oldUid));

            for (ItemChannelLink link : links) {
                String item = link.getItemName();
                try {
                    linkRegistry.remove(link.getUID());
                } catch (Exception ex) {
                    logger.warn("Could not remove old link {} -> {}: {}", item, oldUid, ex.getMessage());
                }

                linkRegistry.add(new ItemChannelLink(item, newUid));
                logger.info("Re-linked item '{}' from '{}' to '{}'", item, oldUid.getId(), newUid.getId());
            }
        }
        updateProperty("thingTypeVersion", Integer.toString(BRIDGE_THING_TYPE_VERSION));
    }

    public void getAllDevices() {
        logger.trace("Loading Device List from Aquatemp Bridge");
        try {
            DeviceDTO devices = api.getAllDevices();
            countApiCalls();
            if (devices != null) {
                allDevices = devices;
                @Nullable
                List<ObjectResult> currentDevices = allDevices.objectResult;
                devicesData = currentDevices;
                if (currentDevices == null) {
                    logger.warn("Device list is empty.");
                } else {
                    for (ObjectResult deviceData : currentDevices) {
                        String deviceId = deviceData.getIdentifier();
                        String deviceNickName = deviceData.getDisplayName();
                        if (!devicesList.contains(deviceId)) {
                            devicesList.add(deviceId);
                            deviceNameMap.put(deviceId, deviceNickName);
                        }
                        logger.trace("Device ID: {} Nickname: {}", deviceId, deviceNickName);
                    }
                }
            }
        } catch (AquatempCommunicationException e) {
            updateBridgeStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Installation not reachable");
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn("Parsing Aquatemp response fails: {}", e.getMessage());
        }
    }

    public void getDeviceError(DeviceHandler handler) {
        if (!api.supportsFaultData()) {
            return;
        }
        String deviceId = handler.getDeviceId();
        logger.trace("Loading error-list from Aquatemp DeviceID: {}", deviceId);
        try {
            if (api.supportsDeviceFaultStatus()) {
                DeviceStatusDTO deviceStatus = api.getDeviceStatus(deviceId);
                countApiCalls();
                Boolean isFault = deviceStatus == null || deviceStatus.objectResult == null ? null
                        : deviceStatus.objectResult.isFault;
                if (isFault == null) {
                    logger.warn("Device fault status is missing for DeviceID: {}", deviceId);
                    return;
                }
                handler.handleDeviceFaultStatus(isFault);
                if (!isFault) {
                    return;
                }
            }
            ErrorDTO errors = api.getErrorsByDevice(deviceId);
            countApiCalls();
            logger.trace("Errors:{}", errors);
            if (errors != null) {
                if (!api.supportsDeviceFaultStatus()
                        || (errors.objectResult != null && !errors.objectResult.isEmpty())) {
                    handler.handleUpdateError(errors);
                }
            }
        } catch (AquatempCommunicationException e) {
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Device not reachable | {}");
            logger.trace("Device not reachable | {}", e.getMessage());
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn("Parsing Aquatemp response fails: {}", e.getMessage());
        }
    }

    public void controlDevice(String deviceCode, Map<String, String> parameters) throws AquatempCommunicationException {
        api.controlDevice(deviceCode, parameters);
    }

    public void controlTargetTemperature(String deviceCode, String value, String mode)
            throws AquatempCommunicationException {
        api.controlTargetTemperature(deviceCode, value, mode);
    }

    public void controlSilentMode(String deviceCode, String value) throws AquatempCommunicationException {
        api.controlSilentMode(deviceCode, value);
    }

    public void controlMode(String deviceCode, String mode, String currentTargetTemperature)
            throws AquatempCommunicationException {
        api.controlMode(deviceCode, mode, currentTargetTemperature);
    }

    private Integer getPollingInterval() {
        if (this.config.pollingInterval > 0) {
            return this.config.pollingInterval;
        } else {

            int calculatedInterval = (86400 / (this.config.apiCallLimit - this.config.bufferApiCommands)
                    * getAllActiveDevices()) + 1;

            if (calculatedInterval < 60) {
                calculatedInterval = 60;
            }
            return calculatedInterval;
        }
    }

    private void countApiCalls() {
        apiCalls++;
        String apiCallsAsString = String.valueOf(apiCalls);
        stateStorage.put(STORED_API_CALLS, apiCallsAsString);
        updateState(COUNT_API_CALLS, DecimalType.valueOf(apiCallsAsString));
    }

    private void checkResetApiCalls() {
        LocalTime time = LocalTime.now();
        if (time.isAfter(LocalTime.of(0, 0, 1)) && (time.isBefore(LocalTime.of(1, 0, 0)))) {
            if (countReset) {
                logger.debug("Resetting API call counts");
                apiCalls = 0;
                countReset = false;
            }
        } else {
            countReset = true;
        }
    }

    private void pollingDevices() {
        getAllDevices();
        List<Thing> children = getThing().getThings().stream().filter(Thing::isEnabled).toList();
        for (Thing child : children) {
            ThingHandler childHandler = child.getHandler();
            if (childHandler instanceof DeviceHandler && ThingHandlerHelper.isHandlerInitialized(childHandler)) {
                // checkDeviceStatus((DeviceHandler) childHandler);
                updateAllDataOfDevice((DeviceHandler) childHandler);
                getDeviceError((DeviceHandler) childHandler);
                checkDeviceStatus((DeviceHandler) childHandler);
            }
        }
    }

    private Integer getAllActiveDevices() {
        Integer activeDevices = 0;
        List<Thing> bridgeChildren = getThing().getThings().stream().filter(Thing::isEnabled).toList();
        for (Thing bridgeChild : bridgeChildren) {
            ThingHandler childHandler = bridgeChild.getHandler();
            if (childHandler instanceof DeviceHandler) {
                activeDevices++;
            }
        }
        return activeDevices;
    }

    public void updateAllDataOfDevice(DeviceHandler handler) {
        String deviceId = handler.getDeviceId();
        logger.debug("Loading data from Device ID: {}", deviceId);
        try {
            DataDTO allData = api.getDataByCode(deviceId);
            countApiCalls();
            if (allData != null && allData.objectResult != null) {
                for (DataObjectResult dataObjectResult : allData.objectResult) {
                    String value = dataObjectResult.value;
                    String code = dataObjectResult.code;
                    logger.trace("DataObject: Code: {} | Value: {} ", code, value);
                }
                handler.handleUpdate(allData);
            } else {
                handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Device data is unavailable");
            }
        } catch (AquatempCommunicationException e) {
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Device not reachable");
            logger.trace("Device not reachable | {}", e.getMessage());
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn("Parsing Aquatemp response fails: {}", e.getMessage());
        }
    }

    private void checkDeviceStatus(DeviceHandler handler) {
        logger.debug("Checking device status");
        String deviceId = handler.getDeviceId();
        @Nullable
        List<ObjectResult> currentDevices = allDevices.objectResult;
        devicesData = currentDevices;
        if (currentDevices == null) {
            logger.warn("Device list is empty.");
        } else {
            for (ObjectResult deviceData : currentDevices) {
                if (deviceId.equals(deviceData.getIdentifier())) {
                    if ("ONLINE".equals(deviceData.deviceStatus)) {
                        handler.updateThingStatus(ThingStatus.ONLINE);
                    } else {
                        handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Device not reachable");
                        logger.trace("Device not reachable | OFFLINE");
                    }
                }
            }
        }
    }

    private void startAquatempBridgePolling(Integer pollingIntervalS, Integer initialDelay) {
        ScheduledFuture<?> currentPollingJob = aquatempBridgePollingJob;
        if (currentPollingJob == null) {
            aquatempBridgePollingJob = scheduler.scheduleWithFixedDelay(() -> {
                api.checkExpiringToken();
                checkResetApiCalls();
                if (!config.disablePolling) {
                    logger.debug("Refresh job scheduled to run every {} seconds for '{}'", pollingIntervalS,
                            getThing().getUID());
                    pollingDevices();
                    int newPollingInterval = getPollingInterval();
                    if (newPollingInterval != pollingInterval) {
                        pollingInterval = newPollingInterval;
                        updateBridgePollingInterval();
                    }
                }
            }, initialDelay, pollingIntervalS, TimeUnit.SECONDS);
        }
    }

    public void stopAquatempBridgePolling() {
        ScheduledFuture<?> currentPollingJob = aquatempBridgePollingJob;
        if (currentPollingJob != null) {
            currentPollingJob.cancel(true);
            aquatempBridgePollingJob = null;
        }
    }

    private void updateBridgePollingInterval() {
        updateProperty("pollingInterval [s]", String.valueOf(pollingInterval));
        stopAquatempBridgePolling();
        startAquatempBridgePolling(pollingInterval, pollingInterval);
    }

    public void updateBridgeStatus(ThingStatus status) {
        updateStatus(status);
    }

    public void updateBridgeStatus(ThingStatus status, ThingStatusDetail statusDetail, String statusMessage) {
        updateStatus(status, statusDetail, statusMessage);
    }
}
