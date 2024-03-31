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
package org.openhab.binding.sonytv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SonyTvBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Bartoníček - Initial contribution
 */
@NonNullByDefault
public class SonyTvBindingConstants {

    public static final String BINDING_ID = "sonytv";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_TV = new ThingTypeUID(BINDING_ID, "tv");

    public static final String CHANNEL_GROUP_VIDEO = "video";
    public static final String CHANNEL_GROUP_SYSTEM = "system";

    // List of all Channel ids
    public static final String CHANNEL_VIDEO_BRIGHTNESS = CHANNEL_GROUP_VIDEO + "#brightness";
    public static final String CHANNEL_VIDEO_SATURATION = CHANNEL_GROUP_VIDEO + "#saturation";
    public static final String CHANNEL_VIDEO_CONTRAST = CHANNEL_GROUP_VIDEO + "#contrast";
    public static final String CHANNEL_VIDEO_SHARPNESS = CHANNEL_GROUP_VIDEO + "#sharpness";
    public static final String CHANNEL_VIDEO_HUE = CHANNEL_GROUP_VIDEO + "#hue";
    public static final String CHANNEL_VIDEO_LOCAL_DIMMING = CHANNEL_GROUP_VIDEO + "#local_dimming";
    public static final String CHANNEL_VIDEO_AUTO_PICTURE_MODE = CHANNEL_GROUP_VIDEO + "#auto_picture_mode";
    public static final String CHANNEL_VIDEO_COLOR_SPACE = CHANNEL_GROUP_VIDEO + "#color_space";
    public static final String CHANNEL_VIDEO_COLOR_TEMPERATURE = CHANNEL_GROUP_VIDEO + "#color_temperature";
    public static final String CHANNEL_VIDEO_LIGHT_SENSOR = CHANNEL_GROUP_VIDEO + "#light_sensor";
    public static final String CHANNEL_VIDEO_PICTURE_MODE = CHANNEL_GROUP_VIDEO + "#picture_mode";
    public static final String CHANNEL_VIDEO_HDR_MODE = CHANNEL_GROUP_VIDEO + "#hdr_mode";
    public static final String CHANNEL_VIDEO_XTENDED_DYNAMIC_RANGE = CHANNEL_GROUP_VIDEO + "#xtended_dynamic_range";

    public static final String CHANNEL_SYSTEM_POWER = CHANNEL_GROUP_SYSTEM + "#power";
    public static final String CHANNEL_SYSTEM_CURRENT_INPUT = CHANNEL_GROUP_SYSTEM + "#current_input";
    public static final String CHANNEL_SYSTEM_DISPLAY_OFF = CHANNEL_GROUP_SYSTEM + "#display_off";
}
