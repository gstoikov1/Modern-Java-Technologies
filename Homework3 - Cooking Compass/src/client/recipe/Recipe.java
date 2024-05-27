package client.recipe;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import client.recipe.criterion.CuisineType;
import client.recipe.criterion.DietLabel;
import client.recipe.criterion.DishType;
import client.recipe.criterion.HealthLabel;
import client.recipe.criterion.MealType;

import java.util.List;

public class Recipe {

    private String label;
    private List<HealthLabel> healthLabels;
    private List<DietLabel> dietLabels;
    private double totalWeight;
    @SerializedName("dishType")
    private List<DishType> dishTypes;
    @SerializedName("cuisineType")
    private List<CuisineType> cuisineTypes;
    @SerializedName("mealType")
    private List<MealType> mealTypes;
    @SerializedName("ingredientLines")
    private List<String> ingredients;

    public String getLabel() {
        return label;
    }

    public List<HealthLabel> getHealthLabels() {
        return healthLabels;
    }

    public List<DietLabel> getDietLabels() {
        return dietLabels;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public List<DishType> getDishTypes() {
        return dishTypes;
    }

    public List<CuisineType> getCuisineTypes() {
        return cuisineTypes;
    }

    public List<MealType> getMealTypes() {
        return mealTypes;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    // Custom deserializer for CuisineType enum
    public static class CuisineTypeDeserializer implements JsonDeserializer<CuisineType> {
        @Override
        public CuisineType deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                       JsonDeserializationContext context) {
            return deserializeEnum(CuisineType.class, json);
        }
    }

    // Custom deserializer for DishType enum
    public static class DishTypeDeserializer implements JsonDeserializer<DishType> {
        @Override
        public DishType deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                    JsonDeserializationContext context) {
            return deserializeEnum(DishType.class, json);
        }
    }

    // Custom deserializer for HealthLabel enum
    public static class HealthLabelDeserializer implements JsonDeserializer<HealthLabel> {
        @Override
        public HealthLabel deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                       JsonDeserializationContext context) {
            return deserializeEnum(HealthLabel.class, json);
        }
    }

    // Custom deserializer for MealType enum
    public static class MealTypeDeserializer implements JsonDeserializer<MealType> {
        @Override
        public MealType deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                    JsonDeserializationContext context) {
            return deserializeEnum(MealType.class, json);
        }
    }

    // Custom deserializer for DietLabel enum
    public static class DietLabelDeserializer implements JsonDeserializer<DietLabel> {
        @Override
        public DietLabel deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                     JsonDeserializationContext context) {
            return deserializeEnum(DietLabel.class, json);
        }
    }

    private static <T extends Enum<T>> T deserializeEnum(Class<T> enumClass, JsonElement json) {
        String enumString = json.getAsString();
        try {
            return Enum.valueOf(enumClass, formatStringForEnumUsage(enumString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + enumClass.getSimpleName() + ": " + enumString);
        }
    }

    private static String formatStringForEnumUsage(String enumString) {
        return enumString
            .toUpperCase()
            .replaceAll(" ", "_")
            .replaceAll("-", "_")
            .replaceAll("/", "_");
    }
}
