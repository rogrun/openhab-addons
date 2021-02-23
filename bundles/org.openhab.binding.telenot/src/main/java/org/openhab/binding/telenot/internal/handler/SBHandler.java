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
import org.openhab.binding.telenot.internal.config.SBConfig;
import org.openhab.binding.telenot.internal.protocol.SBMessage;
import org.openhab.binding.telenot.internal.protocol.TelenotMessage;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SBHandler} is responsible for handling state of SB.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class SBHandler extends TelenotThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SBHandler.class);

    private SBConfig config = new SBConfig();

    public SBHandler(Thing thing) {
        super(thing);
    }

    /** Construct MP id from address */
    public static final String mpID(int address) {
        return String.format("%d", address);
    }

    @Override
    public void initialize() {
        config = getConfigAs(SBConfig.class);

        if (config.address < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid address setting");
            return;
        }
        logger.debug("SB handler initializing for address {}", config.address);

        String id = mpID(config.address);
        updateProperty(PROPERTY_ID, id); // set representation property used by discovery

        initDeviceState();
        logger.trace("SB handler finished initializing");
    }

    /**
     * Set contact channel state to "UNDEF" at init time. The real state will be set either when the first message
     * arrives for the zone, or it should be set to "CLOSED" the first time the panel goes into the "READY" state.
     */
    @Override
    public void initChannelState() {
        UnDefType state = UnDefType.UNDEF;
        updateState(CHANNEL_DISARMED, state);
        updateState(CHANNEL_INTERNALLY_ARMED, state);
        updateState(CHANNEL_EXTERNALLY_ARMED, state);
        updateState(CHANNEL_ALARM, state);
        updateState(CHANNEL_MALFUNCTION, state);
        updateState(CHANNEL_READY_TO_ARM_INTERNALLY, state);
        updateState(CHANNEL_READY_TO_ARM_EXTERNALLY, state);
        updateState(CHANNEL_STATE_INTERNAL_SIGNAL_HORN, state);

        firstUpdateReceived.set(false);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // All channels are read-only, so ignore all commands.
    }

    @Override
    public void handleUpdate(TelenotMessage msg) {
        if (!(msg instanceof SBMessage)) {
            return;
        }
        SBMessage sbMsg = (SBMessage) msg;

        if (config.address == sbMsg.address) {
            logger.trace("SB handler for {} received update: {},{},{},{},{},{},{},{}", config.address, sbMsg.disarmed,
                    sbMsg.internallyArmed, sbMsg.externallyArmed, sbMsg.alarm, sbMsg.malfuntion,
                    sbMsg.readyToArmInternally, sbMsg.readyToArmExternally, sbMsg.statusInternalSignalHorn);

            firstUpdateReceived.set(true);

            updateState(CHANNEL_DISARMED, sbMsg.disarmed == 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(CHANNEL_INTERNALLY_ARMED,
                    sbMsg.internallyArmed == 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(CHANNEL_EXTERNALLY_ARMED,
                    sbMsg.externallyArmed == 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(CHANNEL_ALARM, sbMsg.alarm == 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(CHANNEL_MALFUNCTION, sbMsg.malfuntion == 0 ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_READY_TO_ARM_INTERNALLY,
                    sbMsg.readyToArmInternally == 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(CHANNEL_READY_TO_ARM_EXTERNALLY,
                    sbMsg.readyToArmExternally == 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(CHANNEL_STATE_INTERNAL_SIGNAL_HORN,
                    sbMsg.statusInternalSignalHorn == 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        }
    }
}
