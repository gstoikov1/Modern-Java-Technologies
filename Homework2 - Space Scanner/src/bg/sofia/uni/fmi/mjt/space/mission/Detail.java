package bg.sofia.uni.fmi.mjt.space.mission;

public record Detail(String rocketName, String payload) {
    private static final int ROCKET_NAME_INDEX = 0;
    public static final int PAYLOAD_INDEX = 1;

    public static Detail of(String line) {
        String[] parts = line.split("\\|");
        return new Detail(parts[ROCKET_NAME_INDEX].replaceAll("\"", "").strip(), parts[PAYLOAD_INDEX].strip());
    }

}

