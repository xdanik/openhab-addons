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
package org.openhab.binding.sonytv.internal.DTO;

/**
 * The {@link VideoOptionResponse} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Bartoníček - Initial contribution
 */
public class VideoOptionResponse {
    public String target;

    public String currentValue;

    public boolean isAvailable;
}
