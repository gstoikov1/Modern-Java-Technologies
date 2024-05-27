package client;

import client.recipe.criterion.HealthLabel;
import client.recipe.criterion.MealType;
import client.exception.InternalServerErrorException;
import client.exception.UnauthorizedAccessException;
import client.recipe.Recipe;

import java.util.Collection;
import java.util.List;

public interface RecipeSearcherAPI {

    /**
     * Returns a list of recipes associated with that keyword
     *
     * @param keyword the keyword to be searched
     * @throws IllegalArgumentException     if keyword is null
     * @throws UnauthorizedAccessException  If the combination of app_id and app_key is not a valid API registration
     * @throws InternalServerErrorException If the connection to the server is not possible
     */
    List<Recipe> searchRecipeSingleKeyword(String keyword)
        throws UnauthorizedAccessException, InternalServerErrorException;

    /**
     * Returns a list of recipes associated with that mealType
     *
     * @param mealType the mealType to be searched
     * @throws IllegalArgumentException     if mealType is null
     * @throws UnauthorizedAccessException  If the combination of app_id and app_key is not a valid API registration
     * @throws InternalServerErrorException If the connection to the server is not possible
     */
    List<Recipe> searchRecipeSingleMealType(MealType mealType)
        throws UnauthorizedAccessException, InternalServerErrorException;

    /**
     * Returns a list of recipes associated with that mealType
     *
     * @param healthLabel the healthLabel to be searched
     * @throws IllegalArgumentException     if healthLabel is null
     * @throws UnauthorizedAccessException  If the combination of app_id and app_key is not a valid API registration
     * @throws InternalServerErrorException If the connection to the server is not possible
     */
    List<Recipe> searchRecipeSingleHealthLabel(HealthLabel healthLabel)
        throws UnauthorizedAccessException, InternalServerErrorException;

    /**
     * Returns a list of recipes associated with that list of keywords
     *
     * @param keywords the keywords to be searched
     * @throws IllegalArgumentException     if keywords is null
     * @throws UnauthorizedAccessException  If the combination of app_id and app_key is not a valid API registration
     * @throws InternalServerErrorException If the connection to the server is not possible
     */
    List<Recipe> searchRecipeKeywords(Collection<String> keywords)
        throws UnauthorizedAccessException, InternalServerErrorException;

    /**
     * Returns a list of recipes associated with that list of mealTypes
     *
     * @param mealTypes the mealTypes to be searched
     * @throws IllegalArgumentException     if mealTypes is null
     * @throws UnauthorizedAccessException  If the combination of app_id and app_key is not a valid API registration
     * @throws InternalServerErrorException If the connection to the server is not possible
     */
    List<Recipe> searchRecipeMealTypes(Collection<MealType> mealTypes)
        throws UnauthorizedAccessException, InternalServerErrorException;

    /**
     * Returns a list of recipes associated with that list of healthLabels
     *
     * @param healthLabels the healthLabels to be searched
     * @throws IllegalArgumentException     if healthLabels is null
     * @throws UnauthorizedAccessException  If the combination of app_id and app_key is not a valid API registration
     * @throws InternalServerErrorException If the connection to the server is not possible
     */
    List<Recipe> searchRecipeHealthLabels(Collection<HealthLabel> healthLabels)
        throws UnauthorizedAccessException, InternalServerErrorException;

    /**
     * Returns a list of recipes associated with the lists of mealTypes and healthLabels
     *
     * @param mealTypes    the mealTypes to be searched
     * @param healthLabels the healthLabels to be searched
     * @throws IllegalArgumentException     If either mealTypes or healthLabels is null
     * @throws UnauthorizedAccessException  If the combination of app_id and app_key is not a valid API registration
     * @throws InternalServerErrorException If the connection to the server is not possible
     */
    List<Recipe> searchRecipeMealTypeHealthLabel(Collection<MealType> mealTypes,
                                                 Collection<HealthLabel> healthLabels)
        throws UnauthorizedAccessException, InternalServerErrorException;

    /**
     * Returns a list of recipes associated with the lists of keywords, mealTypes and healthLabels
     *
     * @param keywords     the keywords to be searched
     * @param mealTypes    the mealTypes to be searched
     * @param healthLabels the healthLabels to be searched
     * @throws IllegalArgumentException     If either one of keywords, mealTypes or healthLabels is null
     * @throws UnauthorizedAccessException  If the combination of app_id and app_key is not a valid API registration
     * @throws InternalServerErrorException If the connection to the server is not possible
     */
    List<Recipe> searchRecipeAllCriterion(Collection<String> keywords, Collection<MealType> mealTypes,
                                          Collection<HealthLabel> healthLabels)
        throws UnauthorizedAccessException, InternalServerErrorException;

}
