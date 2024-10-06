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
package org.openhab.binding.aquatemp.internal.dto.oauth;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthorizeResponseDTO} provides AuthorizeResponseDTO
 *
 * @author Ronny Grun - Initial contribution
 */
public class AuthorizeResponseDTO {

    @SerializedName("error_code")
    public String errorCode;
    @SerializedName("error_msg")
    public String errorMsg;
    @SerializedName("error_msg_code")
    public String errorMsgCode;
    @SerializedName("object_result")
    public ObjectResult objectResult;
    @SerializedName("is_reuslt_suc")
    public Boolean isReusltSuc;
}
