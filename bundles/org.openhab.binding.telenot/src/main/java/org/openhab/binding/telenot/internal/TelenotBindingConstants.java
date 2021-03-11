/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.telenot.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TelenotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class TelenotBindingConstants {

    private static final String BINDING_ID = "telenot";

    // List of all Thing Type UIDs
    // public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    public static final ThingTypeUID THING_TYPE_IPBRIDGE = new ThingTypeUID(BINDING_ID, "ipbridge");
    public static final ThingTypeUID THING_TYPE_SB = new ThingTypeUID(BINDING_ID, "sb");
    public static final ThingTypeUID THING_TYPE_MP = new ThingTypeUID(BINDING_ID, "mp");
    public static final ThingTypeUID THING_TYPE_MB = new ThingTypeUID(BINDING_ID, "mb");
    public static final ThingTypeUID THING_TYPE_EMA_STATE = new ThingTypeUID(BINDING_ID, "emaState");

    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_MP, THING_TYPE_SB, THING_TYPE_MB).collect(Collectors.toSet()));

    // Channel IDs for MP / MB handler
    public static final String PROPERTY_ADDRESS = "address";
    public static final String PROPERTY_CHANNEL = "channel";
    public static final String PROPERTY_ID = "id";

    public static final String CHANNEL_CONTACT = "contact";
    public static final String CHANNEL_CONTACT_MB = "contactMB";
    public static final String CHANNEL_DISABLE_MB = "disableMB";
    public static final String CHANNEL_STATE = "state";

    // Channel IDs for SBHandler
    public static final String CHANNEL_DISARMED = "disarmed";
    public static final String CHANNEL_INTERNALLY_ARMED = "internallyArmed";
    public static final String CHANNEL_EXTERNALLY_ARMED = "externallyArmed";
    public static final String CHANNEL_DISARM = "disarm";
    public static final String CHANNEL_INTERNAL_ARM = "internalArm";
    public static final String CHANNEL_EXTERNAL_ARM = "externalArm";
    public static final String CHANNEL_RESET_ALARM = "resetAlarm";

    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_MALFUNCTION = "malfunction";
    public static final String CHANNEL_READY_TO_ARM_INTERNALLY = "readyToArmInternally";
    public static final String CHANNEL_READY_TO_ARM_EXTERNALLY = "readyToArmExternally";
    public static final String CHANNEL_STATE_INTERNAL_SIGNAL_HORN = "statusInternalSignalHorn";

    // Channel ID for emaStateHandler
    public static final String CHANNEL_INT_ARMED_DATETIME = "intArmedDatetime";
    public static final String CHANNEL_EXT_ARMED_DATETIME = "extArmedDatetime";
    public static final String CHANNEL_DISARMED_DATETIME = "disarmedDatetime";
    public static final String CHANNEL_ALARM_DATETIME = "alarmDatetime";
    public static final String CHANNEL_INTRUSION_DATETIME = "intrusionDatetime";
    public static final String CHANNEL_BATTERY_MALFUNCTION_DATETIME = "batteryMalfunctionDatetime";
    public static final String CHANNEL_POWER_OUTAGE_DATETIME = "powerOutageDatetime";
    public static final String CHANNEL_OPTICAL_FLASHER_MALFUNCTION_DATETIME = "flasherMalfunctionDatetime";
    public static final String CHANNEL_HORN_1_MALFUNCTION_DATETIME = "horn1MalfunctionDatetime";
    public static final String CHANNEL_HORN_2_MALFUNCTION_DATETIME = "horn2MalfunctionDatetime";

    public static final String CHANNEL_INT_ARMED_CONTACT = "intArmedContact";
    public static final String CHANNEL_EXT_ARMED_CONTACT = "extArmedContact";
    public static final String CHANNEL_DISARMED_CONTACT = "disarmedContact";
    public static final String CHANNEL_ALARM_CONTACT = "alarmContact";
    public static final String CHANNEL_INTRUSION_CONTACT = "intrusionContact";
    public static final String CHANNEL_BATTERY_MALFUNCTION_CONTACT = "batteryMalfunctionContact";
    public static final String CHANNEL_POWER_OUTAGE_CONTACT = "powerOutageContact";
    public static final String CHANNEL_OPTICAL_FLASHER_MALFUNCTION_CONTACT = "flasherMalfunctionContact";
    public static final String CHANNEL_HORN_1_MALFUNCTION_CONTACT = "horn1MalfunctionContact";
    public static final String CHANNEL_HORN_2_MALFUNCTION_CONTACT = "horn2MalfunctionContact";

    public static final String CHANNEL_ALARM_SET_CLEAR = "alarmSetClear";
    public static final String CHANNEL_INTRUSION_SET_CLEAR = "intrusionSetClear";
    public static final String CHANNEL_BATTERY_MALFUNCTION_SET_CLEAR = "batteryMalfunctionSetClear";
    public static final String CHANNEL_POWER_OUTAGE_SET_CLEAR = "powerOutageSetClear";
    public static final String CHANNEL_OPTICAL_FLASHER_MALFUNCTION_SET_CLEAR = "flasherMalfunctionSetClear";
    public static final String CHANNEL_HORN_1_MALFUNCTION_SET_CLEAR = "horn1MalfunctionSetClear";
    public static final String CHANNEL_HORN_2_MALFUNCTION_SET_CLEAR = "horn2MalfunctionSetClear";

    // public static final List<ChannelTypeDescription> CHANNEL_TYPES = Util.readXMLChannelConfig();
}
