/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.aquatemp.internal.dto.device;

/**
 * The {@link ParamDTO} provides ParamDTO
 *
 * @author Ronny Grun - Initial contribution
 */
public class ParamDTO {

    public String device_code;
    public String protocol_code;
    public String value;

    public ParamDTO(String deviceCode, String protocolCode, String value) {
        this.device_code = deviceCode;
        this.protocol_code = protocolCode;
        this.value = value;
    }
}
