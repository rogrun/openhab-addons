/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.telenot.internal;

import static org.openhab.binding.telenot.internal.TelenotBindingConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.telenot.internal.handler.MPHandler;
import org.openhab.binding.telenot.internal.handler.TelenotBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TelenotDiscoveryService} handles discovery of devices as they are identified by the bridge handler.
 * Requests from the framework to startScan() are ignored, since no active scanning is possible.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class TelenotDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(TelenotDiscoveryService.class);

    private TelenotBridgeHandler bridgeHandler;
    private final Set<String> discoveredMPSet = new HashSet<>();

    public TelenotDiscoveryService(TelenotBridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(DISCOVERABLE_DEVICE_TYPE_UIDS, 0, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        // Ignore start scan requests
    }

    public void processMP(int address) {
        String token = MPHandler.mpID(address);
        if (!discoveredMPSet.contains(token)) {
            notifyDiscoveryOfMP(address, token);
            discoveredMPSet.add(token);
        }
    }

    private void notifyDiscoveryOfMP(int address, String idString) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(THING_TYPE_MP, bridgeUID, idString);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_ADDRESS, address);
        properties.put(PROPERTY_ID, idString);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(properties)
                .withRepresentationProperty(PROPERTY_ID).build();
        thingDiscovered(result);
        logger.debug("Discovered MP {}", uid);
    }
}
