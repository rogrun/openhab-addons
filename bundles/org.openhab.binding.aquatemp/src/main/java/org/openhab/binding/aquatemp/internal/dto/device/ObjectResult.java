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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ObjectResult} provides ObjectResult
 *
 * @author Ronny Grun - Initial contribution
 */
public class ObjectResult {

    @SerializedName(value = "device_status", alternate = { "deviceStatus", "status" })
    public String deviceStatus;
    @SerializedName(value = "device_name", alternate = "deviceName")
    public String deviceName;
    @SerializedName(value = "is_fault", alternate = "isFault")
    public Boolean isFault;
    @SerializedName(value = "device_id", alternate = "deviceId")
    public String deviceId;
    @SerializedName("device_code")
    public String deviceCode;
    @SerializedName(value = "product_id", alternate = "productId")
    public String productId;
    @SerializedName("device_type")
    public Object deviceType;
    @SerializedName("device_nick_name")
    public String deviceNickName;

    public String getIdentifier() {
        return deviceCode == null || deviceCode.isBlank() ? deviceId : deviceCode;
    }

    public String getDisplayName() {
        return deviceNickName == null || deviceNickName.isBlank() ? deviceName : deviceNickName;
    }
}
