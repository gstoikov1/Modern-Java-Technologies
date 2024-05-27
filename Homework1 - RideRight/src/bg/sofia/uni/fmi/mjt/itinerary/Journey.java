package bg.sofia.uni.fmi.mjt.itinerary;

import bg.sofia.uni.fmi.mjt.itinerary.vehicle.VehicleType;

import java.math.BigDecimal;

public record Journey(VehicleType vehicleType, City from, City to, BigDecimal price) {
    public String toString() {
        return "[" + vehicleType.toString() + ", " + from.toString() + ", " + to.toString() + ", " + price.toString() +
            "]";
    }
}