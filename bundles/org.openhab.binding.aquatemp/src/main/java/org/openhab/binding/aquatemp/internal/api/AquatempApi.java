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
package org.openhab.binding.aquatemp.internal.api;

import static org.openhab.binding.aquatemp.internal.AquatempBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.aquatemp.internal.dto.device.DataDTO;
import org.openhab.binding.aquatemp.internal.dto.device.DeviceDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ErrorDTO;
import org.openhab.binding.aquatemp.internal.dto.oauth.TokenResponseDTO;
import org.openhab.binding.aquatemp.internal.handler.AquatempBridgeHandler;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link AquatempApi} is responsible for managing all communication with
 * the Aquatemp API service.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class AquatempApi {

    private static final String HTTP_METHOD_POST = "POST";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    private final Logger logger = LoggerFactory.getLogger(AquatempApi.class);

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private static final int TOKEN_MIN_DIFF_MS = 120000;

    public final Properties httpHeaders;

    private final AquatempBridgeHandler bridgeHandler;
    private final HttpClient httpClient;

    private final String user;
    private final String password;

    private long tokenExpiryDate;
    private long refreshTokenExpiryDate;

    private @NonNullByDefault({}) AquatempAuth aquatempAuth;

    public AquatempApi(final AquatempBridgeHandler bridgeHandler, HttpClient httpClient, String user, String password) {
        this.bridgeHandler = bridgeHandler;
        this.httpClient = httpClient;
        this.user = user;
        this.password = password;
        tokenResponse = null;
        httpHeaders = new Properties();
        httpHeaders.put("User-Agent", "openhab-aquatemp-api/2.0");

        createAuthClientService();
        authorize();
    }

    public Gson getGson() {
        return GSON;
    }

    private @Nullable TokenResponseDTO tokenResponse;

    public void setTokenResponseDTO(TokenResponseDTO newTokenResponse) {
        tokenResponse = newTokenResponse;
    }

    public @Nullable TokenResponseDTO getTokenResponseDTO() throws AquatempAuthException {
        return tokenResponse;
    }

    private @Nullable String xToken;

    public void setToken(String xToken) {
        logger.debug("Set xToken from {} to {}", this.xToken, xToken);
        this.xToken = xToken;
    }

    public void setTokenExpiryDate(long expiresIn) {
        tokenExpiryDate = System.currentTimeMillis() + expiresIn;
    }

    public long getTokenExpiryDate() {
        return tokenExpiryDate;
    }

    public void setRefreshTokenExpiryDate(long expiresIn) {
        refreshTokenExpiryDate = System.currentTimeMillis() + expiresIn;
    }

    public long getRefreshTokenExpiryDate() {
        return refreshTokenExpiryDate;
    }

    public void createAuthClientService() {
        String bridgeUID = bridgeHandler.getThing().getUID().getAsString();
        logger.debug("API: Creating Auth Client Service for {}", bridgeUID);
        aquatempAuth = new AquatempAuth(this, bridgeHandler, httpClient, user, password);
    }

    /**
     * Check to see if the Aquatemp authorization process is complete. This will be determined
     * by requesting an AccessTokenResponse from the API. If we get a valid
     * response, then assume that the Aquatemp authorization process is complete. Otherwise,
     * start the Aquatemp authorization process.
     */
    private void authorize() {
        try {
            if (xToken != null) {
                long difference = getTokenExpiryDate() - System.currentTimeMillis();
                if (difference <= TOKEN_MIN_DIFF_MS) {
                    aquatempAuth.setState(AquatempAuthState.NEED_AUTH);
                } else {
                    aquatempAuth.setState(AquatempAuthState.COMPLETE);
                }
            }
            aquatempAuth.doAuthorization();
        } catch (AquatempAuthException e) {
            if (logger.isDebugEnabled()) {
                logger.info("API: The Aquatemp authorization process threw an exception", e);
            } else {
                logger.info("API: The Aquatemp authorization process threw an exception: {}", e.getMessage());
            }
            aquatempAuth.setState(AquatempAuthState.NEED_AUTH);
        }
    }

    public void checkExpiringToken() {
        logger.debug("Checking if new access token is needed...");
        try {
            long refreshTokenExpire = getTokenExpiryDate() - System.currentTimeMillis();
            if (refreshTokenExpire <= TOKEN_MIN_DIFF_MS) {
                aquatempAuth.setState(AquatempAuthState.NEED_AUTH);
                aquatempAuth.doAuthorization();
            }
        } catch (AquatempAuthException e) {
            if (logger.isDebugEnabled()) {
                logger.info("API: The Aquatemp authorization process threw an exception", e);
            } else {
                logger.info("API: The Aquatemp authorization process threw an exception: {}", e.getMessage());
            }
            aquatempAuth.setState(AquatempAuthState.NEED_AUTH);
        }
    }

    public @Nullable DeviceDTO getAllDevices() throws AquatempCommunicationException {
        String response = executePost(AQUATEMP_BASE_URL + "/app/device/deviceList.json", "");
        return GSON.fromJson(response, DeviceDTO.class);
    }

    public @Nullable ErrorDTO getErrorsByDevice(String deviceId) throws AquatempCommunicationException {
        String json = "{ \"device_code\": \"" + deviceId + "\" }";
        String response = executePost(AQUATEMP_BASE_URL + "/app/device/getFaultDataByDeviceCode.json", json);
        return GSON.fromJson(response, ErrorDTO.class);
    }

    public @Nullable DataDTO getDataByCode(String code) throws AquatempCommunicationException {
        String json = "{ \"device_code\": \"" + code
                + "\", \"protocal_codes\":[\"Power\",\"Mode\",\"Manual-mute\",\"T01\",\"T02\",\"2074\",\"2075\",\"2076\",\"2077\",\"H03\",\"Set_Temp\",\"R08\",\"R09\",\"R10\",\"R11\",\"R01\",\"R02\",\"R03\",\"T03\",\"1158\",\"1159\",\"F17\",\"H02\",\"T04\",\"T05\",\"T07\",\"T14\"] }";

        String response = executePost(AQUATEMP_BASE_URL + "/app/device/getDataByCode.json", json);
        if (response != null) {
            return GSON.fromJson(response, DataDTO.class);
        }
        return null;
    }

    public void controlDevice(String json) throws AquatempCommunicationException {
        executePost(AQUATEMP_BASE_URL + "/app/device/control.json", json);
    }

    private @Nullable String executePost(String url, String json) throws AquatempCommunicationException {
        String response = null;
        try {
            logger.trace("API: POST Request URL is '{}', JSON is '{}'", url, json);
            long startTime = System.currentTimeMillis();
            response = HttpUtil.executeUrl(HTTP_METHOD_POST, url, setHeaders(),
                    new ByteArrayInputStream(json.getBytes()), CONTENT_TYPE_APPLICATION_JSON, API_TIMEOUT_MS);
            logger.trace("API: Response took {} msec: {}", System.currentTimeMillis() - startTime, response);
        } catch (IOException e) {
            logger.info("API IOException: Unable to execute POST: {}", e.getMessage());
            authorize();
        } catch (AquatempAuthException e) {
            logger.info("API AuthException: Unable to execute POST: {}", e.getMessage());
            authorize();
        }
        return response;
    }

    private Properties setHeaders() throws AquatempAuthException {
        if (xToken == null) {
            throw new AquatempAuthException("Can not set auth header because access token is null");
        } else {
            String token = xToken;
            Properties headers = new Properties();
            headers.putAll(httpHeaders);
            headers.put("x-token", "" + token);
            return headers;
        }
    }
}
