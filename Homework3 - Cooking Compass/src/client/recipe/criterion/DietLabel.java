package client.recipe.criterion;

public enum DietLabel {
    BALANCED("balanced"),
    HIGH_FIBER("high-fiber"),
    HIGH_PROTEIN("high-protein"),
    LOW_CARB("low-carb"),
    LOW_FAT("low-fat"),
    LOW_SODIUM("low-sodium");

    private final String label;

    DietLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
