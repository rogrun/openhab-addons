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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.aquatemp.internal.dto.device.DataDTO;
import org.openhab.binding.aquatemp.internal.dto.device.DataObjectResult;
import org.openhab.binding.aquatemp.internal.dto.device.ErrorObjectResult;
import org.openhab.core.items.ManagedItemProvider;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.RefreshType;

/**
 * Tests for API data mappings in {@link DeviceHandler}.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DeviceHandlerTest extends JavaTest {

    @Test
    void selectsModeSpecificTargetTemperatureCode() {
        assertThat(DeviceHandler.getTargetTemperatureCode("0"), is("SetCoolTargetTemp"));
        assertThat(DeviceHandler.getTargetTemperatureCode("1"), is("SetHeatTargetTemp"));
        assertThat(DeviceHandler.getTargetTemperatureCode("2"), is("SetAutoTargetTemp"));
        assertThat(DeviceHandler.getTargetTemperatureCode("unknown"), is("SetCurrentTemp"));
    }

    @Test
    void formatsCompleteAndPartialFaultInformation() {
        ErrorObjectResult fault = new ErrorObjectResult();
        fault.setFaultCode("E01");
        fault.setDescription("Sensor fault");
        assertThat(DeviceHandler.formatError(fault), is("E01 - Sensor fault"));

        fault.setFaultCode(null);
        assertThat(DeviceHandler.formatError(fault), is("Sensor fault"));

        fault.setDescription(null);
        assertThat(DeviceHandler.formatError(fault), is(""));
    }

    @Test
    void findsValueUsingGoHeatingAlias() {
        DataObjectResult result = new DataObjectResult();
        result.code = "Sys1CompWorkingFreq";
        result.value = "35";
        DataDTO data = new DataDTO();
        data.objectResult = java.util.List.of(result);

        assertThat(DeviceHandler.findValueByCode(data, "O07", "Sys1CompWorkingFreq"), is("35"));
        assertThat(DeviceHandler.findValueByCode(data, "O08", "CompressorCurrentDetect"), is(""));
    }

    @Test
    void refreshCommandRequestsCurrentState() {
        AtomicBoolean refreshed = new AtomicBoolean();
        DeviceHandler handler = new DeviceHandler(mock(Thing.class), mock(ItemChannelLinkRegistry.class),
                mock(ManagedItemProvider.class)) {
            @Override
            public void initChannelState() {
                refreshed.set(true);
            }
        };

        handler.handleCommand(mock(ChannelUID.class), RefreshType.REFRESH);

        assertThat(refreshed.get(), is(true));
    }
}
