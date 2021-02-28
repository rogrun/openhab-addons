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
package org.openhab.binding.telenot.internal.protocol;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The various message types that come from the GMS interface
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public enum TelenotMsgType {
    ACK, // ACK Message
    CONF_ACK, // CONF_ACK Message
    MP, // Meldebereiche
    SB, // Sicherungsbereiche
    SYS_EXT_ARMED, // system sytem externally armed
    SYS_INT_ARMED, // system sytem internally armed
    SYS_DISARMED, // system sytem disarmed
    ALARM,
    INTRUSION, //
    BATTERY_MALFUNCTION, //
    POWER_OUTAGE, //
    OPTICAL_FLASHER_MALFUNCTION, //
    HORN_1_MALFUNCTION, //
    HORN_2_MALFUNCTION, //
    INVALID; // invalid message

    /** hash map from protocol message heading to type */
    private static Map<String, TelenotMsgType> startToMsgType = new HashMap<>();

    static {
        startToMsgType.put("6802026840024216", TelenotMsgType.ACK);
        startToMsgType.put("6802026800020216", TelenotMsgType.CONF_ACK);

        // MP Address
        startToMsgType.put("682e2e687302222400000001", TelenotMsgType.MP); // FW: 24.44
        startToMsgType.put("6846466873023a2400050002", TelenotMsgType.MP); // FW: 25.56
        startToMsgType.put("686060687302542400050002", TelenotMsgType.MP); // FW: 33.68

        // State SB Address and MG State
        startToMsgType.put("683e3e687302322400050002", TelenotMsgType.SB); // FW: 24.44
        startToMsgType.put("689393687302872400000001", TelenotMsgType.SB); // FW: 33.68

        // Details for Sicherungsbereich 1 (2c2c)

        // Alarm signal / alarm signal reset ;"682c2c68730205020 0122"
        // Alarm signal / alarm signal reset ;"682c2c68730205020 01a2"
        // REGEX ^682c2c6873020502\w\w\w\w\w\w01(22|a2)(.*)$
        // startToMsgType.put("682c2c687302050201000301", TelenotMsgType.ALARM);

        // info message "sytem externally armed" ;"682c2c68730205020005320161"
        startToMsgType.put("682c2c687302050200053201", TelenotMsgType.SYS_EXT_ARMED);

        // info message "sytem internally armed" ;"682c2c68730205020005310162"
        startToMsgType.put("682c2c687302050200053101", TelenotMsgType.SYS_INT_ARMED);

        // info message "sytem disarmed" ;"682c2c687302050200053001e1"
        startToMsgType.put("682c2c687302050200053001", TelenotMsgType.SYS_DISARMED);

        // info message "system intrusion detection" ;"682c2c68730205020100100123"
        // info message "system intrusion cleared" ;"682c2c687302050201001001a3"
        startToMsgType.put("682c2c687302050201001001", TelenotMsgType.INTRUSION);

        // System detailss

        // info message "battery malfunction" ;"681a1a68730205020000140133"
        // info message "battery malfunction cleared" ;"681a1a687302050200001401b3"
        startToMsgType.put("681a1a687302050200001401", TelenotMsgType.BATTERY_MALFUNCTION);

        // info message "power outage" -> Stromausfall ;"681a1a68730205020000150132"
        // info message "power outage cleared" ;"681a1a687302050200001501b2"
        startToMsgType.put("681a1a687302050200001501", TelenotMsgType.POWER_OUTAGE);

        // info message "optical flasher malfunction" ;"681a1a68730205020000130130"
        // info message "optical flasher malfunction cleared" ";681a1a687302050200001301b0"
        startToMsgType.put("681a1a687302050200001301", TelenotMsgType.OPTICAL_FLASHER_MALFUNCTION);

        // info message "acoustic alarm horn 1 malfuntion" ;"681a1a68730205020000110130"
        // info message "acoustic alarm horn 1 malfuntion cleared" ;"681a1a687302050200001101b0"
        startToMsgType.put("681a1a687302050200001101", TelenotMsgType.HORN_1_MALFUNCTION);

        // info message "acoustic alarm horn 2 malfuntion" ;"681a1a68730205020000120130"
        // info message "acoustic alarm horn 2 malfuntion cleared" ;"681a1a687302050200001201b0"
        startToMsgType.put("681a1a687302050200001201", TelenotMsgType.HORN_2_MALFUNCTION);

        // unknown signal ;"681a1a687302050200ffff0153"
        // unknown signal ;"683c3c687302050200053a0161"
        // unknown signal ;"681a1a68730205020000170134"
        // unknown signal ;"681a1a687302050200001701b4"
    }

    /**
     * Extract message type from message. Relies on static map startToMsgType.
     *
     * @param s message string
     * @return message type
     */
    public static TelenotMsgType getMsgType(@Nullable String s) {
        TelenotMsgType mt = null;
        if (s == null || s.length() < 4) {
            return TelenotMsgType.INVALID;
        }
        if (s.length() == 16) {
            mt = startToMsgType.get(s.substring(0, 16));
        } else if (s.length() == 17) {
            mt = startToMsgType.get(s.substring(0, 17));
        } else if (s.length() > 16) {
            mt = startToMsgType.get(s.substring(0, 24));
        } else {
            mt = startToMsgType.get(s.substring(0, 4));
        }

        if (mt == null) {
            mt = startToMsgType.get(s.substring(0, 16));
        }

        if (mt == null) {
            String regEX = "^682c2c6873020502\\w\\w\\w\\w\\w\\w01(22|a2)(.*)$";
            if (s.matches(regEX)) {
                mt = TelenotMsgType.ALARM;
            }
        }

        if (mt == null) {
            mt = TelenotMsgType.INVALID;
        }

        return mt;
    }
}
