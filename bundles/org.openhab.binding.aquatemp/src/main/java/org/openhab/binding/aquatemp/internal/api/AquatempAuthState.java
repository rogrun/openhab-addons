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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AquatempAuthState} represents that steps in the authorization process.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
enum AquatempAuthState {
    /*
     * This is the initial state. It indicates that an "authorize" API call is needed to get
     * the Aquatemp authcode.
     */
    NEED_AUTH,

    /*
     * This state indicates that the "authorize" and "token" steps were successful.
     */
    COMPLETE
}
