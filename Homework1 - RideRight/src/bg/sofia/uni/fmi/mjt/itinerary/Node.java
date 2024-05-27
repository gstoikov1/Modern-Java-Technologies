package bg.sofia.uni.fmi.mjt.itinerary;

import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable<Node> {
    City city;

    public Node parent = null;
    public Journey parentJourney = null;
    public List<Edge> neighbors;

    public double f = Double.MIN_VALUE;
    public double g = Double.MIN_VALUE;

    public Node(City city) {
        this.city = city;
        this.neighbors = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        return city.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        return this.city.equals(((Node) o).city);
    }

    @Override
    public String toString() {
        return this.city.toString();
    }

    @Override
    public int compareTo(Node n) {
        return Double.compare(this.f, n.f);
    }

    public static class Edge {
        Edge(Journey journey) {
            this.journey = journey;
            this.weight =
                (journey.price().add(journey.price().multiply(journey.vehicleType().getGreenTax()))).doubleValue();
        }

        public Journey journey;
        public double weight;

        @Override
        public String toString() {
            return this.journey.toString();
        }
    }

    public void addBranch(Journey journey) {
        Edge newEdge = new Edge(journey);
        neighbors.add(newEdge);
    }

    public double calculateHeuristic(Node target) {
        final int pricePerKM = 20;
        final double conversionFromMtoKM = 1000.0;
        return (this.city.location().getDistance(target.city.location()) / conversionFromMtoKM) * pricePerKM;
    }
}