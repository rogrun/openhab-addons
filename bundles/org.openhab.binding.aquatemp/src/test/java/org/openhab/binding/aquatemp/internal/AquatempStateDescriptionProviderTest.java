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
package org.openhab.binding.aquatemp.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.StateDescription;

/**
 * Tests for {@link AquatempStateDescriptionProvider}.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class AquatempStateDescriptionProviderTest extends JavaTest {

    @Test
    void providesDeviceSpecificTargetTemperatureRange() {
        AquatempStateDescriptionProvider provider = new AquatempStateDescriptionProvider(mock(EventPublisher.class),
                mock(ItemChannelLinkRegistry.class), mock(ChannelTypeI18nLocalizationService.class));
        ChannelUID channelUID = new ChannelUID(new ThingUID("aquatemp:device:test"), "target-temperature");
        Channel channel = mock(Channel.class);
        when(channel.getUID()).thenReturn(channelUID);

        provider.setTargetTemperatureRange(channelUID, BigDecimal.valueOf(6), BigDecimal.valueOf(35));
        StateDescription description = Objects.requireNonNull(provider.getStateDescription(channel, null, null));

        assertThat(description.getMinimum(), is(BigDecimal.valueOf(6)));
        assertThat(description.getMaximum(), is(BigDecimal.valueOf(35)));
        assertThat(description.getStep(), is(BigDecimal.valueOf(0.5)));
    }
}
