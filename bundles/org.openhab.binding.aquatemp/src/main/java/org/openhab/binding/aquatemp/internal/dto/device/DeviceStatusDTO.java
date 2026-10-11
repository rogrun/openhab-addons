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
 * The {@link DeviceStatusDTO} provides the device status returned by the Go Heating API.
 *
 * @author Ronny Grun - Initial contribution
 */
public class DeviceStatusDTO {

    @SerializedName("error_code")
    public String errorCode;
    @SerializedName("error_msg")
    public String errorMsg;
    @SerializedName(value = "object_result", alternate = "objectResult")
    public ObjectResult objectResult;
    @SerializedName(value = "is_reuslt_suc", alternate = "isReusltSuc")
    public Boolean isReusltSuc;
}
