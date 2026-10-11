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
package org.openhab.binding.aquatemp.internal.dto;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.aquatemp.internal.dto.device.DataDTO;
import org.openhab.binding.aquatemp.internal.dto.device.DeviceDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ObjectResult;
import org.openhab.binding.aquatemp.internal.dto.oauth.AuthorizeResponseDTO;
import org.openhab.core.test.java.JavaTest;

import com.google.gson.Gson;

/**
 * Tests parsing responses from both AquaTemp cloud APIs.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class AquatempDtoTest extends JavaTest {

    private final Gson gson = new Gson();

    @Test
    void parsesLegacyLoginResponse() {
        AuthorizeResponseDTO response = requireNonNull(
                gson.fromJson("{\"error_code\":\"0\",\"object_result\":{\"x-token\":\"token\"},\"is_reuslt_suc\":true}",
                        AuthorizeResponseDTO.class));

        assertThat(response.isReusltSuc, is(true));
        assertThat(response.objectResult.xToken, is("token"));
    }

    @Test
    void parsesGoHeatingLoginResponse() {
        AuthorizeResponseDTO response = requireNonNull(gson.fromJson(
                "{\"error_code\":\"0\",\"objectResult\":{\"x-token\":\"token\",\"accessKey\":\"key\",\"appId\":\"14\"},\"isReusltSuc\":true}",
                AuthorizeResponseDTO.class));

        assertThat(response.isReusltSuc, is(true));
        assertThat(response.objectResult.xToken, is("token"));
        assertThat(response.objectResult.accessKey, is("key"));
    }

    @Test
    void normalizesDeviceFieldsFromBothApis() {
        DeviceDTO legacy = requireNonNull(gson.fromJson(
                "{\"object_result\":[{\"device_code\":\"legacy-id\",\"device_nick_name\":\"Legacy\",\"device_status\":\"ONLINE\"}]}",
                DeviceDTO.class));
        DeviceDTO goHeating = requireNonNull(gson.fromJson(
                "{\"objectResult\":[{\"deviceId\":\"new-id\",\"deviceName\":\"New\",\"deviceStatus\":\"OFFLINE\"}]}",
                DeviceDTO.class));

        ObjectResult legacyDevice = legacy.objectResult.get(0);
        ObjectResult newDevice = goHeating.objectResult.get(0);
        assertThat(legacyDevice.getIdentifier(), is("legacy-id"));
        assertThat(legacyDevice.getDisplayName(), is("Legacy"));
        assertThat(newDevice.getIdentifier(), is("new-id"));
        assertThat(newDevice.getDisplayName(), is("New"));
    }

    @Test
    void parsesGoHeatingDeviceDataResponse() {
        DataDTO response = requireNonNull(gson.fromJson(
                "{\"error_code\":\"0\",\"objectResult\":[{\"code\":\"InletWaterTemp\",\"value\":\"12.5\",\"dataType\":\"TEMP\"}],\"isReusltSuc\":true}",
                DataDTO.class));

        assertThat(response.isReusltSuc, is(true));
        assertThat(response.objectResult.get(0).code, is("InletWaterTemp"));
        assertThat(response.objectResult.get(0).value, is("12.5"));
    }
}
