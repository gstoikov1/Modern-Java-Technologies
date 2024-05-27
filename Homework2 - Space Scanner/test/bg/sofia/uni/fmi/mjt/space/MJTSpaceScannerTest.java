package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Detail;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.management.openmbean.InvalidOpenTypeException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MJTSpaceScannerTest {

    @Test
    void testMJTSpaceScannerConstructorReadMissions() {
        String missionsFile = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            0,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Fri Aug 07, 2020",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
            53,MHI,"LA-Y1, Tanegashima Space Center, Japan","Sun Feb 09, 2020",H-IIA 202 | IGS-Optical 7,StatusActive,"90.0 ",Success
            54,Arianespace,"Site 31/6, Baikonur Cosmodrome, Kazakhstan","Thu Feb 06, 2020",Soyuz 2.1b/Fregat | OneWeb #2,StatusActive,"48.5 ",Success
            271,CASC,"LC-2, Xichang Satellite Launch Center, China","Mon Feb 12, 2018",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success
            """;
        List<Mission> missions = List.of(
            new Mission("0", "SpaceX", "LC-39A, Kennedy Space Center, Florida, USA", LocalDate.of(2020, 8, 7),
                new Detail("Falcon 9 Block 5", "Starlink V1 L9 & BlackSky"),
                RocketStatus.STATUS_ACTIVE, Optional.of(50.0), MissionStatus.SUCCESS),
            new Mission("1", "CASC", "Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China",
                LocalDate.of(2020, 8, 6),
                new Detail("Long March 2D", "Gaofen-9 04 & Q-SAT"),
                RocketStatus.STATUS_ACTIVE, Optional.of(29.75), MissionStatus.SUCCESS),
            new Mission("53", "MHI", "LA-Y1, Tanegashima Space Center, Japan", LocalDate.of(2020, 2, 9),
                new Detail("H-IIA 202", "IGS-Optical 7"),
                RocketStatus.STATUS_ACTIVE, Optional.of(90.0), MissionStatus.SUCCESS),
            new Mission("54", "Arianespace", "Site 31/6, Baikonur Cosmodrome, Kazakhstan", LocalDate.of(2020, 2, 6),
                new Detail("Soyuz 2.1b/Fregat", "OneWeb #2"),
                RocketStatus.STATUS_ACTIVE, Optional.of(48.5), MissionStatus.SUCCESS),
            new Mission("271", "CASC", "LC-2, Xichang Satellite Launch Center, China", LocalDate.of(2018, 2, 12),
                new Detail("Long March 3B/YZ-1", "BeiDou-3 M3 & M4"), RocketStatus.STATUS_ACTIVE, Optional.empty(),
                MissionStatus.SUCCESS)
        );

        Reader r = new StringReader(missionsFile);
        MJTSpaceScanner mjtSpaceScanner =
            new MJTSpaceScanner(new StringReader(missionsFile), new StringReader(""), null);
        assertEquals(missions, mjtSpaceScanner.getAllMissions(),
            "MJTSpaceScanner has to correctly parse data into objects of Mission");
    }

    @Test
    void testMJTSpaceScannerConstructorReadRockets() {
        String rocketsFile = """
            "",Name,Wiki,Rocket Height
            0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m
            1,Tsyklon-4M,https://en.wikipedia.org/wiki/Cyclone-4M,38.7 m
            2,Unha-2,https://en.wikipedia.org/wiki/Unha,28.0 m
            3,Unha-3,https://en.wikipedia.org/wiki/Unha,32.0 m
            62,Atlas-E/F Burner,,
            109,Ceres-1,,19.0 m
            179,H-I (9 SO),https://en.wikipedia.org/wiki/H-I,42.0 m
            180,H-II,https://en.wikipedia.org/wiki/H-II,49.0 m
            """;
        List<Rocket> rockets = List.of(
            new Rocket("0", "Tsyklon-3", Optional.of("https://en.wikipedia.org/wiki/Tsyklon-3"), Optional.of(39.0)),
            new Rocket("1", "Tsyklon-4M", Optional.of("https://en.wikipedia.org/wiki/Cyclone-4M"), Optional.of(38.7)),
            new Rocket("2", "Unha-2", Optional.of("https://en.wikipedia.org/wiki/Unha"), Optional.of(28.0)),
            new Rocket("3", "Unha-3", Optional.of("https://en.wikipedia.org/wiki/Unha"), Optional.of(32.0)),
            new Rocket("62", "Atlas-E/F Burner", Optional.empty(), Optional.empty()),
            new Rocket("109", "Ceres-1", Optional.empty(), Optional.of(19.0)),
            new Rocket("179", "H-I (9 SO)", Optional.of("https://en.wikipedia.org/wiki/H-I"), Optional.of(42.0)),
            new Rocket("180", "H-II", Optional.of("https://en.wikipedia.org/wiki/H-II"), Optional.of(49.0))
        );

        MJTSpaceScanner mjtSpaceScanner =
            new MJTSpaceScanner(new StringReader(""), new StringReader(rocketsFile), null);

        assertEquals(rockets, mjtSpaceScanner.getAllRockets(),
            "MJTSpaceScanner has to correctly parse data into objects of Rocket");
    }

    @Test
    void testGetAllMissionsMissionStatusPartialFailure() {
        String s = """
            275,Roscosmos,"Site 1S, Vostochny Cosmodrome, Russia","Thu Feb 01, 2018",Soyuz 2.1a/Fregat-M | Kanopus-V No. 3-4 & Rideshares,StatusActive,"48.5 ",Success
            276,SpaceX,"SLC-40, Cape Canaveral AFS, Florida, USA","Wed Jan 31, 2018",Falcon 9 Block 3 | GovSat-1 / SES-16,StatusRetired,"62.0 ",Success
            277,Arianespace,"ELA-3, Guiana Space Centre, French Guiana, France","Thu Jan 25, 2018",Ariane 5 ECA | SES-14/GOLD & Al Yah-3,StatusActive,"200.0 ",Partial Failure
            336,Roscosmos,"Site 31/6, Baikonur Cosmodrome, Kazakhstan","Fri Jul 14, 2017",Soyuz 2.1a/Fregat | Kanopus-V IK & Rideshares,StatusActive,"48.5 ",Partial Failure
            344,CASC,"LC-2, Xichang Satellite Launch Center, China","Sun Jun 18, 2017",Long March 3B/E | ChinaSat 9A,StatusActive,"29.15 ",Partial Failure
            """;
        List<Mission> missions = List.of(
            Mission.of(
                "277,Arianespace,\"ELA-3, Guiana Space Centre, French Guiana, France\",\"Thu Jan 25, 2018\",Ariane 5 ECA | SES-14/GOLD & Al Yah-3,StatusActive,\"200.0 \",Partial Failure"),
            Mission.of(
                "336,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Fri Jul 14, 2017\",Soyuz 2.1a/Fregat | Kanopus-V IK & Rideshares,StatusActive,\"48.5 \",Partial Failure"),
            Mission.of(
                "344,CASC,\"LC-2, Xichang Satellite Launch Center, China\",\"Sun Jun 18, 2017\",Long March 3B/E | ChinaSat 9A,StatusActive,\"29.15 \",Partial Failure")
        );
        MJTSpaceScanner mjtSpaceScanner = new MJTSpaceScanner(new StringReader(s), new StringReader(""), null);

        assertEquals(missions, mjtSpaceScanner.getAllMissions(MissionStatus.PARTIAL_FAILURE),
            "result should be a collection with all missions with the given status");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsInvalidArguments() {
        MJTSpaceScanner mjtSpaceScanner = new MJTSpaceScanner(new StringReader(""), new StringReader(""), null);
        LocalDate from = LocalDate.of(2000, 12, 12);
        LocalDate to = LocalDate.of(1999, 12, 12);
        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.getCompanyWithMostSuccessfulMissions(null, to), "from date cannot be null");
        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.getCompanyWithMostSuccessfulMissions(from, null), "to date cannot be null");
        assertThrows(TimeFrameMismatchException.class,
            () -> mjtSpaceScanner.getCompanyWithMostSuccessfulMissions(from, to), "to date cannot be before from date");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissions() {
        String missionsFile = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            0,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Fri Aug 07, 2020",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            0,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Fri Aug 07, 2020",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
            53,MHI,"LA-Y1, Tanegashima Space Center, Japan","Sun Feb 09, 2020",H-IIA 202 | IGS-Optical 7,StatusActive,"90.0 ",Success
            54,Arianespace,"Site 31/6, Baikonur Cosmodrome, Kazakhstan","Thu Feb 06, 2020",Soyuz 2.1b/Fregat | OneWeb #2,StatusActive,"48.5 ",Success
            271,CASC,"LC-2, Xichang Satellite Launch Center, China","Mon Feb 12, 2018",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success
            272,CASC,"LC-2, Xichang Satellite Launch Center, China","Mon Feb 12, 2018",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success
            273,CASC,"LC-2, Xichang Satellite Launch Center, China","Mon Feb 12, 2018",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success
            274,CASC,"LC-2, Xichang Satellite Launch Center, China","Mon Feb 12, 2018",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success
            275,CASC,"LC-2, Xichang Satellite Launch Center, China","Mon Feb 12, 2018",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success
            """;
        MJTSpaceScanner mjtSpaceScanner =
            new MJTSpaceScanner(new StringReader(missionsFile), new StringReader(""), null);
        assertEquals("SpaceX",
            mjtSpaceScanner.getCompanyWithMostSuccessfulMissions(LocalDate.of(2019, 1, 10), LocalDate.of(2023, 1, 10)),
            "result should be the name of the company with most successful missions in the given time frame");
    }

    @Test
    void testGetMissionsPerCountry() {
        String missionsFile = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            0,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Fri Aug 07, 2020",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
            53,MHI,"LA-Y1, Tanegashima Space Center, Japan","Sun Feb 09, 2020",H-IIA 202 | IGS-Optical 7,StatusActive,"90.0 ",Success
            54,Arianespace,"Site 31/6, Baikonur Cosmodrome, Kazakhstan","Thu Feb 06, 2020",Soyuz 2.1b/Fregat | OneWeb #2,StatusActive,"48.5 ",Success
            271,CASC,"LC-2, Xichang Satellite Launch Center, China","Mon Feb 12, 2018",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success
            378,SpaceX,"SLC-4E, Vandenberg AFB, California, USA","Sat Jan 14, 2017",Falcon 9 Block 3 | Iridium-1,StatusRetired,"62.0 ",Success
            """;
        Mission mission1 = Mission.of(
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success");
        Mission mission2 = Mission.of(
            "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success");
        Mission mission3 = Mission.of(
            "53,MHI,\"LA-Y1, Tanegashima Space Center, Japan\",\"Sun Feb 09, 2020\",H-IIA 202 | IGS-Optical 7,StatusActive,\"90.0 \",Success");
        Mission mission4 = Mission.of(
            "54,Arianespace,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Thu Feb 06, 2020\",Soyuz 2.1b/Fregat | OneWeb #2,StatusActive,\"48.5 \",Success");
        Mission mission5 = Mission.of(
            "271,CASC,\"LC-2, Xichang Satellite Launch Center, China\",\"Mon Feb 12, 2018\",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success");
        Mission mission6 = Mission.of(
            "378,SpaceX,\"SLC-4E, Vandenberg AFB, California, USA\",\"Sat Jan 14, 2017\",Falcon 9 Block 3 | Iridium-1,StatusRetired,\"62.0 \",Success");
        Map<String, Collection<Mission>> map = new HashMap<>();
        map.put("USA", List.of(mission1, mission6));
        map.put("China", List.of(mission2, mission5));
        map.put("Japan", List.of(mission3));
        map.put("Kazakhstan", List.of(mission4));

        MJTSpaceScanner mjtSpaceScanner =
            new MJTSpaceScanner(new StringReader(missionsFile), new StringReader(""), null);

        Map<String, Collection<Mission>> map1 = mjtSpaceScanner.getMissionsPerCountry();

        for (String s : map.keySet()) {
            for (Mission mission : map.get(s)) {
                assertTrue(map1.get(s).contains(mission),
                    "result should be map of name of country and the missions that were executed there");
            }
        }

    }

    @Test
    void testGetTopNLeastExpensiveMissionsInvalidArguments() {
        MJTSpaceScanner mjtSpaceScanner = new MJTSpaceScanner(new StringReader(""), new StringReader(""), null);

        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.getTopNLeastExpensiveMissions(-1, null, null), "n cannot be non-positive integer");

        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.getTopNLeastExpensiveMissions(2, null, RocketStatus.STATUS_ACTIVE),
            "missionStatus cannot be null");

        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.getTopNLeastExpensiveMissions(2, MissionStatus.SUCCESS, null),
            "rocketStatus cannot be null");
    }

    @Test
    void testGetTopNLeastExpensiveMissions() {
        String missionsFile = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            0,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Fri Aug 07, 2020",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
            53,MHI,"LA-Y1, Tanegashima Space Center, Japan","Sun Feb 09, 2020",H-IIA 202 | IGS-Optical 7,StatusActive,"90.0 ",Success
            54,Arianespace,"Site 31/6, Baikonur Cosmodrome, Kazakhstan","Thu Feb 06, 2020",Soyuz 2.1b/Fregat | OneWeb #2,StatusActive,"48.5 ",Success
            271,CASC,"LC-2, Xichang Satellite Launch Center, China","Mon Feb 12, 2018",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success
            378,SpaceX,"SLC-4E, Vandenberg AFB, California, USA","Sat Jan 14, 2017",Falcon 9 Block 3 | Iridium-1,StatusRetired,"62.0 ",Success
            """;
        Mission mission1 = Mission.of(
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success");
        Mission mission2 = Mission.of(
            "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success");
        Mission mission3 = Mission.of(
            "53,MHI,\"LA-Y1, Tanegashima Space Center, Japan\",\"Sun Feb 09, 2020\",H-IIA 202 | IGS-Optical 7,StatusActive,\"90.0 \",Success");
        Mission mission4 = Mission.of(
            "54,Arianespace,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Thu Feb 06, 2020\",Soyuz 2.1b/Fregat | OneWeb #2,StatusActive,\"48.5 \",Success");
        Mission mission5 = Mission.of(
            "271,CASC,\"LC-2, Xichang Satellite Launch Center, China\",\"Mon Feb 12, 2018\",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success");
        Mission mission6 = Mission.of(
            "378,SpaceX,\"SLC-4E, Vandenberg AFB, California, USA\",\"Sat Jan 14, 2017\",Falcon 9 Block 3 | Iridium-1,StatusRetired,\"62.0 \",Success");

        MJTSpaceScanner mjtSpaceScanner =
            new MJTSpaceScanner(new StringReader(missionsFile), new StringReader(""), null);
        assertEquals(List.of(mission2, mission4, mission1),
            mjtSpaceScanner.getTopNLeastExpensiveMissions(3, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "result should be a list with the cheapest missions with the given status");
    }

    @Test
    void testGetMostDesiredLocationForMissionsPerCompany() {
        String location = "Target Location";
        String s = """
                        
            0,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Fri Aug 07, 2020",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
            2,SpaceX,"Pad A, Boca Chica, Texas, USA","Tue Aug 04, 2020",Starship Prototype | 150 Meter Hop,StatusActive,,Success
            3,Roscosmos,"Target Location","Thu Jul 30, 2020",Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,"65.0 ",Success
            4,ULA,"SLC-41, Cape Canaveral AFS, Florida, USA","Thu Jul 30, 2020",Atlas V 541 | Perseverance,StatusActive,"145.0 ",Success
            5,CASC,"LC-9, Taiyuan Satellite Launch Center, China","Sat Jul 25, 2020","Long March 4B | Ziyuan-3 03, Apocalypse-10 & NJU-HKU 1",StatusActive,"64.68 ",Success
            6,Roscosmos,"Target Location","Thu Jul 23, 2020",Soyuz 2.1a | Progress MS-15,StatusActive,"48.5 ",Success
            7,CASC,"Target Location","Thu Jul 23, 2020",Long March 5 | Tianwen-1,StatusActive,,Success
            8,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Mon Jul 20, 2020",Falcon 9 Block 5 | ANASIS-II,StatusActive,"50.0 ",Success
            9,JAXA,"LA-Y1, Tanegashima Space Center, Japan","Sun Jul 19, 2020",H-IIA 202 | Hope Mars Mission,StatusActive,"90.0 ",Prelaunch Failure
            10,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Wed Jul 15, 2020",Minotaur IV | NROL-129,StatusActive,"46.0 ",Success
            12,CASC,"Target Location","Thu Jul 09, 2020",Long March 3B/E | Apstar-6D,StatusActive,"29.15 ",Success
            13,IAI,"Pad 1, Palmachim Airbase, Israel","Mon Jul 06, 2020",Shavit-2 | Ofek-16,StatusActive,,Success
            14,CASC,"Target Location","Sat Jul 04, 2020",Long March 2D | Shiyan-6 02,StatusActive,"29.75 ",Success
            """;
        MJTSpaceScanner mjtSpaceScanner = new MJTSpaceScanner(new StringReader(s), new StringReader(""), null);
        Map<String, String> mostDesiredLocationPerContryMap =
            mjtSpaceScanner.getMostDesiredLocationForMissionsPerCompany();

        assertEquals("LC-39A, Kennedy Space Center, Florida, USA", mostDesiredLocationPerContryMap.get("SpaceX"),
            "result should be a map with entries consisting of name of the company name and their respective most successful missions' location ");
        assertEquals(location, mostDesiredLocationPerContryMap.get("Roscosmos"),
            "result should be a map with entries consisting of name of the company name and their respective most successful missions' location ");

    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyInvalidArguments() {
        MJTSpaceScanner mjtSpaceScanner = new MJTSpaceScanner(new StringReader(""), new StringReader(""), null);

        LocalDate from = LocalDate.of(2000, 12, 12);
        LocalDate to = LocalDate.of(1999, 12, 12);
        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(null, from),
            "from date cannot be null");
        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(to, null), "to date cannot be null");

        assertThrows(TimeFrameMismatchException.class,
            () -> mjtSpaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(from, to),
            "to date cannot be before from date");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompany() {
        String missionsFile = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            0,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Fri Aug 07, 2020",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
            53,MHI,"LA-Y1, Tanegashima Space Center, Japan","Sun Feb 09, 2020",H-IIA 202 | IGS-Optical 7,StatusActive,"90.0 ",Success
            54,Arianespace,"Site 31/6, Baikonur Cosmodrome, Kazakhstan","Thu Feb 06, 2020",Soyuz 2.1b/Fregat | OneWeb #2,StatusActive,"48.5 ",Success
            271,CASC,"LC-2, Xichang Satellite Launch Center, China","Mon Feb 12, 2018",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success
            378,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Sat Jan 14, 2017",Falcon 9 Block 3 | Iridium-1,StatusRetired,"62.0 ",Success
            379,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Tue Oct 31, 1995",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            380,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Tue Oct 31, 1995",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            381,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Tue Oct 31, 1995",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            382,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Tue Oct 31, 1995",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            383,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Tue Oct 31, 1995",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            """;
        MJTSpaceScanner mjtSpaceScanner =
            new MJTSpaceScanner(new StringReader(missionsFile), new StringReader(""), null);
        assertEquals("LC-39A, Kennedy Space Center, Florida, USA",
            mjtSpaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(LocalDate.of(2005, 12, 12),
                LocalDate.of(2021, 12, 12)).get("SpaceX"),
            "result should be a map with entries consisting of name of the company name and their respective most successful missions' location in the given time frame");
    }

    @Test
    void testGetTopNTallestRocketsInvalidArguments() {
        MJTSpaceScanner mjtSpaceScanner = new MJTSpaceScanner(new StringReader(""), new StringReader(""), null);

        assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getTopNTallestRockets(-3),
            "n cannot be a negative integer");
        assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getTopNTallestRockets(0),
            "n cannot be zero");
    }

    @Test
    void testGetTopNTallestRockets() {
        String rocketsFile = """
                        
            0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m
            1,Tsyklon-4M,https://en.wikipedia.org/wiki/Cyclone-4M,38.7 m
            2,Unha-2,https://en.wikipedia.org/wiki/Unha,28.0 m
            3,Unha-3,https://en.wikipedia.org/wiki/Unha,32.0 m
            4,Vanguard,https://en.wikipedia.org/wiki/Vanguard_(rocket),23.0 m
            5,Vector-H,https://en.wikipedia.org/wiki/Vector-H,18.3 m
            6,Vector-R,https://en.wikipedia.org/wiki/Vector-R,13.0 m
            """;
        Rocket rocket1 = Rocket.of("0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m");
        Rocket rocket2 = Rocket.of("1,Tsyklon-4M,https://en.wikipedia.org/wiki/Cyclone-4M,38.7 m");
        Rocket rocket3 = Rocket.of("3,Unha-3,https://en.wikipedia.org/wiki/Unha,32.0 m");

        MJTSpaceScanner mjtSpaceScanner =
            new MJTSpaceScanner(new StringReader(""), new StringReader(rocketsFile), null);
        List<Rocket> rockets = List.of(rocket1, rocket2, rocket3);
        assertEquals(rockets, mjtSpaceScanner.getTopNTallestRockets(3),
            "result should be a list with the tallest buildings");
    }

    @Test
    void testGetWikiPageForRocket() {
        String rocketsFile = """
                        
            0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m
            1,Tsyklon-4M,https://en.wikipedia.org/wiki/Cyclone-4M,38.7 m
            2,Unha-2,https://en.wikipedia.org/wiki/Unha,28.0 m
            3,Unha-3,https://en.wikipedia.org/wiki/Unha,32.0 m
            4,Vanguard,https://en.wikipedia.org/wiki/Vanguard_(rocket),23.0 m
            5,Vector-H,https://en.wikipedia.org/wiki/Vector-H,18.3 m
            6,Vector-R,https://en.wikipedia.org/wiki/Vector-R,13.0 m
            7,NoWikiRocket,,20.0 m
            """;
        Rocket rocket1 = Rocket.of("0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m");
        Rocket rocket2 = Rocket.of("1,Tsyklon-4M,https://en.wikipedia.org/wiki/Cyclone-4M,38.7 m");
        Rocket rocket3 = Rocket.of("2,Unha-2,https://en.wikipedia.org/wiki/Unha,28.0 m");
        Rocket rocket4 = Rocket.of("3,Unha-3,https://en.wikipedia.org/wiki/Unha,32.0 m");
        Rocket rocket5 = Rocket.of("4,Vanguard,https://en.wikipedia.org/wiki/Vanguard_(rocket),23.0 m");
        Rocket rocket6 = Rocket.of("5,Vector-H,https://en.wikipedia.org/wiki/Vector-H,18.3 m");
        Rocket rocket7 = Rocket.of("6,Vector-R,https://en.wikipedia.org/wiki/Vector-R,13.0 m");
        Rocket rocket8 = Rocket.of("7,NoWikiRocket,,20.0 m");
        Map<String, Optional<String>> rocketsAndWikiMap = new HashMap<>();
        rocketsAndWikiMap.put(rocket1.name(), rocket1.wiki());
        rocketsAndWikiMap.put(rocket2.name(), rocket2.wiki());
        rocketsAndWikiMap.put(rocket3.name(), rocket3.wiki());
        rocketsAndWikiMap.put(rocket4.name(), rocket4.wiki());
        rocketsAndWikiMap.put(rocket5.name(), rocket5.wiki());
        rocketsAndWikiMap.put(rocket6.name(), rocket6.wiki());
        rocketsAndWikiMap.put(rocket7.name(), rocket7.wiki());
        rocketsAndWikiMap.put(rocket8.name(), rocket8.wiki());

        MJTSpaceScanner mjtSpaceScanner =
            new MJTSpaceScanner(new StringReader(""), new StringReader(rocketsFile), null);
        for (Map.Entry<String, Optional<String>> entry : mjtSpaceScanner.getWikiPageForRocket().entrySet()) {
            assertEquals(rocketsAndWikiMap.get(entry.getKey()), entry.getValue(),
                "Result should be a map containing a name of the rocket and an optional with the wiki for the rocket should it be contained in the data set");
        }

    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsInvalidArguments() {
        MJTSpaceScanner mjtSpaceScanner = new MJTSpaceScanner(new StringReader(""), new StringReader(""), null);
        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(-1, MissionStatus.SUCCESS,
                RocketStatus.STATUS_ACTIVE), "n cannot be a negative integer");
        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(0, MissionStatus.SUCCESS,
                RocketStatus.STATUS_ACTIVE), "n cannot be zero");
        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(3, null,
                RocketStatus.STATUS_ACTIVE), "missionStatus cannot be null");
        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(3, MissionStatus.SUCCESS,
                null), "rocketStatus cannot be null");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissions() {
        String missionsFile = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            0,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Fri Aug 07, 2020",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
            2,SpaceX,"Pad A, Boca Chica, Texas, USA","Tue Aug 04, 2020",Starship Prototype | 150 Meter Hop,StatusActive,,Success
            3,Roscosmos,"Site 200/39, Baikonur Cosmodrome, Kazakhstan","Thu Jul 30, 2020",Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,"65.0 ",Success
            4,ULA,"SLC-41, Cape Canaveral AFS, Florida, USA","Thu Jul 30, 2020",Atlas V 541 | Perseverance,StatusActive,"145.0 ",Success
            5,CASC,"LC-9, Taiyuan Satellite Launch Center, China","Sat Jul 25, 2020","Long March 4B | Ziyuan-3 03, Apocalypse-10 & NJU-HKU 1",StatusActive,"64.68 ",Success
            6,Roscosmos,"Site 31/6, Baikonur Cosmodrome, Kazakhstan","Thu Jul 23, 2020",Soyuz 2.1a | Progress MS-15,StatusActive,"48.5 ",Success
            7,CASC,"LC-101, Wenchang Satellite Launch Center, China","Thu Jul 23, 2020",Long March 5 | Tianwen-1,StatusActive,,Success
            8,SpaceX,"SLC-40, Cape Canaveral AFS, Florida, USA","Mon Jul 20, 2020",Falcon 9 Block 5 | ANASIS-II,StatusActive,"50.0 ",Success
            9,JAXA,"LA-Y1, Tanegashima Space Center, Japan","Sun Jul 19, 2020",H-IIA 202 | Hope Mars Mission,StatusActive,"90.0 ",Success
            10,Northrop,"LP-0B, Wallops Flight Facility, Virginia, USA","Wed Jul 15, 2020",Minotaur IV | NROL-129,StatusActive,"46.0 ",Success
            11,ExPace,"Site 95, Jiuquan Satellite Launch Center, China","Fri Jul 10, 2020","Kuaizhou 11 | Jilin-1 02E, CentiSpace-1 S2",StatusActive,"28.3 ",Failure
            12,CASC,"LC-3, Xichang Satellite Launch Center, China","Thu Jul 09, 2020",Long March 3B/E | Apstar-6D,StatusActive,"29.15 ",Success
            """;

        String rocketsFile = """

            103,Atlas V 541,https://en.wikipedia.org/wiki/Atlas_V,62.2 m
            182,H-IIA 202,https://en.wikipedia.org/wiki/H-IIA,53.0 m
            294,Proton-M/Briz-M,https://en.wikipedia.org/wiki/Proton-M,58.2 m
            """;
        MJTSpaceScanner mjtSpaceScanner =
            new MJTSpaceScanner(new StringReader(missionsFile), new StringReader(rocketsFile), null);
        List<String> rocketsWikis =
            List.of("https://en.wikipedia.org/wiki/Atlas_V",
                "https://en.wikipedia.org/wiki/H-IIA",
                "https://en.wikipedia.org/wiki/Proton-M");
        assertEquals(rocketsWikis,
            mjtSpaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(3, MissionStatus.SUCCESS,
                RocketStatus.STATUS_ACTIVE),
            "Result should contain wikis for the rockets used in the most expensive missions with the provided status");


    }

    @Test
    void testSaveMostReliableRocketNameInvalidArguments() {
        MJTSpaceScanner mjtSpaceScanner = new MJTSpaceScanner(new StringReader(""), new StringReader(""), null);
        LocalDate from = LocalDate.of(2000, 12, 12);
        LocalDate to = LocalDate.of(1999, 12, 12);
        assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.saveMostReliableRocket(null, from, to),
            "Output stream cannot be null");
        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.saveMostReliableRocket(new ByteArrayOutputStream(100), null, to),
            "From date cannot be null");
        assertThrows(IllegalArgumentException.class,
            () -> mjtSpaceScanner.saveMostReliableRocket(new ByteArrayOutputStream(100), from, null),
            "To date cannot be null");
        assertThrows(TimeFrameMismatchException.class,
            () -> mjtSpaceScanner.saveMostReliableRocket(new ByteArrayOutputStream(100), from, to),
            "From date cannot be after to date");
    }

    @Test
    void testSaveMostReliableRocketName() throws NoSuchAlgorithmException, IOException, CipherException {
        int keySize = 128;
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize);
        SecretKey secretKey = keyGenerator.generateKey();
        Rijndael rijndael = new Rijndael(secretKey);

        String missionsFile = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            0,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Fri Aug 07, 2020",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
            53,MHI,"LA-Y1, Tanegashima Space Center, Japan","Sun Feb 09, 2020",H-IIA 202 | IGS-Optical 7,StatusActive,"90.0 ",Success
            54,Arianespace,"Site 31/6, Baikonur Cosmodrome, Kazakhstan","Thu Feb 06, 2020",Soyuz 2.1b/Fregat | OneWeb #2,StatusActive,"48.5 ",Success
            271,CASC,"LC-2, Xichang Satellite Launch Center, China","Mon Feb 12, 2018",Long March 3B/YZ-1 | BeiDou-3 M3 & M4,StatusActive,,Success
            378,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Sat Jan 14, 2017",Falcon 9 Block 3 | Iridium-1,StatusRetired,"62.0 ",Success
            379,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Tue Oct 31, 1995",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            380,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Tue Oct 31, 1995",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            381,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Tue Oct 31, 1995",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            382,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Tue Oct 31, 1995",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            383,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Tue Oct 31, 1995",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
            """;
        MJTSpaceScanner mjtSpaceScanner =
            new MJTSpaceScanner(new StringReader(missionsFile), new StringReader(""), secretKey);

        ByteArrayOutputStream encryptionDestination = new ByteArrayOutputStream();
        ByteArrayOutputStream DecryptionTarget = new ByteArrayOutputStream();
        mjtSpaceScanner.saveMostReliableRocket(encryptionDestination, LocalDate.of(1900, 12, 12),
            LocalDate.of(2020, 12, 12));

        rijndael.decrypt(new ByteArrayInputStream(encryptionDestination.toByteArray()), DecryptionTarget);
        encryptionDestination.close();

        assertEquals("Falcon 9 Block 5", DecryptionTarget.toString(),
            "rocket name should be encrypted to the provided stream with the provided key");
        DecryptionTarget.close();
    }
}
