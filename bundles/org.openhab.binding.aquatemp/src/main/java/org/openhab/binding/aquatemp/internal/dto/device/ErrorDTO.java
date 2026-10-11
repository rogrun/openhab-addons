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

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ErrorDTO} provides ErrorDTO
 *
 * @author Ronny Grun - Initial contribution
 */
public class ErrorDTO {

    @SerializedName("error_code")
    public String errorCode;
    @SerializedName("error_msg")
    public String errorMsg;
    @SerializedName("error_msg_code")
    public String errorMsgCode;
    @SerializedName(value = "total_size", alternate = "totalSize")
    public Integer totalSize;
    @SerializedName(value = "total_page", alternate = "totalPage")
    public Integer totalPage;
    @SerializedName(value = "next_page", alternate = "nextPage")
    public Boolean nextPage;
    @SerializedName(value = "object_result", alternate = "objectResult")
    public List<ErrorObjectResult> objectResult;
    @SerializedName(value = "is_reuslt_suc", alternate = "isReusltSuc")
    public Boolean isReusltSuc;
}
