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

//@formatter:off
// import statc org.openhab.binding.telenot.internal.TelenotBindingConstants.*;
//@formatter:on

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.telenot.internal.TelenotDiscoveryService;
import org.openhab.binding.telenot.internal.actions.BridgeActions;
import org.openhab.binding.telenot.internal.protocol.EMAStateMessage;
import org.openhab.binding.telenot.internal.protocol.MPMessage;
import org.openhab.binding.telenot.internal.protocol.SBMessage;
import org.openhab.binding.telenot.internal.protocol.TelenotCommand;
import org.openhab.binding.telenot.internal.protocol.TelenotMessage;
import org.openhab.binding.telenot.internal.protocol.TelenotMsgType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for bridge handlers responsible for communicating with
 * the Telenot devices.
 *
 * @author Ronny Grun - Initial contribution
 * 
 */
@NonNullByDefault
public abstract class TelenotBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(TelenotBridgeHandler.class);

    protected @Nullable ByteArrayOutputStream baos = null;
    protected @Nullable InputStream inputStream = null;
    protected @Nullable OutputStream outputStream = null;
    protected @Nullable BufferedReader reader = null;
    protected @Nullable InputStreamReader ireader = null;
    protected @Nullable BufferedWriter writer = null;
    protected @Nullable Thread msgReaderThread = null;
    private final Object msgReaderThreadLock = new Object();
    protected @Nullable TelenotDiscoveryService discoveryService;
    protected boolean discovery;
    protected volatile @Nullable Date lastReceivedTime;
    protected volatile boolean writeException;

    protected @Nullable ScheduledFuture<?> connectionCheckJob;
    protected @Nullable ScheduledFuture<?> connectRetryJob;

    public TelenotBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void dispose() {
        logger.trace("dispose called");
        disconnect();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(BridgeActions.class);
    }

    public void setDiscoveryService(TelenotDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Accepts no commands, so do nothing.
    }

    /**
     * Send a command to Telenot.
     *
     * @param command Command string to send including terminator
     */
    public void sendTelenotCommand(TelenotCommand command) {
        logger.debug("Sending Telenot command: {}", command);
        try {
            OutputStream bw = outputStream;
            if (bw != null) {
                bw.write(hexStringToByteArray(command.toString()));
            }
        } catch (IOException e) {
            logger.info("Exception while sending command: {}", e.getMessage());
            writeException = true;
        }
    }

    protected abstract void connect();

    protected abstract void disconnect();

    protected void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling connection retry in {} minutes", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
    }

    protected void startMsgReader() {
        synchronized (msgReaderThreadLock) {
            Thread mrt = new Thread(this::readerThread, "OH-binding-" + getThing().getUID() + "-TelenotReader");
            mrt.setDaemon(true);
            mrt.start();
            msgReaderThread = mrt;
        }
    }

    protected void stopMsgReader() {
        synchronized (msgReaderThreadLock) {
            Thread mrt = msgReaderThread;
            if (mrt != null) {
                logger.trace("Stopping reader thread.");
                mrt.interrupt();
                msgReaderThread = null;
            }
        }
    }

    /**
     * Method executed by message reader thread
     */
    private void readerThread() {
        logger.debug("Message reader thread started");
        String message = null;
        try {
            // read from the stream
            baos = new ByteArrayOutputStream();
            byte[] content = new byte[2048];
            int bytesRead = -1;

            while (!Thread.interrupted() && inputStream != null && (bytesRead = inputStream.read(content)) != -1) {
                baos.reset();
                baos.write(content, 0, bytesRead);

                message = toHexString(baos.toByteArray());

                TelenotMsgType msgType = TelenotMsgType.getMsgType(message);
                if (msgType != TelenotMsgType.INVALID) {
                    lastReceivedTime = new Date();
                }

                try {
                    switch (msgType) {
                        case ACK:
                            logger.debug("Received ACK message");
                            sendTelenotCommand(TelenotCommand.confirmACK());
                            break;
                        case CONF_ACK:
                            logger.debug("Received Confirm-ACK message");
                            sendTelenotCommand(TelenotCommand.confirmACK());
                            break;
                        case MP:
                            logger.debug("Received MP message");
                            parseMpMessage(msgType, message);
                            sendTelenotCommand(TelenotCommand.confirmACK());
                            break;
                        case SB:
                            logger.debug("Received SB message");
                            parseSbMessage(msgType, message);
                            sendTelenotCommand(TelenotCommand.confirmACK());
                            break;
                        case SYS_INT_ARMED:
                        case SYS_EXT_ARMED:
                        case SYS_DISARMED:
                        case INTRUSION:
                        case BATTERY_MALFUNCTION:
                        case POWER_OUTAGE:
                        case OPTICAL_FLASHER_MALFUNCTION:
                        case HORN_1_MALFUNCTION:
                        case HORN_2_MALFUNCTION:
                            logger.debug("Received {} message", msgType);
                            parseEmaStateMessage(msgType, message);
                            sendTelenotCommand(TelenotCommand.confirmACK());
                            sendTelenotCommand(TelenotCommand.sendACK());
                            break;
                        case INVALID:
                            logger.warn("INVALID MsgType: {} hexString: {}", msgType, message);
                            sendTelenotCommand(TelenotCommand.confirmACK());
                            sendTelenotCommand(TelenotCommand.sendACK());
                            break;
                        default:
                            break;
                    }
                } catch (MessageParseException e) {
                    logger.warn("Error {} while parsing message {}. Please report bug.", e.getMessage(), message);
                }
            }

            if (message == null) {
                logger.info("End of input stream detected");
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
            }
        } catch (IOException e) {
            logger.debug("I/O error while reading from stream: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Runtime exception in reader thread", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } finally {
            logger.debug("Message reader thread exiting");
        }
    }

    /**
     * Parse and handle MP messages. The MP messages have
     * identical format.
     *
     * @param mt message type of incoming message
     * @param msg string containing incoming message payload
     * @throws MessageParseException
     */
    private void parseMpMessage(TelenotMsgType mt, String msg) throws MessageParseException {
        // mt is unused at the moment
        MPMessage mpMsg;
        StringBuilder sb = new StringBuilder();

        // msg = msg.substring(24, msg.length());
        logger.trace("MP msg: {}", msg);
        msg = msg.substring(24, msg.length() < 84 ? msg.length() : 84);

        String msgReverseBinaryArray[] = hexStringToReverseBinaryArray(msg);
        int addr = 0;
        for (int i = 0; i < msgReverseBinaryArray.length; i++) {
            String d = msgReverseBinaryArray[i];
            for (int a = 1; a <= 8; a++) {
                sb.append(addr);
                sb.append(",");
                sb.append(d.substring(a - 1, a));
                try {
                    mpMsg = new MPMessage(sb.toString());
                } catch (IllegalArgumentException e) {
                    throw new MessageParseException(e.getMessage());
                }

                notifyChildHandlers(mpMsg);
                TelenotDiscoveryService ds = discoveryService;
                if (discovery && ds != null) {
                    ds.processMP(mpMsg.address);
                }
                sb.setLength(0);
                addr++;
            }
        }
    }

    /**
     * Parse and handle SB messages. The SB messages have
     * identical format.
     *
     * @param mt message type of incoming message
     * @param msg string containing incoming message payload
     * @throws MessageParseException
     */
    private void parseSbMessage(TelenotMsgType mt, String msg) throws MessageParseException {
        // mt is unused at the moment
        SBMessage sbMsg;
        StringBuilder strBuilder = new StringBuilder();

        logger.trace("SB msg: {}", msg);

        if (msg.length() < 33) {
            throw new MessageParseException("wrong SB msg length");
        }
        String msgSb = msg.substring(36, msg.length() < 52 ? msg.length() : 52);

        String msgReverseBinaryArraySb[] = hexStringToReverseBinaryArray(msgSb);
        int addr = 1;
        for (int i = 0; i < msgReverseBinaryArraySb.length; i++) {
            String d = msgReverseBinaryArraySb[i];
            strBuilder.append(addr);
            for (int a = 1; a <= 8; a++) {
                strBuilder.append(",");
                strBuilder.append(d.substring(a - 1, a));
            }
            try {
                sbMsg = new SBMessage(strBuilder.toString());
            } catch (IllegalArgumentException e) {
                throw new MessageParseException(e.getMessage());
            }

            notifyChildHandlers(sbMsg);
            strBuilder.setLength(0);
            addr++;
        }
    }

    /**
     * Parse and handle EMA State messages. The SB messages have
     * identical format.
     *
     * @param mt message type of incoming message
     * @param msg string containing incoming message payload
     * @throws MessageParseException
     */
    private void parseEmaStateMessage(TelenotMsgType mt, String msg) throws MessageParseException {
        EMAStateMessage emaMessage;
        StringBuilder sb = new StringBuilder();
        sb.append(mt);
        sb.append(":");
        sb.append(msg);

        try {
            emaMessage = new EMAStateMessage(sb.toString());
        } catch (IllegalArgumentException e) {
            throw new MessageParseException(e.getMessage());
        }
        notifyChildHandlers(emaMessage);
    }

    /**
     * Notify appropriate child thing handlers of an Telenot message by calling their handleUpdate() methods.
     *
     * @param msg message to forward to child handler(s)
     */
    private void notifyChildHandlers(TelenotMessage msg) {
        for (Thing thing : getThing().getThings()) {
            TelenotThingHandler handler = (TelenotThingHandler) thing.getHandler();
            //@formatter:off
            if (handler != null && ((handler instanceof SBHandler && msg instanceof SBMessage) ||
                                    (handler instanceof MPHandler && msg instanceof MPMessage) ||
                                    (handler instanceof EMAStateHandler && msg instanceof EMAStateMessage))) {
                handler.handleUpdate(msg);
            }
            //@formatter:on
        }
    }

    /**
     * Converts bytes into a hex string
     */
    public static String toHexString(byte @Nullable [] bytes) {
        StringBuilder sb = new StringBuilder();
        if (bytes != null)
            for (byte b : bytes) {
                final String hexString = Integer.toHexString(b & 0xff);

                if (hexString.length() == 1)
                    sb.append('0');

                sb.append(hexString);
            }
        return sb.toString();
    }

    /**
     * Converts hex string to binary array
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length() - 2;
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Converts hex string into an array
     */
    public static String[] hexStringToArray(String s) {
        int len = s.length();
        String[] data = new String[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = s.substring(i, i + 2);
        }
        return data;
    }

    /**
     * Converts a hex string into a reversed binary array
     */
    public static String[] hexStringToReverseBinaryArray(String s) {
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        int dlen;
        String[] data = new String[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = hexToBinary(s.substring(i, i + 2));
            dlen = data[i / 2].length();
            if (dlen <= 7) {
                for (int a = dlen; a < 8; a++) {
                    sb.append('0');
                }
                sb.append(data[i / 2]);
            } else {
                sb.append(data[i / 2]);
            }
            data[i / 2] = sb.reverse().toString();
            sb.setLength(0);
        }
        return data;
    }

    /**
     * Converts hex into binary
     */
    public static String hexToBinary(String hex) {
        int i = Integer.parseInt(hex, 16);
        String bin = Integer.toBinaryString(i);
        return bin;
    }

    /**
     * Exception thrown by message parsing code when it encounters a malformed message
     */
    private static class MessageParseException extends Exception {
        private static final long serialVersionUID = 1L;

        public MessageParseException(@Nullable String msg) {
            super(msg);
        }
    }
}
