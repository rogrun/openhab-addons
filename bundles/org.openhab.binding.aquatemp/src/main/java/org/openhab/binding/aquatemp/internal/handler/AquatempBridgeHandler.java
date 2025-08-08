/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.aquatemp.internal.AquatempDiscoveryService;
import org.openhab.binding.aquatemp.internal.api.AquatempApi;
import org.openhab.binding.aquatemp.internal.api.AquatempCommunicationException;
import org.openhab.binding.aquatemp.internal.config.BridgeConfiguration;
import org.openhab.binding.aquatemp.internal.dto.device.DataDTO;
import org.openhab.binding.aquatemp.internal.dto.device.DataObjectResult;
import org.openhab.binding.aquatemp.internal.dto.device.DeviceDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ErrorDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ObjectResult;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
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
    private boolean countReset = true;

    private @Nullable ScheduledFuture<?> aquatempBridgePollingJob;

    public @Nullable List<ObjectResult> devicesData;

    private DeviceDTO allDevices = new DeviceDTO();
    protected final List<String> devicesList = new ArrayList<>();
    protected final Map<String, String> deviceNameMap = new HashMap<>();

    private BridgeConfiguration config = new BridgeConfiguration();

    public AquatempBridgeHandler(Bridge bridge, Storage<String> stateStorage, HttpClient httpClient) {
        super(bridge);
        this.stateStorage = stateStorage;
        this.httpClient = httpClient;
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
        stopAquatempBridgePolling();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(AquatempDiscoveryService.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Aquatemp Accountservice");

        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);
        this.config = config;
        String storedApiCalls = this.stateStorage.get(STORED_API_CALLS);
        if (storedApiCalls != null) {
            apiCalls = Integer.parseInt(storedApiCalls);
        } else {
            apiCalls = 0;
        }

        api = new AquatempApi(this, httpClient, this.config.user, this.config.password);
        getAllDevices();
        if (!devicesList.isEmpty()) {
            updateBridgeStatus(ThingStatus.ONLINE);
            startAquatempBridgePolling(getPollingInterval(), 1);
        }
    }

    public void getAllDevices() {
        logger.trace("Loading Device List from Aquatemp Bridge");
        try {
            DeviceDTO devices = api.getAllDevices();
            countApiCalls();
            if (devices != null) {
                allDevices = devices;
                devicesData = allDevices.objectResult;
                if (devicesData == null) {
                    logger.warn("Device list is empty.");
                } else {
                    for (ObjectResult deviceData : allDevices.objectResult) {
                        String deviceId = deviceData.deviceCode;
                        String deviceNickName = deviceData.deviceNickName;
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
        String deviceId = handler.getDeviceId();
        logger.trace("Loading error-list from Aquatemp DeviceID: {}", deviceId);
        try {
            ErrorDTO errors = api.getErrorsByDevice(deviceId);
            countApiCalls();
            logger.trace("Errors:{}", errors);
            if (errors != null) {
                handler.handleUpdateError(errors);
            }
        } catch (AquatempCommunicationException e) {
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Device not reachable | {}");
            logger.trace("Device not reachable | {}", e.getMessage());
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warn("Parsing Aquatemp response fails: {}", e.getMessage());
        }
    }

    public void controlDevice(String json) throws AquatempCommunicationException {
        api.controlDevice(json);
    }

    private Integer getPollingInterval() {
        if (this.config.pollingInterval > 0) {
            return this.config.pollingInterval;
        } else {
            return (86400 / (this.config.apiCallLimit - this.config.bufferApiCommands) * devicesList.size()) + 1;
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
        List<Thing> children = getThing().getThings().stream().filter(Thing::isEnabled).collect(Collectors.toList());
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

    public void updateAllDataOfDevice(DeviceHandler handler) {
        String deviceId = handler.getDeviceId();
        logger.debug("Loading data from Device ID: {}", deviceId);
        try {
            DataDTO allData = api.getDataByCode(deviceId);
            countApiCalls();
            if (allData != null) {
                for (DataObjectResult dataObjectResult : allData.objectResult) {
                    String value = dataObjectResult.value;
                    String code = dataObjectResult.code;
                    logger.trace("DataObject: Code: {} | Value: {} ", code, value);
                }
                handler.handleUpdate(allData);
            } else {
                handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Device not reachable | Data is NULL");
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
        if (allDevices != null) {
            devicesData = allDevices.objectResult;
            if (devicesData == null) {
                logger.warn("Device list is empty.");
            } else {
                for (ObjectResult deviceData : allDevices.objectResult) {
                    if (deviceId.equals(deviceData.deviceCode)) {
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

    public void updateBridgeStatus(ThingStatus status) {
        updateStatus(status);
    }

    public void updateBridgeStatus(ThingStatus status, ThingStatusDetail statusDetail, String statusMessage) {
        updateStatus(status, statusDetail, statusMessage);
    }
}
