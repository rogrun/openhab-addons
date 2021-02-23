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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TelenotCommand} class represents an Telenot command, and contains the static methods and definitions
 * used to construct one. Not all supported Telenot commands are necessarily used by the current binding.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class TelenotCommand {

    private static final String TERM = "\r\n";

    private static final String COMMAND_REBOOT = "=";
    private static final String COMMAND_ACK = "6802026840024216";
    private static final String COMMAND_CONF_ACK = "6802026800020216";

    public final String command;

    public TelenotCommand(String command) {
        this.command = command + TERM;
    }

    @Override
    public String toString() {
        return command;
    }

    public static TelenotCommand reboot() {
        return new TelenotCommand(COMMAND_REBOOT);
    }

    /**
     * Construct an Telenot command to acknowledge that a received CRC message was valid.
     *
     * @return TelenotCommand object containing the constructed command
     */
    public static TelenotCommand confirmACK() {
        return new TelenotCommand(COMMAND_CONF_ACK);
    }

    /**
     * Construct an Telenot command to acknowledge that a received CRC message was valid.
     *
     * @return TelenotCommand object containing the constructed command
     */
    public static TelenotCommand sendACK() {
        return new TelenotCommand(COMMAND_ACK);
    }
}
