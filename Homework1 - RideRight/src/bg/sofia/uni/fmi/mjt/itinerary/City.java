package bg.sofia.uni.fmi.mjt.itinerary;

public record City(String name, Location location) {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        return this.name.equals(((City) o).name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + location.x() + ", " + location.y() + ")";
    }
}