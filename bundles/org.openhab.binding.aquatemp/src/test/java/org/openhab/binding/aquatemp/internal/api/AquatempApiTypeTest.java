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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.test.java.JavaTest;

/**
 * Tests for {@link AquatempApiType}.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class AquatempApiTypeTest extends JavaTest {

    @Test
    void parsesConfiguredApiTypes() {
        assertThat(AquatempApiType.fromConfig("linkedGo"), is(AquatempApiType.LINKED_GO));
        assertThat(AquatempApiType.fromConfig("goHeating"), is(AquatempApiType.GO_HEATING));
        assertThat(AquatempApiType.LINKED_GO.getLoginUrl(),
                is("https://cloud.linked-go.com/cloudservice/api/app/user/login.json"));
        assertThat(AquatempApiType.GO_HEATING.getLoginUrl(),
                is("https://cloud.go-heating.com/crmservice/api/app/user/login?lang=en"));
    }

    @Test
    void rejectsUnknownApiType() {
        assertThrows(IllegalArgumentException.class, () -> AquatempApiType.fromConfig("unknown"));
    }
}
