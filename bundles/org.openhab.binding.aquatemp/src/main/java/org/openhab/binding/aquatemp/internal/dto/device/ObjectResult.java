/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

    @SerializedName("device_status")
    public String deviceStatus;
    @SerializedName("device_name")
    public Object deviceName;
    @SerializedName("is_fault")
    public Boolean isFault;
    @SerializedName("device_id")
    public String deviceId;
    @SerializedName("device_code")
    public String deviceCode;
    @SerializedName("product_id")
    public String productId;
    @SerializedName("device_type")
    public Object deviceType;
    @SerializedName("device_nick_name")
    public String deviceNickName;
}
