package bg.sofia.uni.fmi.mjt.space.mission;

import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.lang.reflect.Member;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public record Mission(String id, String company, String location, LocalDate date, Detail detail,
                      RocketStatus rocketStatus, Optional<Double> cost, MissionStatus missionStatus) {
    private static final String REGEX = ",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";

    private static final int ID_INDEX = 0;
    private static final int COMPANY_INDEX = 1;
    private static final int LOCATION_INDEX = 2;
    private static final int DATE_INDEX = 3;
    private static final int DETAIL_INDEX = 4;
    private static final int ROCKET_STATUS_INDEX = 5;
    private static final int COST_INDEX = 6;
    private static final int MISSION_STATUS_INDEX = 7;

    public static Mission of(String line) {
        String[] parts = line.split(REGEX);
        return new Mission(parts[ID_INDEX], parts[COMPANY_INDEX], parts[LOCATION_INDEX].replaceAll("\"", "").strip(),
            formatDate(parts[DATE_INDEX].replaceAll("\"", "")), Detail.of(parts[DETAIL_INDEX]),
            formatRocketStatus(parts[ROCKET_STATUS_INDEX]),
            formatDouble(parts[COST_INDEX].replaceAll("\"", "").strip()),
            formatMissionStatus(parts[MISSION_STATUS_INDEX]));
    }

    public String getCountry() {
        String[] locationParts = this.location.split(",");
        return locationParts[locationParts.length - 1].strip();
    }

    public String getRocketName() {
        return this.detail.rocketName();
    }

    private static RocketStatus formatRocketStatus(String line) {
        return switch (line) {
            case "StatusActive" -> RocketStatus.STATUS_ACTIVE;
            case "StatusRetired" -> RocketStatus.STATUS_RETIRED;
            default -> throw new IllegalArgumentException("Invalid RocketStatus value");
        };
    }

    private static MissionStatus formatMissionStatus(String status) {
        return switch (status) {
            case "Success" -> MissionStatus.SUCCESS;
            case "Failure" -> MissionStatus.FAILURE;
            case "Partial Failure" -> MissionStatus.PARTIAL_FAILURE;
            case "Prelaunch Failure" -> MissionStatus.PRELAUNCH_FAILURE;
            default -> throw new IllegalArgumentException("Invalid Mission Status");
        };
    }

    private static LocalDate formatDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd, yyyy", Locale.ENGLISH);

        return LocalDate.parse(date, formatter);
    }

    private static Optional<Double> formatDouble(String cost) {

        if (cost.isBlank() || cost.isEmpty()) {
            return Optional.empty();
        }
        DecimalFormat decimalFormat = new DecimalFormat("#,###.0");
        try {
            double parsedNumber = decimalFormat.parse(cost).doubleValue();
            return Optional.of(parsedNumber);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

}
