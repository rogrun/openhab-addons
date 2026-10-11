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

import java.net.URI;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The cloud API used by an AquaTemp account.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public enum AquatempApiType {
    LINKED_GO("linkedGo", "https://cloud.linked-go.com/cloudservice/api", "/app/user/login.json",
            URI.create("https://cloud.linked-go.com")),
    GO_HEATING("goHeating", "https://cloud.go-heating.com/crmservice/api", "/app/user/login?lang=en",
            URI.create("https://cloud.go-heating.com"));

    private final String configurationValue;
    private final String baseUrl;
    private final String loginPath;
    private final URI origin;

    AquatempApiType(String configurationValue, String baseUrl, String loginPath, URI origin) {
        this.configurationValue = configurationValue;
        this.baseUrl = baseUrl;
        this.loginPath = loginPath;
        this.origin = origin;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getLoginUrl() {
        return baseUrl + loginPath;
    }

    public URI getOrigin() {
        return origin;
    }

    public static AquatempApiType fromConfig(String value) {
        String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
        for (AquatempApiType apiType : values()) {
            if (apiType.configurationValue.toLowerCase(Locale.ROOT).equals(normalizedValue)) {
                return apiType;
            }
        }
        throw new IllegalArgumentException("Unsupported AquaTemp cloud API: " + value);
    }
}
