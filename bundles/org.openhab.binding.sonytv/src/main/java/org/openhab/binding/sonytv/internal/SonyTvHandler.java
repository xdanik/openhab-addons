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

import static org.openhab.binding.sonytv.internal.SonyTvBindingConstants.*;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.sonytv.internal.DTO.VideoOptionResponse;
import org.openhab.binding.sonytv.internal.exceptions.ApiException;
import org.openhab.binding.sonytv.internal.exceptions.ConnectionException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;

/**
 * The {@link SonyTvHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Bartoníček - Initial contribution
 */
@NonNullByDefault
public class SonyTvHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SonyTvHandler.class);

    private @Nullable SonyTvConfiguration config;

    public SonyTvHandler(Thing thing) {
        super(thing);
        httpClient = new HttpClient();
        gson = new Gson();
    }

    protected Gson gson;

    protected HttpClient httpClient;

    @Nullable
    protected ScheduledFuture refreshScheduler;

    protected Boolean isActive = false;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Received command {} for channel {}", command.toFullString(), channelUID.getAsString());

        if (channelUID.getGroupId() != null) {
            String target = null;
            String value = null;

            switch (channelUID.getGroupId()) {
                case CHANNEL_GROUP_VIDEO:
                    switch (channelUID.getId()) {
                        case CHANNEL_VIDEO_PICTURE_MODE:
                            target = "pictureMode";
                            value = command.toFullString();
                            break;
                        case CHANNEL_VIDEO_BRIGHTNESS:
                            target = "brightness";
                            value = command.toFullString();
                            break;
                        case CHANNEL_VIDEO_AUTO_PICTURE_MODE:
                            target = "autoPictureMode";
                            value = command.toFullString();
                            break;
                        case CHANNEL_VIDEO_COLOR_SPACE:
                            target = "colorSpace";
                            value = command.toFullString();
                            break;
                        case CHANNEL_VIDEO_COLOR_TEMPERATURE:
                            target = "colorTemperature";
                            value = command.toFullString();
                            break;
                        case CHANNEL_VIDEO_HDR_MODE:
                            target = "hdrMode";
                            value = command.toFullString();
                            break;
                        case CHANNEL_VIDEO_LIGHT_SENSOR:
                            target = "lightSensor";
                            value = command.toFullString().equalsIgnoreCase("on") ? "on" : "off";
                            break;
                        case CHANNEL_VIDEO_LOCAL_DIMMING:
                            target = "autoLocalDimming";
                            value = command.toFullString();
                            break;
                        case CHANNEL_VIDEO_SATURATION:
                            target = "color";
                            value = command.toFullString();
                            break;
                        case CHANNEL_VIDEO_CONTRAST:
                            target = "contrast";
                            value = command.toFullString();
                            break;
                        case CHANNEL_VIDEO_HUE:
                            target = "hue";
                            value = command.toFullString();
                            break;
                        case CHANNEL_VIDEO_SHARPNESS:
                            target = "sharpness";
                            value = command.toFullString();
                            break;
                        case CHANNEL_VIDEO_XTENDED_DYNAMIC_RANGE:
                            target = "xtendedDynamicRange";
                            value = command.toFullString();
                            break;
                    }
                    if (target != null && value != null) {
                        try {
                            dispatchRequest("video", "setPictureQualitySettings", 12,
                                    buildVideoSetParams(target, value));
                        } catch (ConnectionException | ApiException e) {
                            logger.error("Command failed: {}", e.getMessage(), e);
                        }
                        return;
                    }
                    break;
                case CHANNEL_GROUP_SYSTEM:
                    switch (channelUID.getId()) {
                        case CHANNEL_SYSTEM_POWER:
                            try {
                                sendPowerCommand(command.toFullString().equalsIgnoreCase("on"));
                            } catch (ConnectionException | ApiException e) {
                                logger.error("Command failed: {}", e.getMessage(), e);
                            }
                            break;
                        case CHANNEL_SYSTEM_CURRENT_INPUT:
                            try {
                                sendSwitchInputCommand(command.toFullString());
                            } catch (ConnectionException | ApiException e) {
                                logger.error("Command failed: {}", e.getMessage(), e);
                            }
                            break;
                    }
                    break;
            }
        }
        logger.warn("Ignoring command to unknown channel {}", channelUID.getId());
    }

    protected void sendPowerCommand(Boolean state) throws ConnectionException, ApiException {
        JsonArray params = new JsonArray();
        JsonObject standByObject = new JsonObject();
        standByObject.addProperty("status", state);
        params.add(standByObject);
        dispatchRequest("system", "setPowerStatus", 55, params);
    }

    protected void sendSwitchInputCommand(String input) throws ConnectionException, ApiException {
        switch (input) {
            case "hdmi1": {
                input = "extInput:hdmi?port=1";
                break;
            }
            case "hdmi2": {
                input = "extInput:hdmi?port=2";
                break;
            }
            case "hdmi3": {
                input = "extInput:hdmi?port=3";
                break;
            }
            case "hdmi4": {
                input = "extInput:hdmi?port=4";
                break;
            }
            case "miracast": {
                input = "extInput:widi?port=1";
                break;
            }
        }

        JsonArray params = new JsonArray();
        JsonObject standByObject = new JsonObject();
        standByObject.addProperty("uri", input);
        params.add(standByObject);
        dispatchRequest("avContent", "setPlayContent", 55, params);
    }

    @Override
    public void initialize() {
        config = getConfigAs(SonyTvConfiguration.class);

        if (config == null || config.host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host not configured");
            return;
        }
        if (config.apiKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Api key not configured");
        }

        try {
            httpClient.start();
        } catch (Exception e) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to init http client. Error: " + e.getMessage());
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        refreshScheduler = scheduler.scheduleAtFixedRate(this::refresh, 0L, config.refreshInterval, TimeUnit.SECONDS);
    }

    protected void refresh() {
        try {
            refreshPowerStatus();
            if (isActive) {
                refreshCurrentInput();
                refreshVideoOptions();
            }
        } catch (ConnectionException e) {
            if (e.getCause() instanceof TimeoutException) {
                logger.warn(e.getMessage(), e);
            } else {
                updateStatus(ThingStatus.OFFLINE);
                logger.error("Error during refresh: {}", e.getMessage(), e);
            }
            return;
        } catch (ApiException e) {
            logger.error("Error during refresh: {}", e.getMessage(), e);
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    public ApiResponse dispatchRequest(String endpoint, String method, int methodId, JsonArray params)
            throws ConnectionException, ApiException {
        config = getConfigAs(SonyTvConfiguration.class);

        URI uri = URI.create("http://" + config.host + "/sony/" + endpoint);
        Request request = httpClient.newRequest(uri);
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.header("X-Auth-PSK", config.apiKey);
        request.header("cache-control", "no-cache");
        request.timeout(1000L, TimeUnit.MILLISECONDS);

        JsonObject requestObject = new JsonObject();
        requestObject.addProperty("method", method);
        requestObject.addProperty("id", methodId);
        requestObject.addProperty("version", "1.0");
        requestObject.add("params", params);

        String payload = gson.toJson(requestObject);

        request.content(new StringContentProvider(payload, "utf-8"));

        ContentResponse response;
        try {
            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ConnectionException(e.getMessage(), e);
        }

        ApiResponse apiResponse = gson.fromJson(response.getContentAsString(), ApiResponse.class);

        if (apiResponse.error != null) {
            throw new ApiException(apiResponse.error.toString());
        }

        return apiResponse;
    }

    protected void refreshVideoOptions() throws ConnectionException, ApiException {
        JsonArray params = new JsonArray();
        params.add(new JsonObject());

        ApiResponse response = dispatchRequest("video", "getPictureQualitySettings", 52, params);

        try {
            for (JsonElement optionJson : response.result.getAsJsonArray().get(0).getAsJsonArray()) {
                VideoOptionResponse option = gson.fromJson(optionJson, VideoOptionResponse.class);

                switch (option.target) {
                    case "brightness":
                        updateState(CHANNEL_VIDEO_BRIGHTNESS, DecimalType.valueOf(option.currentValue));
                        break;
                    case "color":
                        updateState(CHANNEL_VIDEO_SATURATION, DecimalType.valueOf(option.currentValue));
                        break;
                    case "contrast":
                        updateState(CHANNEL_VIDEO_CONTRAST, DecimalType.valueOf(option.currentValue));
                        break;
                    case "sharpness":
                        updateState(CHANNEL_VIDEO_SHARPNESS, DecimalType.valueOf(option.currentValue));
                        break;
                    case "hue":
                        updateState(CHANNEL_VIDEO_HUE, DecimalType.valueOf(option.currentValue));
                        break;
                    case "autoLocalDimming":
                        updateState(CHANNEL_VIDEO_LOCAL_DIMMING, StringType.valueOf(option.currentValue));
                        break;
                    case "autoPictureMode":
                        updateState(CHANNEL_VIDEO_AUTO_PICTURE_MODE, StringType.valueOf(option.currentValue));
                        break;
                    case "colorSpace":
                        updateState(CHANNEL_VIDEO_COLOR_SPACE, StringType.valueOf(option.currentValue));
                        break;
                    case "colorTemperature":
                        updateState(CHANNEL_VIDEO_COLOR_TEMPERATURE, StringType.valueOf(option.currentValue));
                        break;
                    case "lightSensor":
                        updateState(CHANNEL_VIDEO_LIGHT_SENSOR, OnOffType.from(option.currentValue.equals("on")));
                        break;
                    case "pictureMode":
                        updateState(CHANNEL_VIDEO_PICTURE_MODE, StringType.valueOf(option.currentValue));
                        break;
                    case "hdrMode":
                        updateState(CHANNEL_VIDEO_HDR_MODE, StringType.valueOf(option.currentValue));
                        break;
                    case "xtendedDynamicRange":
                        updateState(CHANNEL_VIDEO_XTENDED_DYNAMIC_RANGE, StringType.valueOf(option.currentValue));
                        break;
                }
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException(e.getMessage());
        }
    }

    protected void refreshCurrentInput() throws ConnectionException, ApiException {

        ApiResponse response = dispatchRequest("avContent", "getPlayingContentInfo", 103, new JsonArray());
        String input;
        try {
            input = response.result.getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("uri").getAsString();
        } catch (JsonSyntaxException e) {
            throw new ApiException(e.getMessage());
        }
        String humanFriendlyInput;
        switch (input) {
            case "extInput:hdmi?port=1": {
                humanFriendlyInput = "hdmi1";
                break;
            }
            case "extInput:hdmi?port=2": {
                humanFriendlyInput = "hdmi2";
                break;
            }
            case "extInput:hdmi?port=3": {
                humanFriendlyInput = "hdmi3";
                break;
            }
            case "extInput:hdmi?port=4": {
                humanFriendlyInput = "hdmi4";
                break;
            }
            case "extInput:widi?port=1": {
                humanFriendlyInput = "miracast";
                break;
            }
            default:
                humanFriendlyInput = input;
        }
        updateState(CHANNEL_SYSTEM_CURRENT_INPUT, StringType.valueOf(humanFriendlyInput));
    }

    protected void refreshPowerStatus() throws ConnectionException, ApiException {
        ApiResponse response = dispatchRequest("system", "getPowerStatus", 50, new JsonArray());
        String status;
        try {
            status = response.result.getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("status")
                    .getAsString();
        } catch (JsonSyntaxException e) {
            throw new ApiException(e.getMessage());
        }

        isActive = status.equalsIgnoreCase("active");
        updateState(CHANNEL_SYSTEM_POWER, OnOffType.from(isActive));
    }

    @Override
    public void dispose() {
        logger.warn("Disposing handler");
        if (refreshScheduler != null) {
            refreshScheduler.cancel(true);
        }
        super.dispose();
    }

    protected JsonArray buildVideoSetParams(String target, String value) {
        JsonArray params = new JsonArray();
        JsonObject settingsObject = new JsonObject();
        params.add(settingsObject);
        JsonArray settings = new JsonArray();
        settingsObject.add("settings", settings);

        JsonObject newValue = new JsonObject();
        newValue.addProperty("value", value);
        newValue.addProperty("target", target);
        settings.add(newValue);

        return params;
    }
}
