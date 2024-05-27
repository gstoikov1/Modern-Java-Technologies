package bg.sofia.uni.fmi.mjt.itinerary;

public record Location(int x, int y) {
    int getDistance(Location loc) {
        return Math.abs(this.x - loc.x) + Math.abs(this.y - loc.y);
    }
}