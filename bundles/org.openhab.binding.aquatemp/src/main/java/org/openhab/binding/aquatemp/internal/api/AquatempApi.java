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
package org.openhab.binding.aquatemp.internal.api;

import static org.openhab.binding.aquatemp.internal.AquatempBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.aquatemp.internal.dto.device.DataDTO;
import org.openhab.binding.aquatemp.internal.dto.device.DeviceDTO;
import org.openhab.binding.aquatemp.internal.dto.device.DeviceStatusDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ErrorDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ParamDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ParamRequest;
import org.openhab.binding.aquatemp.internal.dto.oauth.TokenResponseDTO;
import org.openhab.binding.aquatemp.internal.handler.AquatempBridgeHandler;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link AquatempApi} is responsible for managing all communication with
 * the Aquatemp API service.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class AquatempApi {

    private static final String HTTP_METHOD_POST = "POST";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    private final Logger logger = LoggerFactory.getLogger(AquatempApi.class);

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private static final int TOKEN_MIN_DIFF_MS = 120000;

    private static final List<String> GO_HEATING_DATA_CODES = List.of("SetPVReadyMode", "Power", "SetFrequencyMode",
            "SetFanMode", "EnBoostMode", "EnAPPTimeUpdate", "Mode", "SetCurrentTemp", "ModeType", "SetTempUnit",
            "EnOutletWaterTempSensor", "EnSlient", "SetCoolTargetTemp", "SetHeatTargetTemp", "SetAutoTargetTemp",
            "SetCoolMinTargetTemp", "SetCoolMaxTargetTemp", "SetHeatMinTargetTemp", "SetHeatMaxTargetTemp",
            "InletWaterTemp", "OutletWaterTemp", "AT", "EnTimerSlient", "SetTimerSlientStartTime",
            "SetTimerSlientStopTime", "EnTimer1On", "EnTimer1Off", "SetTimer1StartHour", "SetTimer1StartMinute",
            "SetTimer1StopHour", "SetTimer1StopMinute", "SetCompType", "Sys1CompWorkingFreq", "SetHeatMaxFreq",
            "SetCoolMaxFreq", "EnSupportAI", "SetAIMode", "EnAIPower", "CompressorCurrentDetect",
            "InverterBoardACInputVoltage", "Fault1", "MainBoardSoftwareVer", "MainBoardSoftwareCode",
            "DisplaySoftwareVer", "DisplaySoftwareCode", "Z04", "Z07", "O07", "O08", "state_mode");

    private static final List<String> GO_HEATING_BIT_CODES = List.of("Sys1HighPressureSwitch", "Sys1LowPressureSwitch",
            "WaterFlowSwitch", "EmergencySwitch", "ModeSwitch", "Master/SlaveSwitch", "Sys1CompState",
            "InletWaterTempSensorFault", "OutletWaterTempSensorFault", "ATSensorFault", "S03");

    public final Properties httpHeaders;

    private final AquatempBridgeHandler bridgeHandler;
    private final HttpClient httpClient;

    private final String user;
    private final String password;
    private final AquatempApiType apiType;
    private final List<String> productIds;

    private long tokenExpiryDate;
    private long refreshTokenExpiryDate;

    private @NonNullByDefault({}) AquatempAuth aquatempAuth;

    public AquatempApi(final AquatempBridgeHandler bridgeHandler, HttpClient httpClient, String user, String password,
            AquatempApiType apiType, String productIds) {
        this.bridgeHandler = bridgeHandler;
        this.httpClient = httpClient;
        this.user = user;
        this.password = password;
        this.apiType = apiType;
        this.productIds = Arrays.stream(productIds.split(",")).map(String::trim).filter(id -> !id.isEmpty()).toList();
        tokenResponse = null;
        httpHeaders = new Properties();
        httpHeaders.put("User-Agent", "openhab-aquatemp-api/2.0");

        createAuthClientService();
        authorize();
    }

    public Gson getGson() {
        return GSON;
    }

    private @Nullable TokenResponseDTO tokenResponse;

    public void setTokenResponseDTO(TokenResponseDTO newTokenResponse) {
        tokenResponse = newTokenResponse;
    }

    public @Nullable TokenResponseDTO getTokenResponseDTO() throws AquatempAuthException {
        return tokenResponse;
    }

    private @Nullable String xToken;

    public void setToken(String xToken) {
        this.xToken = xToken;
    }

    public void setTokenExpiryDate(long expiresIn) {
        tokenExpiryDate = System.currentTimeMillis() + expiresIn;
    }

    public long getTokenExpiryDate() {
        return tokenExpiryDate;
    }

    public void setRefreshTokenExpiryDate(long expiresIn) {
        refreshTokenExpiryDate = System.currentTimeMillis() + expiresIn;
    }

    public long getRefreshTokenExpiryDate() {
        return refreshTokenExpiryDate;
    }

    public void createAuthClientService() {
        String bridgeUID = bridgeHandler.getThing().getUID().getAsString();
        logger.debug("API: Creating Auth Client Service for {}", bridgeUID);
        aquatempAuth = new AquatempAuth(this, bridgeHandler, httpClient, user, password, apiType);
    }

    /**
     * Check to see if the Aquatemp authorization process is complete. This will be determined
     * by requesting an AccessTokenResponse from the API. If we get a valid
     * response, then assume that the Aquatemp authorization process is complete. Otherwise,
     * start the Aquatemp authorization process.
     */
    private void authorize() {
        try {
            if (xToken != null) {
                long difference = getTokenExpiryDate() - System.currentTimeMillis();
                if (difference <= TOKEN_MIN_DIFF_MS) {
                    aquatempAuth.setState(AquatempAuthState.NEED_AUTH);
                } else {
                    aquatempAuth.setState(AquatempAuthState.COMPLETE);
                }
            }
            aquatempAuth.doAuthorization();
        } catch (AquatempAuthException e) {
            if (logger.isDebugEnabled()) {
                logger.info("API: The Aquatemp authorization process threw an exception", e);
            } else {
                logger.info("API: The Aquatemp authorization process threw an exception: {}", e.getMessage());
            }
            aquatempAuth.setState(AquatempAuthState.NEED_AUTH);
        }
    }

    public void checkExpiringToken() {
        logger.debug("Checking if new access token is needed...");
        try {
            long refreshTokenExpire = getTokenExpiryDate() - System.currentTimeMillis();
            if (refreshTokenExpire <= TOKEN_MIN_DIFF_MS) {
                aquatempAuth.setState(AquatempAuthState.NEED_AUTH);
                aquatempAuth.doAuthorization();
            }
        } catch (AquatempAuthException e) {
            if (logger.isDebugEnabled()) {
                logger.info("API: The Aquatemp authorization process threw an exception", e);
            } else {
                logger.info("API: The Aquatemp authorization process threw an exception: {}", e.getMessage());
            }
            aquatempAuth.setState(AquatempAuthState.NEED_AUTH);
        }
    }

    public @Nullable DeviceDTO getAllDevices() throws AquatempCommunicationException {
        String path;
        if (apiType == AquatempApiType.GO_HEATING) {
            path = "/app/device/deviceList?lang=en";
        } else {
            path = "/app/device/deviceList.json";
        }
        String response = executePost(apiType.getBaseUrl() + path, buildDeviceListRequest(apiType, productIds));
        return GSON.fromJson(response, DeviceDTO.class);
    }

    public @Nullable ErrorDTO getErrorsByDevice(String deviceId) throws AquatempCommunicationException {
        String path;
        String json;
        if (apiType == AquatempApiType.GO_HEATING) {
            path = "/app/device/getFaultDataByDeviceCode?lang=en";
            json = buildDeviceCodeRequest(deviceId);
        } else {
            path = "/app/device/getFaultDataByDeviceCode.json";
            json = "{ \"device_code\": \"" + deviceId + "\" }";
        }
        String response = executePost(apiType.getBaseUrl() + path, json);
        return GSON.fromJson(response, ErrorDTO.class);
    }

    public @Nullable DeviceStatusDTO getDeviceStatus(String deviceId) throws AquatempCommunicationException {
        String response = executePost(apiType.getBaseUrl() + "/app/device/getDeviceStatus?lang=en",
                buildDeviceCodeRequest(deviceId));
        return GSON.fromJson(response, DeviceStatusDTO.class);
    }

    public @Nullable DataDTO getDataByCode(String code) throws AquatempCommunicationException {
        String path;
        String json;
        if (apiType == AquatempApiType.GO_HEATING) {
            path = "/app/device/v5/getDataByCode?lang=en";
            json = buildDeviceDataRequest(code);
        } else {
            path = "/app/device/getDataByCode.json";
            json = "{ \"device_code\": \"" + code
                    + "\", \"protocal_codes\":[\"Power\",\"Mode\",\"Manual-mute\",\"T01\",\"T02\",\"2074\",\"2075\",\"2076\",\"2077\",\"H03\",\"Set_Temp\",\"R08\",\"R09\",\"R10\",\"R11\",\"R01\",\"R02\",\"R03\",\"T03\",\"1158\",\"1159\",\"F17\",\"H02\",\"T04\",\"T05\",\"T07\",\"T14\"] }";
        }

        String response = executePost(apiType.getBaseUrl() + path, json);
        return GSON.fromJson(response, DataDTO.class);
    }

    public boolean supportsFaultData() {
        return true;
    }

    public boolean supportsDeviceFaultStatus() {
        return apiType == AquatempApiType.GO_HEATING;
    }

    static String buildDeviceCodeRequest(String deviceCode) {
        JsonObject request = new JsonObject();
        request.addProperty("deviceCode", deviceCode);
        return GSON.toJson(request);
    }

    public void controlDevice(String deviceCode, Map<String, String> parameters) throws AquatempCommunicationException {
        String path;
        if (apiType == AquatempApiType.GO_HEATING) {
            if (parameters.keySet().stream()
                    .anyMatch(code -> !"Power".equals(code) && !"SetHeatTargetTemp".equals(code)
                            && !"SetAutoTargetTemp".equals(code) && !"SetCoolTargetTemp".equals(code)
                            && !"EnSlient".equals(code) && !"Mode".equals(code) && !"SetCurrentTemp".equals(code))) {
                throw new AquatempCommunicationException(
                        "The captured Go Heating request format is not available for this control command");
            }
            path = "/app/device/v3/control?lang=en";
        } else {
            path = "/app/device/control.json";
        }

        String response = executePost(apiType.getBaseUrl() + path,
                buildControlRequest(apiType, deviceCode, parameters));
        if (apiType == AquatempApiType.GO_HEATING) {
            JsonObject result = JsonParser.parseString(response).getAsJsonObject();
            if (!"0".equals(result.get("error_code").getAsString()) || !result.get("isReusltSuc").getAsBoolean()
                    || !result.get("objectResult").getAsBoolean()) {
                throw new AquatempCommunicationException("The Go Heating API rejected the control command");
            }
        }
    }

    public void controlTargetTemperature(String deviceCode, String value, String mode)
            throws AquatempCommunicationException {
        if (apiType == AquatempApiType.GO_HEATING) {
            String code = switch (mode) {
                case "0" -> "SetCoolTargetTemp";
                case "1" -> "SetHeatTargetTemp";
                case "2" -> "SetAutoTargetTemp";
                default -> throw new AquatempCommunicationException(
                        "Go Heating target-temperature control is not confirmed for the current mode");
            };
            controlDevice(deviceCode, Map.of(code, value));
        } else {
            controlDevice(deviceCode, Map.of("R01", value, "R02", value, "R03", value, "Set_Temp", value));
        }
    }

    public void controlSilentMode(String deviceCode, String value) throws AquatempCommunicationException {
        controlDevice(deviceCode, Map.of(apiType == AquatempApiType.GO_HEATING ? "EnSlient" : "Manual-mute", value));
    }

    public void controlMode(String deviceCode, String mode, String currentTargetTemperature)
            throws AquatempCommunicationException {
        if (apiType == AquatempApiType.GO_HEATING) {
            if (!"0".equals(mode) && !"1".equals(mode) && !"2".equals(mode)) {
                throw new AquatempCommunicationException("The Go Heating API does not support the requested mode");
            }
            if (currentTargetTemperature.isBlank()) {
                throw new AquatempCommunicationException(
                        "The current target temperature is not available for the Go Heating mode command");
            }
            Map<String, String> parameters = new LinkedHashMap<>();
            parameters.put("Mode", mode);
            parameters.put("SetCurrentTemp", currentTargetTemperature);
            controlDevice(deviceCode, parameters);
        } else {
            controlDevice(deviceCode, Map.of("mode", mode));
        }
    }

    private String executePost(String url, String json) throws AquatempCommunicationException {
        try {
            logger.trace("API: POST request to '{}'", url);
            long startTime = System.currentTimeMillis();
            String response = HttpUtil.executeUrl(HTTP_METHOD_POST, url, setHeaders(),
                    new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), CONTENT_TYPE_APPLICATION_JSON,
                    API_TIMEOUT_MS);
            logger.trace("API: POST request completed in {} ms", System.currentTimeMillis() - startTime);
            return response;
        } catch (IOException e) {
            authorize();
            throw new AquatempCommunicationException("Unable to communicate with the AquaTemp API: " + e.getMessage());
        } catch (AquatempAuthException e) {
            authorize();
            throw new AquatempCommunicationException("Unable to authenticate with the AquaTemp API");
        }
    }

    static String buildDeviceListRequest(AquatempApiType apiType, List<String> productIds) {
        return apiType == AquatempApiType.GO_HEATING ? GSON.toJson(new DeviceListRequest(productIds)) : "";
    }

    static String buildDeviceDataRequest(String deviceCode) {
        return GSON.toJson(new DeviceDataRequest(deviceCode, GO_HEATING_DATA_CODES, GO_HEATING_BIT_CODES));
    }

    static String buildControlRequest(AquatempApiType apiType, String deviceCode, Map<String, String> parameters) {
        if (apiType == AquatempApiType.GO_HEATING) {
            List<ControlParameter> controlParameters = parameters.entrySet().stream()
                    .map(entry -> new ControlParameter(entry.getKey(), entry.getValue())).toList();
            return GSON.toJson(new ControlRequest(deviceCode, controlParameters));
        }

        ParamRequest request = new ParamRequest();
        parameters.forEach((code, value) -> request.param.add(new ParamDTO(deviceCode, code, value)));
        return GSON.toJson(request);
    }

    private Properties setHeaders() throws AquatempAuthException {
        if (xToken == null) {
            throw new AquatempAuthException("Can not set auth header because access token is null");
        } else {
            String token = Objects.requireNonNull(xToken);
            Properties headers = new Properties();
            headers.putAll(httpHeaders);
            headers.put("x-token", token);
            return headers;
        }
    }

    private record DeviceListRequest(List<String> productIds) {
    }

    private record DeviceDataRequest(String deviceCode, List<String> codes, List<String> bitCodes) {
    }

    private record ControlRequest(String deviceCode, List<ControlParameter> params) {
    }

    private record ControlParameter(String code, String value) {
    }
}
