package DTO;

import org.openhab.binding.playstationsimple.internal.PlayStationSimpleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PS5ResponseParser {
    private final Logger logger = LoggerFactory.getLogger(PlayStationSimpleHandler.class);

    protected boolean powerStatus;
    protected String statusLine;

    public PS5ResponseParser(String response) {
        String[] lines = response.split("\n");

        statusLine = lines[0];
        String[] statusLineParts = statusLine.split(" ", 3);
        powerStatus = !statusLineParts[1].equals("620");
    }

    public boolean getPowerStatus() {
        return powerStatus;
    }

    public String getStatusLine() {
        return statusLine;
    }
}
