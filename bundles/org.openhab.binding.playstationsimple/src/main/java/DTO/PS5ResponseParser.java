package DTO;

import java.util.HashMap;

public class PS5ResponseParser {
    protected boolean powerStatus;
    protected String runningApplicationName;
    protected String runningApplicationId;
    protected String statusLine;

    public PS5ResponseParser(String response) {
        String[] lines = response.split("\n");

        statusLine = lines[0];
        String[] statusLineParts = statusLine.split(" ", 3);
        powerStatus = !statusLineParts[1].equals("620");

        HashMap<String, String> properties = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int delimiterPosition = line.indexOf(':');
            if (delimiterPosition != -1) {
                properties.put(line.substring(0, delimiterPosition), line.substring(delimiterPosition + 1));
            }
        }

        if (properties.containsKey("running-app-name")) {
            runningApplicationName = properties.get("running-app-name");
        }
        if (properties.containsKey("running-app-titleid")) {
            runningApplicationId = properties.get("running-app-titleid");
        }
    }

    public boolean getPowerStatus() {
        return powerStatus;
    }

    public String getRunningApplicationName() {
        return runningApplicationName;
    }

    public String getRunningApplicationId() {
        return runningApplicationId;
    }

    public String getStatusLine() {
        return statusLine;
    }
}
