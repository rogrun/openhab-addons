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
package org.openhab.binding.aquatemp.internal.dto.device;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ErrorObjectResult} provides ErrorObjectResult
 *
 * @author Ronny Grun - Initial contribution
 */
public class ErrorObjectResult {

    @SerializedName("data_id")
    private String dataId;
    @SerializedName("device_code")
    private String deviceCode;
    @SerializedName("device_nick_name")
    private String deviceNickName;
    @SerializedName("protocal_id")
    private String protocalId;
    @SerializedName("address")
    private String address;
    @SerializedName("create_time")
    private String createTime;
    @SerializedName("data_value")
    private String dataValue;
    @SerializedName("fault_code")
    private String faultCode;
    @SerializedName("description")
    private String description;
    @SerializedName("falut_detail_id")
    private String falutDetailId;
    @SerializedName("error_level")
    private Integer errorLevel;

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getDeviceNickName() {
        return deviceNickName;
    }

    public void setDeviceNickName(String deviceNickName) {
        this.deviceNickName = deviceNickName;
    }

    public String getProtocalId() {
        return protocalId;
    }

    public void setProtocalId(String protocalId) {
        this.protocalId = protocalId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFalutDetailId() {
        return falutDetailId;
    }

    public void setFalutDetailId(String falutDetailId) {
        this.falutDetailId = falutDetailId;
    }

    public Integer getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(Integer errorLevel) {
        this.errorLevel = errorLevel;
    }
}
