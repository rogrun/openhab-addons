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
 * The {@link ObjectResult} provides ObjectResult
 *
 * @author Ronny Grun - Initial contribution
 */
public class ObjectResult {

    @SerializedName("role_name")
    public String roleName;
    @SerializedName("user_type")
    public String userType;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("user_name")
    public String userName;
    @SerializedName("nick_name")
    public String nickName;
    @SerializedName("x-token")
    public String xToken;
    @SerializedName("real_name")
    public Object realName;
}
