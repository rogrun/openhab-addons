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
package org.openhab.binding.aquatemp.internal;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AquatempBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class AquatempBindingConstants {
    public static final String BINDING_ID = "aquatemp";
    public static final String BINDING_NAME = "AquaTemp Binding";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPE_UIDS = Set.of(THING_TYPE_DEVICE);

    public static final String COUNT_API_CALLS = "countApiCalls";

    // References for needed API identifiers
    public static final String AQUATEMP_BASE_URL = "https://cloud.linked-go.com/cloudservice/api";
    public static final String IAM_BASE_URL = "https://cloud.linked-go.com/cloudservice/api";
    public static final String AQUATEMP_AUTHORIZE_URL = IAM_BASE_URL + "/app/user/login.json";

    public static final URI AQUATEMP_URI = URI.create("http://cloud.linked-go.com");

    public static final int REFRESH_TOKEN_EXPIRE = 15552000;

    public static final int API_TIMEOUT_MS = 20000;
    public static final String PROPERTY_ID = "deviceId";

    public static final Map<String, String> UNIT_MAP = Map.of( //
            "celsius", SIUnits.CELSIUS.getSymbol(), //
            "kelvin", Units.KELVIN.toString(), //
            "kilowattHour", Units.KILOWATT_HOUR.toString(), //
            "percent", Units.PERCENT.toString(), //
            "minute", Units.MINUTE.toString(), //
            "hour", Units.HOUR.toString(), //
            "hours", Units.HOUR.toString(), //
            "liter", Units.LITRE.toString(), //
            "cubicMeter", SIUnits.CUBIC_METRE.toString());

    public static final String CHANNEL_RUN_QUERY_ONCE = "runQueryOnce";
}
