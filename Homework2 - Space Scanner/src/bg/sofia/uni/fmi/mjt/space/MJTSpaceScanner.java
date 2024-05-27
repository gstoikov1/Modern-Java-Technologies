package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MJTSpaceScanner implements SpaceScannerAPI {
    private final List<Mission> missions;
    private final List<Rocket> rockets;
    SecretKey secretKey;

    public MJTSpaceScanner(Reader missionsReader, Reader rocketsReader, SecretKey secretKey) {
        this.secretKey = secretKey;

        try (var bf = new BufferedReader(missionsReader)) {
            missions = bf.lines().skip(1).map(Mission::of).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("An error occurred while reading missions", e);
        }
        try (var bf = new BufferedReader(rocketsReader)) {
            rockets = bf.lines().skip(1).map(Rocket::of).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("An error occurred while reading rockets", e);
        }
    }

    public Collection<Mission> getAllMissions() {
        return missions;
    }

    public Collection<Mission> getAllMissions(MissionStatus missionStatus) {
        if (missionStatus == null) {
            throw new IllegalArgumentException("missionStatus was null");
        }
        return missions.stream().filter(m -> m.missionStatus().equals(missionStatus)).toList();
    }

    public String getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to) {
        if (from == null) {
            throw new IllegalArgumentException("from date was null");
        }
        if (to == null) {
            throw new IllegalArgumentException("to date was null");
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("to date was before from date");
        }
        Map<String, Long> result =
            missions.stream().filter(m -> m.date().isAfter(from) && m.date().isBefore(to) &&
                    m.missionStatus().equals(MissionStatus.SUCCESS))
                .collect(Collectors.groupingBy(Mission::company, Collectors.counting()));
        return result.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }

    public Map<String, Collection<Mission>> getMissionsPerCountry() {
        return missions.stream().map(this::mapMissionToCountry)
            .collect(
                Collectors.toMap(AbstractMap.SimpleEntry::getKey, this::newSetFromMissionEntry,
                    this::mergeCollections));

    }

    private AbstractMap.SimpleEntry<String, Mission> mapMissionToCountry(Mission mission) {
        return new AbstractMap.SimpleEntry<>(mission.getCountry(), mission);
    }

    private Set<Mission> newSetFromMissionEntry(AbstractMap.SimpleEntry<String, Mission> missionEntry) {
        Set<Mission> missionSet = new HashSet<>();
        missionSet.add(missionEntry.getValue());
        return missionSet;
    }

    private Collection<Mission> mergeCollections(Collection<Mission> set1, Collection<Mission> set2) {
        set1.addAll(set2);
        return set1;
    }

    public List<Mission> getTopNLeastExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus) {
        if (n < 1) {
            throw new IllegalArgumentException("n cannot be a non-positive integer");
        }
        if (missionStatus == null) {
            throw new IllegalArgumentException("missionStatus was null");
        }
        if (rocketStatus == null) {
            throw new IllegalArgumentException("rocketStatus was null");
        }
        return missions.stream()
            .filter(m -> m.cost().isPresent() &&
                m.missionStatus().equals(missionStatus) &&
                m.rocketStatus().equals(rocketStatus))
            .sorted(Comparator.comparingDouble(m -> m.cost().orElse(0.0)))
            .limit(n)
            .toList();
    }

    public Map<String, String> getMostDesiredLocationForMissionsPerCompany() {
        return
            missions.stream()
                .map(this::mapMissionToCompanyAndLocation)
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, this::newListFromCompanyAndLocationEntry,
                    this::mergeLists)).entrySet().stream().map(this::convertToMostDesiredLocationPerCompany)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private AbstractMap.SimpleEntry<String, String> mapMissionToCompanyAndLocation(Mission mission) {
        return new AbstractMap.SimpleEntry<>(mission.company(), mission.location());
    }

    private List<String> newListFromCompanyAndLocationEntry(
        AbstractMap.SimpleEntry<String, String> companyLocationEntry) {
        List<String> locationSet = new ArrayList<>();
        locationSet.add(companyLocationEntry.getValue());
        return locationSet;
    }

    private List<String> mergeLists(List<String> s1, List<String> s2) {
        s1.addAll(s2);
        return s1;
    }

    private Map.Entry<String, String> convertToMostDesiredLocationPerCompany(
        Map.Entry<String, List<String>> companyLocationsEntry) {
        Map<String, Long> result = companyLocationsEntry
            .getValue()
            .stream()
            .collect(Collectors.groupingBy(String::toString, Collectors.counting()));
        return new AbstractMap.SimpleEntry<>(companyLocationsEntry.getKey(),
            result.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey());

    }

    public Map<String, String> getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to) {
        if (from == null) {
            throw new IllegalArgumentException("from date was null");
        }
        if (to == null) {
            throw new IllegalArgumentException("to date was null");
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("to date was before from date");
        }

        return
            missions.stream()
                .filter(m -> m.missionStatus().equals(MissionStatus.SUCCESS) && m.date().isAfter(from) &&
                    m.date().isBefore(to))
                .map(this::mapMissionToCompanyAndLocation)
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, this::newListFromCompanyAndLocationEntry,
                    this::mergeLists)).entrySet().stream().map(this::convertToMostDesiredLocationPerCompany)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Collection<Rocket> getAllRockets() {
        return rockets;
    }

    public List<Rocket> getTopNTallestRockets(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n cannot be a non-positive integer");
        }
        return rockets.stream()
            .filter(r -> r.height().isPresent())
            .sorted(Comparator.comparingDouble(r -> ((Rocket) r).height().get()).reversed())
            .limit(n)
            .toList();
    }

    public Map<String, Optional<String>> getWikiPageForRocket() {
        return rockets.stream().map(this::mapRocketToNameAndWiki)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

    private Map.Entry<String, Optional<String>> mapRocketToNameAndWiki(Rocket rocket) {
        return new AbstractMap.SimpleEntry<>(rocket.name(), rocket.wiki());
    }

    public List<String> getWikiPagesForRocketsUsedInMostExpensiveMissions(int n, MissionStatus missionStatus,
                                                                          RocketStatus rocketStatus) {
        if (n < 1) {
            throw new IllegalArgumentException("n cannot be a non-positive integer");
        }
        if (missionStatus == null) {
            throw new IllegalArgumentException("missionStatus was null");
        }
        if (rocketStatus == null) {
            throw new IllegalArgumentException("rocketStatus was null");
        }
        Map<String, Optional<String>> rocketWikiMap = this.getWikiPageForRocket();

        return missions.stream().filter(
                m -> m.missionStatus().equals(missionStatus) && m.rocketStatus().equals(rocketStatus) &&
                    m.cost().isPresent())
            .sorted(Comparator.comparingDouble(m -> ((Mission) m).cost().orElse(0.0)).reversed()).limit(n)
            .map(Mission::getRocketName).map(m -> nameToWiki(m, rocketWikiMap)).filter(m -> m.isPresent())
            .map(Optional::get).toList();
    }

    private Optional<String> nameToWiki(String name, Map<String, Optional<String>> rocketWikiMap) {
        return rocketWikiMap.get(name.strip());
    }

    public void saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to) throws CipherException {
        if (from == null) {
            throw new IllegalArgumentException("from date was null");
        }
        if (to == null) {
            throw new IllegalArgumentException("to date was null");
        }
        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream was null");
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("to date was before from date");
        }
        String mostReliableRocketName = getMostReliableRocketName(from, to);
        byte[] byteArray = mostReliableRocketName.getBytes();
        InputStream inputStream = new ByteArrayInputStream(byteArray);

        Rijndael rijndael = new Rijndael(secretKey);
        rijndael.encrypt(inputStream, outputStream);
    }

    private String getMostReliableRocketName(LocalDate from, LocalDate to) {
        return missions.stream()
            .filter(m -> m.date().isAfter(from) && m.date().isBefore(to))
            .collect(Collectors.toMap(Mission::getRocketName, this::newSetFromMission, this::mergeSets)).entrySet()
            .stream().map(this::calculateRocketsReliabilityBaseOnMissions)
            .max(Comparator.comparingDouble(Map.Entry::getValue))
            .orElseGet(() -> new AbstractMap.SimpleEntry<>("", 0.0)).getKey();
    }

    private Set<Mission> newSetFromMission(Mission mission) {
        Set<Mission> missionsSet = new HashSet<>();
        missionsSet.add(mission);
        return missionsSet;
    }

    private Set<Mission> mergeSets(Set<Mission> s1, Set<Mission> s2) {
        s1.addAll(s2);
        return s1;
    }

    private Map.Entry<String, Double> calculateRocketsReliabilityBaseOnMissions(
        Map.Entry<String, Set<Mission>> missionEntry) {
        String rocketName = missionEntry.getKey();
        Set<Mission> missions = missionEntry.getValue();
        if (missions.isEmpty()) {
            return new AbstractMap.SimpleEntry<>(rocketName, 0.0);
        }
        long successfulMissionsCount =
            missions.stream().filter(m -> m.missionStatus().equals(MissionStatus.SUCCESS)).count();
        long unsuccessfulMissionsCount = missions.size() - successfulMissionsCount;

        double result = (2 * successfulMissionsCount + unsuccessfulMissionsCount) / 2.0 * missions.size();
        return new AbstractMap.SimpleEntry<>(rocketName, result);
    }
}