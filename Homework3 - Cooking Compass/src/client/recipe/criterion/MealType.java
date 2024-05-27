package client.recipe.criterion;

public enum MealType {
    BREAKFAST("breakfast"),
    BRUNCH("brunch"),
    LUNCH_DINNER("lunch_dinner"),
    SNACK("snack"),
    TEATIME("teatime");

    private final String type;

    MealType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
