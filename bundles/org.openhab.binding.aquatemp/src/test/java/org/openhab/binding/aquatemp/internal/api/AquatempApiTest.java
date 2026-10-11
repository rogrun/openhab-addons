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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.aquatemp.internal.dto.device.DeviceStatusDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ErrorDTO;
import org.openhab.core.test.java.JavaTest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests for API-specific request payloads.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class AquatempApiTest extends JavaTest {

    @Test
    void legacyDeviceListUsesEmptyRequestBody() {
        assertThat(AquatempApi.buildDeviceListRequest(AquatempApiType.LINKED_GO, List.of("ignored")), is(""));
    }

    @Test
    void goHeatingDeviceListUsesConfiguredProductIds() {
        String request = AquatempApi.buildDeviceListRequest(AquatempApiType.GO_HEATING, List.of("123", "456"));

        assertThat(request, is("{\"productIds\":[\"123\",\"456\"]}"));
    }

    @Test
    void goHeatingDeviceDataRequestUsesCapturedFormat() {
        JsonObject request = JsonParser.parseString(AquatempApi.buildDeviceDataRequest("device-id")).getAsJsonObject();

        assertThat(request.get("deviceCode").getAsString(), is("device-id"));
        assertThat(request.getAsJsonArray("codes").asList().stream().map(element -> element.getAsString()).toList(),
                contains("SetPVReadyMode", "Power", "SetFrequencyMode", "SetFanMode", "EnBoostMode", "EnAPPTimeUpdate",
                        "Mode", "SetCurrentTemp", "ModeType", "SetTempUnit", "EnOutletWaterTempSensor", "EnSlient",
                        "SetCoolTargetTemp", "SetHeatTargetTemp", "SetAutoTargetTemp", "SetCoolMinTargetTemp",
                        "SetCoolMaxTargetTemp", "SetHeatMinTargetTemp", "SetHeatMaxTargetTemp", "InletWaterTemp",
                        "OutletWaterTemp", "AT", "EnTimerSlient", "SetTimerSlientStartTime", "SetTimerSlientStopTime",
                        "EnTimer1On", "EnTimer1Off", "SetTimer1StartHour", "SetTimer1StartMinute", "SetTimer1StopHour",
                        "SetTimer1StopMinute", "SetCompType", "Sys1CompWorkingFreq", "SetHeatMaxFreq", "SetCoolMaxFreq",
                        "EnSupportAI", "SetAIMode", "EnAIPower", "CompressorCurrentDetect",
                        "InverterBoardACInputVoltage", "Fault1", "MainBoardSoftwareVer", "MainBoardSoftwareCode",
                        "DisplaySoftwareVer", "DisplaySoftwareCode"));
        assertThat(request.getAsJsonArray("bitCodes").asList().stream().map(element -> element.getAsString()).toList(),
                contains("Sys1HighPressureSwitch", "Sys1LowPressureSwitch", "WaterFlowSwitch", "EmergencySwitch",
                        "ModeSwitch", "Master/SlaveSwitch", "Sys1CompState", "InletWaterTempSensorFault",
                        "OutletWaterTempSensorFault", "ATSensorFault"));
    }

    @Test
    void goHeatingDeviceCodeRequestUsesCapturedFormat() {
        assertThat(AquatempApi.buildDeviceCodeRequest("device-id"), is("{\"deviceCode\":\"device-id\"}"));
    }

    @Test
    void parsesCapturedGoHeatingDeviceStatus() {
        String json = """
                {"error_code":"0","error_msg":"Success","objectResult":{"is_fault":false,"isFault":false,"status":"ONLINE"},"isReusltSuc":true}
                """;

        DeviceStatusDTO status = Objects.requireNonNull(new Gson().fromJson(json, DeviceStatusDTO.class));

        assertThat(status.objectResult.isFault, is(false));
        assertThat(status.objectResult.deviceStatus, is("ONLINE"));
        assertThat(status.isReusltSuc, is(true));
    }

    @Test
    void parsesCapturedEmptyGoHeatingFaultList() {
        String json = """
                {"error_code":"0","error_msg":"Success","totalSize":0,"totalPage":0,"nextPage":false,"objectResult":[],"isReusltSuc":true}
                """;

        ErrorDTO errors = Objects.requireNonNull(new Gson().fromJson(json, ErrorDTO.class));

        assertThat(errors.totalSize, is(0));
        assertThat(errors.objectResult.isEmpty(), is(true));
        assertThat(errors.isReusltSuc, is(true));
    }

    @Test
    void buildsApiSpecificPowerControlRequests() {
        assertThat(AquatempApi.buildControlRequest(AquatempApiType.GO_HEATING, "device-id", Map.of("Power", "1")),
                is("{\"deviceCode\":\"device-id\",\"params\":[{\"code\":\"Power\",\"value\":\"1\"}]}"));
        assertThat(AquatempApi.buildControlRequest(AquatempApiType.GO_HEATING, "device-id", Map.of("Power", "0")),
                is("{\"deviceCode\":\"device-id\",\"params\":[{\"code\":\"Power\",\"value\":\"0\"}]}"));
        assertThat(
                AquatempApi.buildControlRequest(AquatempApiType.GO_HEATING, "device-id",
                        Map.of("SetHeatTargetTemp", "30.5")),
                is("{\"deviceCode\":\"device-id\",\"params\":[{\"code\":\"SetHeatTargetTemp\",\"value\":\"30.5\"}]}"));
        assertThat(
                AquatempApi.buildControlRequest(AquatempApiType.GO_HEATING, "device-id",
                        Map.of("SetAutoTargetTemp", "30.5")),
                is("{\"deviceCode\":\"device-id\",\"params\":[{\"code\":\"SetAutoTargetTemp\",\"value\":\"30.5\"}]}"));
        assertThat(
                AquatempApi.buildControlRequest(AquatempApiType.GO_HEATING, "device-id",
                        Map.of("SetCoolTargetTemp", "30.5")),
                is("{\"deviceCode\":\"device-id\",\"params\":[{\"code\":\"SetCoolTargetTemp\",\"value\":\"30.5\"}]}"));
        assertThat(AquatempApi.buildControlRequest(AquatempApiType.GO_HEATING, "device-id", Map.of("EnSlient", "1")),
                is("{\"deviceCode\":\"device-id\",\"params\":[{\"code\":\"EnSlient\",\"value\":\"1\"}]}"));
        assertThat(AquatempApi.buildControlRequest(AquatempApiType.LINKED_GO, "device-id", Map.of("Power", "1")),
                is("{\"param\":[{\"device_code\":\"device-id\",\"protocol_code\":\"Power\",\"value\":\"1\"}]}"));
        assertThat(AquatempApi.buildControlRequest(AquatempApiType.LINKED_GO, "device-id", Map.of("Manual-mute", "0")),
                is("{\"param\":[{\"device_code\":\"device-id\",\"protocol_code\":\"Manual-mute\",\"value\":\"0\"}]}"));
    }

    @Test
    void buildsApiSpecificModeControlRequests() {
        Map<String, String> goHeatingParameters = new java.util.LinkedHashMap<>();
        goHeatingParameters.put("Mode", "0");
        goHeatingParameters.put("SetCurrentTemp", "31.0");

        assertThat(AquatempApi.buildControlRequest(AquatempApiType.GO_HEATING, "device-id", goHeatingParameters), is(
                "{\"deviceCode\":\"device-id\",\"params\":[{\"code\":\"Mode\",\"value\":\"0\"},{\"code\":\"SetCurrentTemp\",\"value\":\"31.0\"}]}"));
        goHeatingParameters.put("Mode", "2");
        assertThat(AquatempApi.buildControlRequest(AquatempApiType.GO_HEATING, "device-id", goHeatingParameters), is(
                "{\"deviceCode\":\"device-id\",\"params\":[{\"code\":\"Mode\",\"value\":\"2\"},{\"code\":\"SetCurrentTemp\",\"value\":\"31.0\"}]}"));
        assertThat(AquatempApi.buildControlRequest(AquatempApiType.LINKED_GO, "device-id", Map.of("mode", "0")),
                is("{\"param\":[{\"device_code\":\"device-id\",\"protocol_code\":\"mode\",\"value\":\"0\"}]}"));
    }
}
