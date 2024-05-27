package bg.sofia.uni.fmi.mjt.itinerary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.SequencedCollection;
import java.util.List;
import java.util.Collections;

public class Algorithm {

    public static Node aStar(City from, City to, HashMap<City, Node> nodes) {
        PriorityQueue<Node> closedList = new PriorityQueue<>();
        PriorityQueue<Node> openList = new PriorityQueue<>();

        Node start = nodes.get(from);
        Node target = nodes.get(to);
        start.f = start.g + start.calculateHeuristic(target);

        openList.add(start);
        while (!openList.isEmpty()) {
            Node n = openList.peek();
            if (n.equals(target)) {
                return n;
            }

            for (Node.Edge edge : n.neighbors) {

                Node m = nodes.get(edge.journey.to());
                double totalWeight = n.g + edge.weight;

                if (!openList.contains(m) && !closedList.contains(m)) {
                    m.parent = n;
                    m.parentJourney = edge.journey;
                    m.g = totalWeight;
                    m.f = m.g + m.calculateHeuristic(target);
                    openList.add(m);
                } else {
                    if (totalWeight < m.g) {
                        m.parent = n;
                        m.parentJourney = edge.journey;
                        m.g = totalWeight;
                        m.f = m.g + m.calculateHeuristic(target);

                        if (closedList.contains(m)) {
                            closedList.remove(m);
                            openList.add(m);
                        }
                    }
                }
            }
            openList.remove(n);
            closedList.add(n);
        }
        return null;
    }

    public static SequencedCollection<Journey> getPath(Node target) {
        Node n = target;

        if (n == null) {
            return null;
        }

        List<Journey> ids = new ArrayList<>();

        while (n.parent != null) {
            ids.add(n.parentJourney);
            n = n.parent;
        }
        Collections.reverse(ids);
        return ids;
    }
}
