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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.aquatemp.internal.AquatempStateDescriptionProvider;
import org.openhab.binding.aquatemp.internal.api.AquatempCommunicationException;
import org.openhab.binding.aquatemp.internal.config.ThingsConfig;
import org.openhab.binding.aquatemp.internal.dto.AquatempMessage;
import org.openhab.binding.aquatemp.internal.dto.device.DataDTO;
import org.openhab.binding.aquatemp.internal.dto.device.DataObjectResult;
import org.openhab.binding.aquatemp.internal.dto.device.ErrorDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ErrorObjectResult;
import org.openhab.binding.aquatemp.internal.util.AquatempUtil;
import org.openhab.core.items.ManagedItemProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceHandler} is responsible for handling DeviceHandler
 *
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DeviceHandler extends AquatempThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    private ThingsConfig config = new ThingsConfig();
    private String currentMode = "";
    private String currentTargetTemperature = "";

    private final ItemChannelLinkRegistry linkRegistry;
    private final ManagedItemProvider managedItemProvider;
    private final AquatempStateDescriptionProvider stateDescriptionProvider;

    public DeviceHandler(Thing thing, ItemChannelLinkRegistry linkRegistry, ManagedItemProvider managedItemProvider,
            AquatempStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.linkRegistry = linkRegistry;
        this.managedItemProvider = managedItemProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void initialize() {
        ThingsConfig config = getConfigAs(ThingsConfig.class);
        this.config = config;
        if (config.deviceId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid device id setting");
            return;
        }
        updateProperty(PROPERTY_ID, config.deviceId); // set representation property used by discovery

        int thingTypeVersion = AquatempUtil.getThingTypeVersion(thing);
        if (thingTypeVersion < DEVICE_THING_TYPE_VERSION) {
            migrateChannelIds();
        }

        initDeviceState();
        logger.trace("Device handler finished initializing");
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
            String oldChannelType = AquatempUtil
                    .camelToHyphen(Objects.requireNonNull(channel.getChannelTypeUID()).toString());
            oldChannelType = oldChannelType.replace(BINDING_ID + ":", "");
            String newChannelType = oldChannelType;

            if ("type-settemperature".equals(oldChannelType) || "settemperature".equals(oldChannelType)) {
                newChannelType = "set-temperature";
                updateChannelType = true;
            }

            if (oldChannelType.contains("type-")) {
                newChannelType = newChannelType.replace("type-", "");
                updateChannelType = true;
            }

            if (oldChannelType.contains("type-string-readOnly")) {
                newChannelType = "string-read-only";
                updateChannelType = true;
            }

            if (oldChannelType.contains("type-boolean-readOnly")) {
                newChannelType = "boolean-read-only";
                updateChannelType = true;
            }

            if (!newId.equals(oldId) || updateChannelType) {
                logger.info("Migrating channel '{}' -> '{}' | channel-type  '{}' -> '{}'", oldId, newId, oldChannelType,
                        newChannelType);

                ChannelUID oldUid = channel.getUID();
                ChannelUID newUid = new ChannelUID(thing.getUID(), newId);
                String channelDescription = channel.getDescription();
                if (channelDescription == null) {
                    channelDescription = "";
                }

                ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, newChannelType);
                if (channelLabel != null) {
                    Channel newChannel = ChannelBuilder.create(newUid, channel.getAcceptedItemType())
                            .withLabel(channelLabel).withDescription(channelDescription).withType(channelTypeUID)
                            .withProperties(channel.getProperties()).build();

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
                if (isManagedItem(item)) {
                    try {
                        linkRegistry.remove(link.getUID());
                    } catch (Exception ex) {
                        logger.warn("Could not remove old link {} -> {}: {}", item, oldUid, ex.getMessage());
                    }

                    linkRegistry.add(new ItemChannelLink(item, newUid));
                    logger.info("Re-linked item '{}' from '{}' to '{}'", item, oldUid.getId(), newUid.getId());
                }
            }
        }
        updateProperty("thingTypeVersion", Integer.toString(DEVICE_THING_TYPE_VERSION));
    }

    @Override
    public void initChannelState() {
        Bridge bridge = getBridge();
        AquatempBridgeHandler bridgeHandler = bridge == null ? null : (AquatempBridgeHandler) bridge.getHandler();
        if (bridgeHandler != null) {
            bridgeHandler.updateAllDataOfDevice(this);
            bridgeHandler.getDeviceError(this);
        }
    }

    public String getDeviceId() {
        return config.deviceId;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            initChannelState();
            return;
        }

        try {
            Channel ch = thing.getChannel(channelUID.getId());
            if (ch != null) {
                logger.trace("ChannelUID: {} | Properties: {}", ch.getUID().getId(), ch.getProperties());
                Map<String, String> parameters = new LinkedHashMap<>();
                if ("power-state".equals(ch.getUID().getId())) {
                    String state = (OnOffType.ON.equals(command) ? "1" : "0");
                    parameters.put("Power", state);
                }

                if ("silent-mode".equals(ch.getUID().getId())) {
                    String state = (OnOffType.ON.equals(command) ? "1" : "0");
                    Bridge bridge = getBridge();
                    AquatempBridgeHandler bridgeHandler = bridge == null ? null
                            : (AquatempBridgeHandler) bridge.getHandler();
                    if (bridgeHandler != null) {
                        try {
                            bridgeHandler.controlSilentMode(getDeviceId(), state);
                        } catch (AquatempCommunicationException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    }
                    return;
                }

                if ("mode".equals(ch.getUID().getId())) {
                    String state = command.toString();
                    Bridge bridge = getBridge();
                    AquatempBridgeHandler bridgeHandler = bridge == null ? null
                            : (AquatempBridgeHandler) bridge.getHandler();
                    if (bridgeHandler != null) {
                        try {
                            bridgeHandler.controlMode(getDeviceId(), state, currentTargetTemperature);
                        } catch (AquatempCommunicationException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    }
                    return;
                }

                if ("target-temperature".equals(ch.getUID().getId())) {
                    String value = getTemperatureValue(command);
                    Bridge bridge = getBridge();
                    AquatempBridgeHandler bridgeHandler = bridge == null ? null
                            : (AquatempBridgeHandler) bridge.getHandler();
                    if (bridgeHandler != null) {
                        try {
                            bridgeHandler.controlTargetTemperature(getDeviceId(), value, currentMode);
                        } catch (AquatempCommunicationException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    }
                    return;
                }

                if (!parameters.isEmpty()) {
                    Bridge bridge = getBridge();
                    AquatempBridgeHandler bridgeHandler = bridge == null ? null
                            : (AquatempBridgeHandler) bridge.getHandler();
                    if (bridgeHandler != null) {
                        try {
                            bridgeHandler.controlDevice(getDeviceId(), parameters);
                        } catch (AquatempCommunicationException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            logger.warn("handleCommand fails", e);
        }
    }

    @Override
    public void handleUpdateChannel(AquatempMessage msg) {
        logger.trace("handleUpdateChannel: {}", msg);
    }

    @Override
    public void handleUpdate(DataDTO data) {
        updateStatus(ThingStatus.ONLINE);

        float consumption = 0;
        boolean silentMode = false;
        boolean powerState = false;
        String mode = findValueByCode(data, "Mode");

        // Consumption
        String current = findValueByCode(data, "T07", "CompressorCurrentDetect");
        String voltage = findValueByCode(data, "T14", "InverterBoardACInputVoltage");
        if ("1".equals(findValueByCode(data, "Power")) && !current.isBlank() && !voltage.isBlank()) {
            consumption = Float.parseFloat(current) * Float.parseFloat(voltage);
        }

        // Inlet-Temperature
        float inletTemperature = Float.parseFloat(findValueByCode(data, "T02", "InletWaterTemp"));

        // Outlet-Temperature
        float outletTemperature = Float.parseFloat(findValueByCode(data, "T03", "OutletWaterTemp"));

        // Target-Temperature
        String targetTemperatureCode = getTargetTemperatureCode(mode);
        float targetTemperature = Float.parseFloat(findValueByCode(data, "Set_Temp", targetTemperatureCode));

        // Ambient temperature
        float ambientTemperature = Float.parseFloat(findValueByCode(data, "T05", "AT"));

        // Silent mode
        if ("1".equals(findValueByCode(data, "Manual-mute", "EnSlient"))) {
            silentMode = true;
        }

        // state power "Power"
        if ("1".equals(findValueByCode(data, "Power"))) {
            powerState = true;
        }

        updateState("power-state", powerState ? OnOffType.ON : OnOffType.OFF);
        updateState("silent-mode", silentMode ? OnOffType.ON : OnOffType.OFF);

        updateChannelState("inlet-temperature", String.valueOf(inletTemperature), SIUnits.CELSIUS.getSymbol());
        updateChannelState("outlet-temperature", String.valueOf(outletTemperature), SIUnits.CELSIUS.getSymbol());
        updateChannelState("ambient-temperature", String.valueOf(ambientTemperature), SIUnits.CELSIUS.getSymbol());

        updateChannelState("target-temperature", String.valueOf(targetTemperature), SIUnits.CELSIUS.getSymbol());
        updateTargetTemperatureRange(data, mode);

        updateChannelState("consumption", String.valueOf(consumption), Units.WATT.toString());

        updateOptionalQuantityChannel(data, "actual-power", Units.WATT.toString(), "Z04");
        updateOptionalQuantityChannel(data, "electricity-consumption-per-hour", Units.KILOWATT_HOUR.toString(), "Z07");
        updateOptionalQuantityChannel(data, "compressor-output-frequency", Units.HERTZ.toString(), "O07", "o07",
                "Sys1CompWorkingFreq");
        updateOptionalQuantityChannel(data, "compressor-current", Units.AMPERE.toString(), "O08", "o08",
                "CompressorCurrentDetect");

        String operatingState = formatOperatingState(findValueByCode(data, "state_mode"));
        if (operatingState.isBlank()) {
            operatingState = formatOperatingMode(findValueByCode(data, "ModeType"));
        }
        if (operatingState.isBlank()) {
            operatingState = formatCompressorState(findValueByCode(data, "Sys1CompState"));
        }
        if (!operatingState.isBlank()) {
            updateState("operating-state", StringType.valueOf(operatingState));
        }

        String flowSwitch = findValueByCode(data, "S03", "WaterFlowSwitch");
        if (!flowSwitch.isBlank()) {
            updateState("flow-switch", toFlowSwitchState(flowSwitch));
        }

        updateState("mode", StringType.valueOf(mode));
        currentMode = mode;
        currentTargetTemperature = Float.toString(targetTemperature);
    }

    private void updateTargetTemperatureRange(DataDTO data, String mode) {
        boolean heating = "1".equals(mode);
        String minimum = findValueByCode(data, heating ? "SetHeatMinTargetTemp" : "SetCoolMinTargetTemp");
        String maximum = findValueByCode(data, heating ? "SetHeatMaxTargetTemp" : "SetCoolMaxTargetTemp");
        BigDecimal defaultMinimum = BigDecimal.valueOf(heating ? 6 : 8);
        BigDecimal defaultMaximum = BigDecimal.valueOf(35);
        stateDescriptionProvider.setTargetTemperatureRange(new ChannelUID(thing.getUID(), "target-temperature"),
                parseDecimal(minimum, defaultMinimum), parseDecimal(maximum, defaultMaximum));
    }

    private static BigDecimal parseDecimal(String value, BigDecimal defaultValue) {
        try {
            return value.isBlank() ? defaultValue : new BigDecimal(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    static OnOffType toFlowSwitchState(String value) {
        return "0".equals(value) ? OnOffType.ON : OnOffType.OFF;
    }

    static String formatOperatingState(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "heat", "heating" -> "Heating";
            case "cool", "cooling" -> "Cooling";
            case "idle", "standby" -> "Idle";
            default -> value.trim();
        };
    }

    static String formatOperatingMode(String value) {
        return switch (value.trim()) {
            case "0" -> "Cooling";
            case "1" -> "Heating";
            case "2" -> "Auto";
            default -> formatOperatingState(value);
        };
    }

    static String formatCompressorState(String value) {
        return switch (value.trim()) {
            case "0" -> "Idle";
            case "1" -> "Running";
            default -> value.trim();
        };
    }

    @Override
    public void handleUpdateError(ErrorDTO error) {
        if (error.objectResult != null && !error.objectResult.isEmpty()) {
            updateState("error-is-active", OnOffType.ON);
            String description = error.objectResult.stream().map(DeviceHandler::formatError).filter(s -> !s.isEmpty())
                    .collect(Collectors.joining("; "));
            updateState("error-message",
                    StringType.valueOf(description.isEmpty() ? "Device fault reported" : description));
        } else {
            handleDeviceFaultStatus(false);
        }
    }

    public void handleDeviceFaultStatus(boolean isFault) {
        updateState("error-is-active", isFault ? OnOffType.ON : OnOffType.OFF);
        updateState("error-message", StringType.valueOf(""));
    }

    static String formatError(ErrorObjectResult error) {
        String faultCode = Objects.requireNonNullElse(error.getFaultCode(), "").trim();
        String description = Objects.requireNonNullElse(error.getDescription(), "").trim();
        if (faultCode.isEmpty()) {
            return description;
        }
        if (description.isEmpty()) {
            return faultCode;
        }
        return faultCode + " - " + description;
    }

    private void updateChannelState(String channelId, String stateAsString, @Nullable String unit) {
        if (unit != null) {
            updateState(channelId, new QuantityType<>(stateAsString + " " + unit));
        } else {
            DecimalType s = DecimalType.valueOf(stateAsString);
            updateState(channelId, s);
        }
    }

    private void updateOptionalQuantityChannel(DataDTO data, String channelId, String unit, String... codes) {
        String value = findValueByCode(data, codes);
        if (!value.isBlank()) {
            try {
                updateChannelState(channelId, value, unit);
            } catch (IllegalArgumentException e) {
                logger.debug("Ignoring invalid value '{}' for channel '{}': {}", value, channelId, e.getMessage());
            }
        }
    }

    static String findValueByCode(DataDTO data, String... codes) {
        for (DataObjectResult dataObjectResult : data.objectResult) {
            for (String code : codes) {
                if (code.equals(dataObjectResult.code)) {
                    return dataObjectResult.value;
                }
            }
        }
        return "";
    }

    private String getTemperatureValue(Command command) {
        if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> celsius = quantity.toUnit(SIUnits.CELSIUS);
            if (celsius != null) {
                return celsius.toBigDecimal().stripTrailingZeros().toPlainString();
            }
        }
        if (command instanceof DecimalType decimal) {
            return decimal.toBigDecimal().stripTrailingZeros().toPlainString();
        }
        throw new IllegalArgumentException("Unsupported target-temperature command: " + command);
    }

    static String getTargetTemperatureCode(String mode) {
        return switch (mode) {
            case "0" -> "SetCoolTargetTemp";
            case "1" -> "SetHeatTargetTemp";
            case "2" -> "SetAutoTargetTemp";
            default -> "SetCurrentTemp";
        };
    }

    private boolean isManagedItem(String itemName) {
        boolean isManaged = managedItemProvider.getAll().stream().anyMatch(item -> item.getName().equals(itemName));

        if (isManaged) {
            return true; // item comes from UI / JSONDB
        }

        // fallback: treat as file-based
        logger.debug("Item '{}' not found in managed provider – treating as file-based.", itemName);
        return false;
    }
}
