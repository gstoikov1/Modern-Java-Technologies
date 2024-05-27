package dungeons.entities.player;

import dungeons.treasure.Treasure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Inventory {
    private static final int INVENTORY_CAPACITY = 5;
    private final List<Treasure> treasures;

    public Inventory() {
        treasures = new ArrayList<>();
    }

    public void putTreasure(Treasure treasure) {
        treasures.add(treasure);
    }

    public boolean isEmpty() {
        return treasures.isEmpty();
    }

    public boolean isFull() {
        return treasures.size() == INVENTORY_CAPACITY;
    }

    public List<Treasure> getTreasures() {
        return treasures;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        int counter = 0;
        for (Treasure treasure : treasures) {
            if (treasure != null) {
                stringBuilder.append(Integer.toString(counter++)).append(".").append(treasure)
                    .append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }

    public Treasure getTreasureAtIndex(int index) {
        if (index >= treasures.size()) {
            return null;
        }
        return treasures.get(index);
    }

    public void removeTreasureAtIndex(int index) {
        if (index >= treasures.size() || index < 0) {
            return;
        }
        treasures.remove(index);
    }

    public void removeRandomTreasure() {
        if (treasures.isEmpty()) {
            return;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(treasures.size());
        treasures.remove(randomIndex);
    }

}
