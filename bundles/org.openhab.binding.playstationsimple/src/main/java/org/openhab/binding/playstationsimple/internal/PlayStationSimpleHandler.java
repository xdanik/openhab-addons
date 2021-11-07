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
package org.openhab.binding.playstationsimple.internal;

import static org.openhab.binding.playstationsimple.internal.PlayStationSimpleBindingConstants.*;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DTO.PS5ResponseParser;

/**
 * The {@link PlayStationSimpleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Bartonicek - Initial contribution
 */
@NonNullByDefault
public class PlayStationSimpleHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PlayStationSimpleHandler.class);

    private @Nullable PlayStationSimpleConfiguration config;

    @Nullable
    private DatagramSocket udpSocket;

    @Nullable
    private InetAddress clientIp;

    @Nullable
    private String userCredential;

    @Nullable
    protected ScheduledFuture refreshScheduler;

    private static final int PS5_PORT = 9302;

    public PlayStationSimpleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        switch (channelUID.getId()) {
            case CHANNEL_POWER:
                try {
                    sendRequest("WAKEUP * HTTP/1.1\nclient-type:vr\nauth-type:R\nmodel:w\napp-type:r\nuser-credential:"
                            + userCredential + "\ndevice-discovery-protocol-version:00030010");
                } catch (IOException e) {
                    logger.error("Unable to send wake command: " + e.getMessage());
                }
                break;
            default:
                logger.warn("Ignoring command to unknown channel {}", channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(PlayStationSimpleConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        if (config == null || config.hostname.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname not set");
            return;
        }
        try {
            clientIp = InetAddress.getByName(config.hostname);
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid hostname: " + e.getMessage());
            return;
        }

        if (config.password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Password not set");
            return;
        }
        userCredential = config.password;

        try {
            udpSocket = new DatagramSocket(0);
            udpSocket.setSoTimeout(2000);
        } catch (SocketException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to setup UDP socket: " + e.getMessage());
            return;
        }

        refreshScheduler = scheduler.scheduleAtFixedRate(this::refresh, 0L, config.refreshInterval, TimeUnit.SECONDS);
    }

    protected void refresh() {
        byte[] buffer = new byte[1024];
        try {
            sendStatusQuery();
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            udpSocket.receive(responsePacket);
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            PS5ResponseParser responseParser = new PS5ResponseParser(response);

            updateState(CHANNEL_POWER, OnOffType.from(responseParser.getPowerStatus()));
            updateState(CHANNEL_RUNNING_APPLICATION_ID, OnOffType.from(responseParser.getRunningApplicationId()));
            updateState(CHANNEL_RUNNING_APPLICATION_NAME, OnOffType.from(responseParser.getRunningApplicationName()));

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected void sendStatusQuery() throws IOException {
        sendRequest("SRCH * HTTP/1.1\ndevice-discovery-protocol-version:00030010");
    }

    protected void sendRequest(String query) throws IOException {
        byte[] buffer = query.getBytes(StandardCharsets.UTF_8);
        DatagramPacket statusPacket = new DatagramPacket(buffer, buffer.length, clientIp, PS5_PORT);
        udpSocket.send(statusPacket);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler");
        if (refreshScheduler != null) {
            refreshScheduler.cancel(true);
        }
        super.dispose();
    }
}