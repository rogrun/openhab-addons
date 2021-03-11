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
package org.openhab.binding.telenot.internal.handler;

import static org.openhab.binding.telenot.internal.TelenotBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.telenot.internal.protocol.EMAStateMessage;
import org.openhab.binding.telenot.internal.protocol.TelenotMessage;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EMAStateHandler} is responsible for handling state of internally armed.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class EMAStateHandler extends TelenotThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EMAStateHandler.class);

    // private SBConfig config = new SBConfig();

    public EMAStateHandler(Thing thing) {
        super(thing);
    }

    /** Construct mp id from address */
    public static final String mpID(int address) {
        return String.format("%d", address);
    }

    @Override
    public void initialize() {
        // config = getConfigAs(SBConfig.class);

        // if (config.address < 0) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid address setting");
        // return;
        // }
        // logger.debug("SB handler initializing for address {}", config.address);

        // String id = mpID(config.address);
        // updateProperty(PROPERTY_ID, id); // set representation property used by discovery

        initDeviceState();
        logger.trace("emaState handler finished initializing");
    }

    /**
     * Set contact channel state to "UNDEF" at init time. The real state will be set either when the first message
     * arrives for the zone, or it should be set to "CLOSED" the first time the panel goes into the "READY" state.
     */
    @Override
    public void initChannelState() {
        UnDefType state = UnDefType.UNDEF;
        updateState(CHANNEL_INTRUSION_DATETIME, state);
        updateState(CHANNEL_BATTERY_MALFUNCTION_DATETIME, state);
        updateState(CHANNEL_POWER_OUTAGE_DATETIME, state);
        updateState(CHANNEL_OPTICAL_FLASHER_MALFUNCTION_DATETIME, state);
        updateState(CHANNEL_HORN_1_MALFUNCTION_DATETIME, state);
        updateState(CHANNEL_HORN_2_MALFUNCTION_DATETIME, state);

        updateState(CHANNEL_INTRUSION_CONTACT, state);
        updateState(CHANNEL_BATTERY_MALFUNCTION_CONTACT, state);
        updateState(CHANNEL_POWER_OUTAGE_CONTACT, state);
        updateState(CHANNEL_OPTICAL_FLASHER_MALFUNCTION_CONTACT, state);
        updateState(CHANNEL_HORN_1_MALFUNCTION_CONTACT, state);
        updateState(CHANNEL_HORN_2_MALFUNCTION_CONTACT, state);

        updateState(CHANNEL_INTRUSION_SET_CLEAR, state);
        updateState(CHANNEL_BATTERY_MALFUNCTION_SET_CLEAR, state);
        updateState(CHANNEL_POWER_OUTAGE_SET_CLEAR, state);
        updateState(CHANNEL_OPTICAL_FLASHER_MALFUNCTION_SET_CLEAR, state);
        updateState(CHANNEL_HORN_1_MALFUNCTION_SET_CLEAR, state);
        updateState(CHANNEL_HORN_2_MALFUNCTION_SET_CLEAR, state);

        firstUpdateReceived.set(false);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // All channels are read-only, so ignore all commands.
    }

    @Override
    public void handleUpdate(TelenotMessage msg) {
        if (!(msg instanceof EMAStateMessage)) {
            return;
        }
        EMAStateMessage emaMsg = (EMAStateMessage) msg;

        logger.trace("emaState handler for received update: {},{}", emaMsg.date, emaMsg.contact);

        firstUpdateReceived.set(true);

        switch (emaMsg.messagetype) {
            case "INTRUSION":
                updateState(CHANNEL_INTRUSION_DATETIME, emaMsg.date);
                updateState(CHANNEL_INTRUSION_CONTACT, new StringType(emaMsg.contact));
                updateState(CHANNEL_INTRUSION_SET_CLEAR, emaMsg.alarmSetClear ? OnOffType.ON : OnOffType.OFF);
                break;
            case "BATTERY_MALFUNCTION":
                updateState(CHANNEL_BATTERY_MALFUNCTION_DATETIME, emaMsg.date);
                updateState(CHANNEL_BATTERY_MALFUNCTION_CONTACT, new StringType(emaMsg.contact));
                updateState(CHANNEL_BATTERY_MALFUNCTION_SET_CLEAR, emaMsg.alarmSetClear ? OnOffType.ON : OnOffType.OFF);
                break;
            case "POWER_OUTAGE":
                updateState(CHANNEL_POWER_OUTAGE_DATETIME, emaMsg.date);
                updateState(CHANNEL_POWER_OUTAGE_CONTACT, new StringType(emaMsg.contact));
                updateState(CHANNEL_POWER_OUTAGE_SET_CLEAR, emaMsg.alarmSetClear ? OnOffType.ON : OnOffType.OFF);
                break;
            case "OPTICAL_FLASHER_MALFUNCTION":
                updateState(CHANNEL_OPTICAL_FLASHER_MALFUNCTION_DATETIME, emaMsg.date);
                updateState(CHANNEL_OPTICAL_FLASHER_MALFUNCTION_CONTACT, new StringType(emaMsg.contact));
                updateState(CHANNEL_OPTICAL_FLASHER_MALFUNCTION_SET_CLEAR,
                        emaMsg.alarmSetClear ? OnOffType.ON : OnOffType.OFF);
                break;
            case "HORN_1_MALFUNCTION":
                updateState(CHANNEL_HORN_1_MALFUNCTION_DATETIME, emaMsg.date);
                updateState(CHANNEL_HORN_1_MALFUNCTION_CONTACT, new StringType(emaMsg.contact));
                updateState(CHANNEL_HORN_1_MALFUNCTION_SET_CLEAR, emaMsg.alarmSetClear ? OnOffType.ON : OnOffType.OFF);
                break;
            case "HORN_2_MALFUNCTION":
                updateState(CHANNEL_HORN_2_MALFUNCTION_DATETIME, emaMsg.date);
                updateState(CHANNEL_HORN_2_MALFUNCTION_CONTACT, new StringType(emaMsg.contact));
                updateState(CHANNEL_HORN_2_MALFUNCTION_SET_CLEAR, emaMsg.alarmSetClear ? OnOffType.ON : OnOffType.OFF);
                break;
            default:
                break;
        }
    }
}
