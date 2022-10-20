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
package org.openhab.binding.telenot.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MBMessage} class represents a parsed MP message.
 * *
 * 
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class MBMessage extends TelenotMessage {

    /** Address number */
    public final int address;

    /** Message data */
    public final int data;

    public MBMessage(String message) throws IllegalArgumentException {
        super(message);

        String parts[] = message.split(",");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid number of parts in MB message");
        }

        try {
            address = Integer.parseInt(parts[0]);
            data = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("MB message contains invalid number: " + e.getMessage());
        }

        if ((data & ~0x1) != 0) {
            throw new IllegalArgumentException("MB status should only be 0 or 1");
        }
    }
}
