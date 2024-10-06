/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.aquatemp.internal.api.AquatempCommunicationException;
import org.openhab.binding.aquatemp.internal.config.ThingsConfig;
import org.openhab.binding.aquatemp.internal.dto.AquatempMessage;
import org.openhab.binding.aquatemp.internal.dto.device.DataDTO;
import org.openhab.binding.aquatemp.internal.dto.device.DataObjectResult;
import org.openhab.binding.aquatemp.internal.dto.device.ErrorDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ErrorObjectResult;
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
import org.openhab.core.types.Command;
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

    public DeviceHandler(Thing thing) {
        super(thing);
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

        initDeviceState();
        logger.trace("Device handler finished initializing");
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
        try {
            Channel ch = thing.getChannel(channelUID.getId());
            if (ch != null) {
                logger.trace("ChannelUID: {} | Properties: {}", ch.getUID().getId(), ch.getProperties());
                String json;
                Properties prop = new Properties();
                if ("powerState".equals(ch.getUID().getId())) {
                    String state = (OnOffType.ON.equals(command) ? "1" : "0");
                    prop.put("Power", state);
                }

                if ("silentMode".equals(ch.getUID().getId())) {
                    String state = (OnOffType.ON.equals(command) ? "1" : "0");
                    prop.put("Manual-mute", state);
                }

                if ("mode".equals(ch.getUID().getId())) {
                    String state = command.toString();
                    prop.put("mode", state);
                }

                if ("targetTemperature".equals(ch.getUID().getId())) {
                    String state = command.toString().substring(0, 2);
                    prop.put("R01", state);
                    prop.put("R02", state);
                    prop.put("R03", state);
                    prop.put("Set_Temp", state);
                }

                json = getJson(prop);
                if (json != null) {
                    Bridge bridge = getBridge();
                    AquatempBridgeHandler bridgeHandler = bridge == null ? null
                            : (AquatempBridgeHandler) bridge.getHandler();
                    if (bridgeHandler != null) {
                        try {
                            bridgeHandler.controlDevice(json);
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

    private @Nullable String getJson(@Nullable Properties prop) {
        if (prop != null) {
            logger.trace("Properties:{}", prop);
            StringBuilder sb = new StringBuilder();
            sb.append("{\"param\":[");
            prop.forEach((k, v) -> {
                logger.trace("protocolCode : {} | value: {}", k, v);
                sb.append("{ \"device_code\": \"").append(getDeviceId()).append("\", \"protocol_code\": ");
                sb.append("\"").append(k).append("\",\"value\": \"").append(v).append("\" }");
                sb.append(", ");
            });
            sb.append("]}");

            return sb.substring(0, sb.toString().length() - 4) + sb.substring(sb.toString().length() - 2);
        }
        return null;
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

        // Consumption
        if ("1".equals(findValueByCode(data, "Power"))) {
            consumption = Float.parseFloat(findValueByCode(data, "T07"))
                    * Float.parseFloat(findValueByCode(data, "T14"));
        }

        // Inlet-Temperature T02
        float inletTemperature = Float.parseFloat((findValueByCode(data, "T02")));

        // outlet-Temperature T03
        float outletTemperature = Float.parseFloat((findValueByCode(data, "T03")));

        // Target-Temperature Set_Temp
        float targetTemperature = Float.parseFloat((findValueByCode(data, "Set_Temp")));

        // ambient temperature T05
        float ambientTemperature = Float.parseFloat((findValueByCode(data, "T05")));

        // silent mode Manual-mute
        if ("1".equals(findValueByCode(data, "Manual-mute"))) {
            silentMode = true;
        }

        // state power "Power"
        if ("1".equals(findValueByCode(data, "Power"))) {
            powerState = true;
        }

        updateState("powerState", powerState ? OnOffType.ON : OnOffType.OFF);
        updateState("silentMode", silentMode ? OnOffType.ON : OnOffType.OFF);

        updateChannelState("inletTemperature", String.valueOf(inletTemperature), SIUnits.CELSIUS.getSymbol());
        updateChannelState("outletTemperature", String.valueOf(outletTemperature), SIUnits.CELSIUS.getSymbol());
        updateChannelState("ambientTemperature", String.valueOf(ambientTemperature), SIUnits.CELSIUS.getSymbol());

        updateChannelState("targetTemperature", String.valueOf(targetTemperature), SIUnits.CELSIUS.getSymbol());

        updateChannelState("consumption", String.valueOf(consumption), Units.WATT.toString());

        updateState("mode", DecimalType.valueOf(findValueByCode(data, "Mode")));
    }

    @Override
    public void handleUpdateError(ErrorDTO error) {
        if (error.totalSize > 0) {
            updateState("errorIsActive", OnOffType.ON);
            ErrorObjectResult obj = error.objectResult.get(0);
            String description = obj.getFaultCode() + " - " + obj.getDescription();
            updateState("errorMessage", StringType.valueOf(description));
        } else {
            updateState("errorIsActive", OnOffType.OFF);
        }
    }

    private void updateChannelState(String channelId, String stateAsString, @Nullable String unit) {
        if (unit != null) {
            updateState(channelId, new QuantityType<>(stateAsString + " " + unit));
        } else {
            DecimalType s = DecimalType.valueOf(stateAsString);
            updateState(channelId, s);
        }
    }

    private String findValueByCode(DataDTO data, String code) {
        for (DataObjectResult dataObjectResult : data.objectResult) {
            if (code.equals(dataObjectResult.code)) {
                return dataObjectResult.value;
            }
        }
        return "";
    }
}
