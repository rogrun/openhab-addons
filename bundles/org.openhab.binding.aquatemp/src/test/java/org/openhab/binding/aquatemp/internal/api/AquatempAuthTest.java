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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.test.java.JavaTest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests for {@link AquatempAuth} request generation.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class AquatempAuthTest extends JavaTest {

    @Test
    void createsLegacyLoginRequest() {
        JsonObject request = JsonParser
                .parseString(AquatempAuth.buildLoginRequest(AquatempApiType.LINKED_GO, "user@example.org", "secret"))
                .getAsJsonObject();

        assertThat(request.get("user_name").getAsString(), is("user@example.org"));
        assertThat(request.get("password").getAsString(), is("secret"));
        assertThat(request.get("type").getAsString(), is("2"));
    }

    @Test
    void createsGoHeatingLoginRequest() {
        JsonObject request = JsonParser
                .parseString(AquatempAuth.buildLoginRequest(AquatempApiType.GO_HEATING, "user@example.org", "secret"))
                .getAsJsonObject();

        assertThat(request.get("userName").getAsString(), is("user@example.org"));
        assertThat(request.get("password").getAsString(), is("5ebe2294ecd0e0f08eab7690d2a6ee69"));
        assertThat(request.get("type").getAsInt(), is(2));
        assertThat(request.get("loginSource").getAsString(), is("Flutter-Android"));
        assertThat(request.get("areaCode").getAsString(), is("en"));
        assertThat(request.get("appId").getAsInt(), is(14));
    }
}
