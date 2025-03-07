/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * The {@link MBDMessage} class represents a parsed MPD message.
 * *
 * 
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class MBDMessage extends TelenotMessage {

    /** Address number */
    public final int address;

    /** Message data */
    public final int data;

    public MBDMessage(String message) throws IllegalArgumentException {
        super(message);

        String parts[] = message.split(",");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid number of parts in MBD message");
        }

        try {
            address = Integer.parseInt(parts[0]);
            data = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("MBD message contains invalid number: " + e.getMessage());
        }

        if ((data & ~0x1) != 0) {
            throw new IllegalArgumentException("MBD status should only be 0 or 1");
        }
    }
}
