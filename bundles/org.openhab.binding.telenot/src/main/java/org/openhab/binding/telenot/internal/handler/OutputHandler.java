/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.telenot.internal.TelenotCommandException;
import org.openhab.binding.telenot.internal.protocol.MBDMessage;
import org.openhab.binding.telenot.internal.protocol.MBMessage;
import org.openhab.binding.telenot.internal.protocol.TelenotCommand;
import org.openhab.binding.telenot.internal.protocol.TelenotMessage;
import org.openhab.binding.telenot.internal.protocol.UsedMbMessage;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OutputHandler} is responsible for handling OutputHandler
 **
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class OutputHandler extends TelenotThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OutputHandler.class);
    private static final Pattern REGEX_DISABLE = Pattern.compile("0x([0-9A-Fa-f]){4}");

    public OutputHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        initDeviceState();
        logger.trace("Input handler finished initializing");
    }

    @Override
    public void initChannelState() {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String cuid = channelUID.getId();
        Matcher m = REGEX_DISABLE.matcher(cuid);
        if (m.matches()) {
            String addr = cuid.substring(2);
            if (command instanceof OnOffType) {
                if (command == OnOffType.OFF) {
                    logger.debug("Received command: ENABLE_REPORTING_POINT for address: {}", addr);
                    try {
                        sendCommand(TelenotCommand.disableHexReportingPoint(addr, 0));
                    } catch (TelenotCommandException e) {
                        logger.error("{}", e.getMessage());
                    }
                } else if (command == OnOffType.ON) {
                    logger.debug("Received command: DISABLE_REPORTING_POINT for address: {}", addr);
                    try {
                        sendCommand(TelenotCommand.disableHexReportingPoint(addr, 1));
                    } catch (TelenotCommandException e) {
                        logger.error("{}", e.getMessage());
                    }
                }
            }
        }
        logger.trace("CUID: {}", cuid);
        if (channelUID.getId().equals(GET_USED_STATE)) {
            if (command instanceof OnOffType) {
                if (command == OnOffType.ON) {
                    sendCommand(TelenotCommand.sendUsedState());
                }
            }
        }
    }

    @Override
    public void handleUpdateChannel(TelenotMessage msg) {
        if (!(msg instanceof UsedMbMessage)) {
            return;
        }
        UsedMbMessage mbMsg = (UsedMbMessage) msg;
        createChannel(mbMsg.address, mbMsg.name);
        logger.trace("handleUpdateChannel: {}", mbMsg.address);
    }

    @Override
    public void handleUpdate(TelenotMessage msg) {
        if (msg instanceof MBMessage) {
            MBMessage mbMsg = (MBMessage) msg;
            String hex = String.format("0x%04x", mbMsg.address + 1391);
            OpenClosedType state = (mbMsg.data == 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(hex, state);
        } else if (msg instanceof MBDMessage) {
            MBDMessage mbdMsg = (MBDMessage) msg;
            String hex = String.format("0x%04x", mbdMsg.address + 1519);
            OnOffType state = (mbdMsg.data == 0 ? OnOffType.ON : OnOffType.OFF);
            updateState(hex, state);
        } else {
            return;
        }
    }

    /**
     * Creates new channels for the thing.
     *
     * @param channelId ID of the channel to be created.
     */
    private void createChannel(String channelId, @Nullable String label) {
        if (label == null) {
            label = "Contact " + channelId;
        }
        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            logger.warn("Thing '{}'not initialized, could not get callback.", thing.getUID());
            return;
        }

        ChannelUID contactChannelUID = new ChannelUID(thing.getUID(), channelId);
        Channel contactChannel = callback.createChannelBuilder(contactChannelUID, CHANNEL_TYPE_CONTACT).withLabel(label)
                .build();

        String hex = String.format("0x%04x", Integer.parseInt(channelId.substring(2), 16) + 128);
        ChannelUID switchChannelUID = new ChannelUID(thing.getUID(), hex);
        Channel switchChannel = callback.createChannelBuilder(switchChannelUID, CHANNEL_TYPE_SWITCH)
                .withLabel("Disable " + label).build();
        updateThing(editThing().withoutChannel(contactChannelUID).withChannel(contactChannel)
                .withoutChannel(switchChannelUID).withChannel(switchChannel).build());
    }
}
