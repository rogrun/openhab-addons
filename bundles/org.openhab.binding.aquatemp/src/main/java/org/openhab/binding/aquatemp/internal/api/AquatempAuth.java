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

import java.net.CookieStore;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.aquatemp.internal.dto.oauth.AuthorizeResponseDTO;
import org.openhab.binding.aquatemp.internal.handler.AquatempBridgeHandler;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link AquatempAuth} performs the initial OAuth authorization
 * with the Aquatemp authorization servers.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class AquatempAuth {

    private final Logger logger = LoggerFactory.getLogger(AquatempAuth.class);

    private final AquatempBridgeHandler bridgeHandler;
    private final AquatempApi api;
    private final String user;
    private final String password;

    private final HttpClient httpClient;

    private AquatempAuthState state;

    private @Nullable AuthorizeResponseDTO authResponse;
    public @Nullable String accessToken;

    private @Nullable String refreshToken;

    public AquatempAuth(AquatempApi api, AquatempBridgeHandler bridgeHandler, HttpClient httpClient, String user,
            String password) {
        this.api = api;
        this.httpClient = httpClient;
        this.bridgeHandler = bridgeHandler;
        this.user = user;
        this.password = password;
        state = AquatempAuthState.NEED_AUTH;
        authResponse = null;
    }

    public void setState(AquatempAuthState newState) {
        if (newState != state) {
            logger.debug("AquatempAuth: Change state from {} to {}", state, newState);
            state = newState;
        }
    }

    public void setRefreshToken(String newRefreshToken) {
        if (!newRefreshToken.equals(refreshToken)) {
            logger.debug("AquatempAuth: Change refreshToken from {} to {}", refreshToken, newRefreshToken);
            refreshToken = newRefreshToken;
        }
    }

    public boolean isComplete() {
        return state == AquatempAuthState.COMPLETE;
    }

    public AquatempAuthState doAuthorization() throws AquatempAuthException {
        switch (state) {
            case NEED_AUTH:
                authorize();
                break;
            case COMPLETE:
                bridgeHandler.updateBridgeStatus(ThingStatus.ONLINE);
                break;
        }
        return state;
    }

    public @Nullable String getAccessToken() throws AquatempAuthException {
        return this.accessToken;
    }

    public void setAccessToken(String newAccessToken) {
        this.accessToken = newAccessToken;
    }

    /**
     * Call the Aquatemp authorize endpoint to get the authorization code.
     */
    private void authorize() throws AquatempAuthException {
        logger.debug("AquatempAuth: State is {}: Executing step: 'authorize'", state);
        logger.trace("AquatempAuth: Getting authorize URL={}", AQUATEMP_AUTHORIZE_URL);

        String response = executeUrlAuthorize(AQUATEMP_AUTHORIZE_URL);
        logger.trace("AquatempAuth: Auth response: {}", response);
        try {
            authResponse = api.getGson().fromJson(response, AuthorizeResponseDTO.class);
            if (authResponse == null) {
                logger.debug("AquatempAuth: Got null authorize response from Aquatemp API");
                setState(AquatempAuthState.NEED_AUTH);
            } else {
                AuthorizeResponseDTO resp = this.authResponse;
                if (resp == null) {
                    logger.warn("AuthorizeResponseDTO is null. This should not happen.");
                    return;
                }
                if ("Success".contains(resp.errorMsg)) {
                    api.setToken(resp.objectResult.xToken);
                    api.setTokenExpiryDate(TimeUnit.SECONDS.toMillis(3600));
                    setState(AquatempAuthState.COMPLETE);
                    bridgeHandler.updateBridgeStatus(ThingStatus.ONLINE);
                } else if ("Error username or password".contains(resp.errorMsg)) {
                    logger.debug("AquatempAuth: Error username or password");
                    updateBridgeStatusLogin();
                } else {
                    logger.debug("AquatempAuth: Got null authorize response from Aquatemp API");
                    setState(AquatempAuthState.NEED_AUTH);
                }
            }
        } catch (JsonSyntaxException e) {
            logger.info("AquatempAuth: Exception while parsing authorize response: {}", e.getMessage());
            setState(AquatempAuthState.NEED_AUTH);
        }
    }

    private void updateBridgeStatusLogin() {
        bridgeHandler.updateBridgeStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "Login fails. Please check user and password.");
    }

    private void updateBridgeStatusApiKey() {
        bridgeHandler.updateBridgeStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "Login fails. Please check API Key.");
    }

    private @Nullable String executeUrlAuthorize(String url) {
        StringBuilder json = new StringBuilder();
        json.append("{ \"user_name\": \"");
        json.append(user);
        json.append("\", \"password\": \"");
        json.append(password);
        json.append("\", \"type\": \"2\" }");

        Request request = httpClient.newRequest(url).timeout(API_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .method(HttpMethod.POST).header("Content-Type", "application/json")
                .content(new StringContentProvider(json.toString(), "utf-8"));
        CookieStore cookieStore = httpClient.getCookieStore();
        if (!cookieStore.get(AQUATEMP_URI).isEmpty()) {
            cookieStore.remove(AQUATEMP_URI, cookieStore.get(AQUATEMP_URI).get(0));
        }
        try {
            ContentResponse contentResponse = request.send();
            switch (contentResponse.getStatus()) {
                case HttpStatus.OK_200:
                    return contentResponse.getContentAsString();
                case HttpStatus.BAD_REQUEST_400:
                    logger.debug("BAD REQUEST(400) response received: {}", contentResponse.getContentAsString());
                    updateBridgeStatusApiKey();
                    return contentResponse.getContentAsString();
                case HttpStatus.UNAUTHORIZED_401:
                    logger.debug("UNAUTHORIZED(401) response received: {}", contentResponse.getContentAsString());
                    return contentResponse.getContentAsString();
                case HttpStatus.NO_CONTENT_204:
                    logger.debug("HTTP response 204: No content. Check configuration");
                    break;
                default:
                    logger.debug("HTTP GET failed: {}, {}", contentResponse.getStatus(), contentResponse.getReason());
                    break;
            }
        } catch (TimeoutException e) {
            logger.debug("TimeoutException: Call to Aquatemp API timed out");
        } catch (ExecutionException e) {
            logger.debug("ExecutionException on call to Aquatemp authorization API", e);
        } catch (InterruptedException e) {
            logger.debug("InterruptedException on call to Aquatemp authorization API: {}", e.getMessage());
        }
        return null;
    }
}
