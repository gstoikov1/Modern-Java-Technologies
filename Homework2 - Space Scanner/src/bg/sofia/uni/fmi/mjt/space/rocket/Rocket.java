package bg.sofia.uni.fmi.mjt.space.rocket;

import java.util.Optional;

public record Rocket(String id, String name, Optional<String> wiki, Optional<Double> height) {
    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int WIKI_INDEX = 2;
    private static final int HEIGHT_INDEX = 3;

    private static final int PARTS_COUNT_IF_NO_HEIGHT_PRESENT = 3;
    private static final int PARTS_COUNT_IF_NO_HEIGHT_AND_NO_WIKI_PRESENT = 2;

    public static Rocket of(String line) {
        String regex = line.contains("\"") ? ",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)" : ",";
        String[] parts = line.split(regex);

        if (parts.length == PARTS_COUNT_IF_NO_HEIGHT_AND_NO_WIKI_PRESENT) {
            return new Rocket(parts[ID_INDEX], parts[NAME_INDEX].replaceAll("\"", "").strip(), formatWiki(""),
                formatHeight(""));
        } else if (parts.length == PARTS_COUNT_IF_NO_HEIGHT_PRESENT) {
            return new Rocket(parts[ID_INDEX], parts[NAME_INDEX].replaceAll("\"", "").strip(),
                formatWiki(parts[WIKI_INDEX]),
                formatHeight(""));
        } else {
            return new Rocket(parts[ID_INDEX], parts[NAME_INDEX].replaceAll("\"", "").strip(),
                formatWiki(parts[WIKI_INDEX]),
                formatHeight(parts[HEIGHT_INDEX]));
        }
    }

    private static Optional<String> formatWiki(String wiki) {
        if (wiki.isBlank() || wiki.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(wiki);
    }

    private static Optional<Double> formatHeight(String height) {
        if (height.isBlank() || height.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Double.valueOf(height.substring(0, height.length() - 1)));
    }
}
