package dungeons.entities.position;

public record GamePosition(int x, int y) {
    public int getDistance(GamePosition gamePosition) {
        return Math.abs(x - gamePosition.x) + Math.abs(y - gamePosition.y);
    }
}
