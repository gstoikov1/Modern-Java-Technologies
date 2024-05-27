package bg.sofia.uni.fmi.mjt.itinerary;

import bg.sofia.uni.fmi.mjt.itinerary.exception.CityNotKnownException;
import bg.sofia.uni.fmi.mjt.itinerary.exception.NoPathToDestinationException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.SequencedCollection;

public class RideRight implements ItineraryPlanner {
    private List<Journey> schedule;
    private HashMap<City, Node> graph;

    private void initialiseGraph() {
        this.graph = new HashMap<>();
        for (Journey j : schedule) {
            if (!graph.containsKey(j.from())) {
                Node node = new Node(j.from());
                graph.put(j.from(), node);
            }
            if (!graph.containsKey(j.to())) {
                Node node = new Node(j.to());
                graph.put(j.to(), node);
            }
        }
        for (Journey j : schedule) {
            graph.get(j.from()).neighbors.add(new Node.Edge(j));
        }
    }

    public RideRight(List<Journey> schedule) {
        this.schedule = schedule;
    }

    @Override
    public SequencedCollection<Journey> findCheapestPath(City start, City destination, boolean allowTransfer)
        throws CityNotKnownException, NoPathToDestinationException {
        initialiseGraph();

        if (!graph.containsKey(start) || !graph.containsKey(destination)) {
            throw new CityNotKnownException("Starting point or destination point not known");
        }
        Node result = null;
        if (allowTransfer) {
            result = Algorithm.aStar(start, destination, graph);
        } else {
            BigDecimal minPrice = BigDecimal.valueOf(-1);
            for (Journey j : schedule) {
                if (j.from().equals(start) && j.to().equals(destination)
                    && (minPrice.equals(BigDecimal.valueOf(-1)) || minPrice.compareTo(j.price()) < 0)) {
                    result = graph.get(j.to());
                    result.parentJourney = j;
                    result.parent = graph.get(j.from());
                }
            }
        }
        if (result == null) {
            throw new NoPathToDestinationException("No existing path to destination");
        } else {
            return Algorithm.getPath(result);
        }
    }
}
