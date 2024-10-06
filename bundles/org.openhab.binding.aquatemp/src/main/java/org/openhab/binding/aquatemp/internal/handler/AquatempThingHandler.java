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
package org.openhab.binding.aquatemp.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.aquatemp.internal.dto.AquatempMessage;
import org.openhab.binding.aquatemp.internal.dto.device.DataDTO;
import org.openhab.binding.aquatemp.internal.dto.device.ErrorDTO;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AquatempThingHandler} is the abstract base class for all Aquatemp thing handlers.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public abstract class AquatempThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AquatempThingHandler.class);

    public AquatempThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Initialize device state. Should be called at the end of initialize(). Also called by bridgeStatusChanged() when
     * bridge status changes from OFFLINE to ONLINE. Calls initChannelState() to initialize channels if setting status
     * to ONLINE.
     */
    protected void initDeviceState() {
        logger.trace("Initializing device state");
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            initChannelState();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Initialize channel states if necessary
     */
    public abstract void initChannelState();

    /**
     * Notify handler of a device from the Aquatemp via the bridge
     *
     * @param data The DataDTO to handle
     */
    public abstract void handleUpdate(DataDTO data);

    /**
     * Notify handler of a device from the Aquatemp via the bridge
     *
     * @param error The ErrorDTO to handle
     */
    public abstract void handleUpdateError(ErrorDTO error);

    /**
     * Notify handler of a channel message from the Aquatemp via the bridge
     *
     * @param msg The AquatempMessage to handle
     */
    public abstract void handleUpdateChannel(AquatempMessage msg);

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        ThingStatus bridgeStatus = bridgeStatusInfo.getStatus();
        logger.debug("Bridge status changed to {} for Aquatemp handler", bridgeStatus);

        if (bridgeStatus == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            initDeviceState();
        } else if (bridgeStatus == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    public void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail, String statusMessage) {
        updateStatus(status, statusDetail, statusMessage);
    }

    public void updateThingStatus(ThingStatus status) {
        updateStatus(status);
    }
}
