package DTO;

public class PS5ResponseParser {
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
