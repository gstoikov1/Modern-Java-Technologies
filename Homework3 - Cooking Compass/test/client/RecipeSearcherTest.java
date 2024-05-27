package client;

import client.recipe.criterion.HealthLabel;
import client.recipe.criterion.MealType;
import client.exception.InternalServerErrorException;
import client.exception.UnauthorizedAccessException;
import client.recipe.Recipe;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecipeSearcherTest {
    private final static String API_KEY = "TEST_API_KEY";
    private final static String API_ID = "TEST_API_ID";
    private static final String URI_PREFIX =
        "https://api.edamam.com/api/recipes/v2?type=public" + "&app_id=" + API_ID + "&app_key=" + API_KEY;
    private static final String MEAL_TYPE_PREFIX = "&mealType=";
    private static final String QUERY_PREFIX = "&q=";
    private static final String HEALTH_LABEL_PREFIX = "&health=";

    private static final int ERROR_CODE_UNAUTHORIZED_ACCESS = 401;
    private static final int ERROR_CODE_INTERNAL_SERVER_ERROR_FROM = 500;
    private static final int ERROR_CODE_INTERNAL_SERVER_ERROR_TO = 505;


    private static RecipeSearcher recipeSearcher;
    private static HttpClient httpClientMock;
    private static CustomHttpResponse httpResponse;

    @BeforeAll
    static void initialise() {
        recipeSearcher = new RecipeSearcher(API_ID, API_KEY);
        httpClientMock = mock(HttpClient.class);
        recipeSearcher.setHttpClient(httpClientMock);
    }

    @BeforeEach
    void initialiseHttpResponse() {
        httpResponse = new CustomHttpResponse();
    }

    @Test
    void testSearchRecipeBySingleCriterionMealTypeSNACK()
        throws IOException, InterruptedException, UnauthorizedAccessException, InternalServerErrorException {
        String expectedRequest = URI_PREFIX + MEAL_TYPE_PREFIX + MealType.SNACK.getType();
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(expectedRequest)).build();
        httpResponse.setBody("{\"hits\": [ { \"recipe\": { \"mealType\": [ \"snack\"] } }]} ");

        when(httpClientMock.send(httpRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);

        Recipe recipe = recipeSearcher.searchRecipeSingleMealType(MealType.SNACK).get(0);
        assertTrue(recipe.getMealTypes().contains(MealType.SNACK),
            "Recipes should have MealType.SNACK in their list of MealTypes");
    }

    @Test
    void testSearchRecipeBySingleCriterionKeywordFish()
        throws IOException, InterruptedException, UnauthorizedAccessException, InternalServerErrorException {
        String expectedRequest = URI_PREFIX + QUERY_PREFIX + "Fish";
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(expectedRequest)).build();
        httpResponse.setBody("{\"hits\": [ { \"recipe\": { \"label\": \"Fish\" } }]} ");
        when(httpClientMock.send(httpRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);
        Recipe recipe = recipeSearcher.searchRecipeKeywords(Set.of("Fish")).get(0);
        assertTrue(recipe.getLabel().contains("Fish"), "Recipes should have the word Fish in their label");

    }

    @Test
    void testSearchRecipeBySingleCriterionHealthLabelDAIRY_FREE()
        throws IOException, InterruptedException, UnauthorizedAccessException, InternalServerErrorException {
        String expectedRequest = URI_PREFIX + HEALTH_LABEL_PREFIX + HealthLabel.DAIRY_FREE.getLabel();
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(expectedRequest)).build();
        httpResponse.setBody("{\"hits\": [ { \"recipe\": { \"healthLabels\": [\"Dairy-Free\" ] } }]} ");
        when(httpClientMock.send(httpRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);
        Recipe recipe = recipeSearcher.searchRecipeSingleHealthLabel(HealthLabel.DAIRY_FREE).get(0);
        Set<HealthLabel> expected = new HashSet<>();
        expected.add(HealthLabel.DAIRY_FREE);

        Set<HealthLabel> actual = new HashSet<>(recipe.getHealthLabels());

        assertEquals(expected, actual, "Recipes should have HealthLabel.DAIRY_FREE in their list of HealtLabels");
    }

    @Test
    void testSearchRecipeByAllCriterionOnePageResults()
        throws IOException, InterruptedException, UnauthorizedAccessException, InternalServerErrorException {
        String expectedRequest1 =
            URI_PREFIX + QUERY_PREFIX + "Fish" + MEAL_TYPE_PREFIX +
                MealType.BREAKFAST.getType() + HEALTH_LABEL_PREFIX +
                HealthLabel.DAIRY_FREE.getLabel() + HEALTH_LABEL_PREFIX + HealthLabel.MEDITERRANEAN.getLabel();
        HttpRequest httpRequest1 = HttpRequest.newBuilder().uri(URI.create(expectedRequest1)).build();

        String expectedRequest2 =
            URI_PREFIX + QUERY_PREFIX + "Fish" + MEAL_TYPE_PREFIX +
                MealType.BREAKFAST.getType() + HEALTH_LABEL_PREFIX +
                HealthLabel.MEDITERRANEAN.getLabel() + HEALTH_LABEL_PREFIX + HealthLabel.DAIRY_FREE.getLabel();
        HttpRequest httpRequest2 = HttpRequest.newBuilder().uri(URI.create(expectedRequest2)).build();

        String body = "{\"hits\": [ { \"recipe\": " +
            "{ \"healthLabels\": [\"Dairy-Free\", \"Mediterranean\"], " +
            "\"mealType\": [\"breakfast\"]," +
            "\"label\" : \"Fish\", " +
            "\"cuisineType\" : [\"french\"]," +
            "\"dishType\" : [\"condiments and sauces\"]," +
            "\"dietLabels\" : [\"High-Fiber\", \"Low-Carb\"]" +
            "} }]} ";

        httpResponse.setBody(body);
        when(httpClientMock.send(httpRequest1, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);
        when(httpClientMock.send(httpRequest2, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);

        Recipe recipe = recipeSearcher.searchRecipeAllCriterion(Set.of("Fish"), Set.of(MealType.BREAKFAST),
            Set.of(HealthLabel.DAIRY_FREE, HealthLabel.MEDITERRANEAN)).get(0);

        Set<MealType> expectedMealTypes = new HashSet<>();
        expectedMealTypes.add(MealType.BREAKFAST);

        Set<HealthLabel> expectedHealthLabels = new HashSet<>();
        expectedHealthLabels.add(HealthLabel.DAIRY_FREE);
        expectedHealthLabels.add(HealthLabel.MEDITERRANEAN);

        Set<MealType> actualMealTypes = new HashSet<>(recipe.getMealTypes());
        Set<HealthLabel> actualHealthLabels = new HashSet<>(recipe.getHealthLabels());
        assertEquals(expectedMealTypes, actualMealTypes,
            "Recipes should have all the MealTypes given as arguments");
        assertEquals(expectedHealthLabels, actualHealthLabels,
            "Recipes should have all the HealthLabels given as arguments");
    }

    @Test
    void testInternalServerErrorException() throws IOException, InterruptedException {
        String expectedRequest = URI_PREFIX + QUERY_PREFIX + "Fish";
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(expectedRequest)).build();
        httpResponse.setStatusCode(ERROR_CODE_INTERNAL_SERVER_ERROR_FROM);

        when(httpClientMock.send(httpRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);

        assertThrows(InternalServerErrorException.class, () -> recipeSearcher.searchRecipeSingleKeyword("Fish"),
            "Responses with status code between 500 and 505 should throw InternalServerErrorException");
    }

    @Test
    void testUnauthorizedAccessException() throws IOException, InterruptedException {
        String expectedRequest = URI_PREFIX + QUERY_PREFIX + "Fish";
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(expectedRequest)).build();
        httpResponse.setStatusCode(ERROR_CODE_UNAUTHORIZED_ACCESS);

        when(httpClientMock.send(httpRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);

        assertThrows(UnauthorizedAccessException.class, () -> recipeSearcher.searchRecipeSingleKeyword("Fish"),
            "Response with status code 401 should throw UnauthorizedAccessException");
    }

    @Test
    void testSearchRecipeByAllCriterionSecondPage()
        throws IOException, InterruptedException, UnauthorizedAccessException, InternalServerErrorException {

        String expectedRequest =
            URI_PREFIX + QUERY_PREFIX + "Fish";
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(expectedRequest)).build();

        String body = "{\"hits\": [ { \"recipe\": " +
            "{ \"healthLabels\": [\"Dairy-Free\", \"Mediterranean\"], " +
            "\"mealType\": [\"breakfast\"]," +
            "\"label\" : \"Fish\", " +
            "\"cuisineType\" : [\"french\"]," +
            "\"dishType\" : [\"condiments and sauces\"]," +
            "\"dietLabels\" : [\"High-Fiber\", \"Low-Carb\"]" +
            "} }], \"_links\": {\"next\": {\"href\": \"" + URI_PREFIX + "&secondPage" + "\"}}}";

        httpResponse.setBody(body);
        when(httpClientMock.send(httpRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);

        String bodySecondPage = "{\"hits\": [ { \"recipe\": " +
            "{ \"healthLabels\": [\"Dairy-Free\", \"Mediterranean\"], " +
            "\"mealType\": [\"breakfast\"]," +
            "\"label\" : \"Second Page\", " +
            "\"cuisineType\" : [\"french\"]," +
            "\"dishType\" : [\"condiments and sauces\"]," +
            "\"dietLabels\" : [\"High-Fiber\", \"Low-Carb\"]" +
            "} }]} ";

        CustomHttpResponse httpResponseSecondPage = new CustomHttpResponse();
        httpResponseSecondPage.setBody(bodySecondPage);
        HttpRequest httpRequestSecondPage =
            HttpRequest.newBuilder().uri(URI.create(URI_PREFIX + "&secondPage")).build();

        when(httpClientMock.send(httpRequestSecondPage, HttpResponse.BodyHandlers.ofString())).thenReturn(
            httpResponseSecondPage);

        Recipe recipeFromSecondPage = recipeSearcher.searchRecipeSingleKeyword("Fish").get(1);

        assertTrue(recipeFromSecondPage.getLabel().contains("Second Page"),
            "Client should correctly obtain all recipes from the second page should it exist");

    }

}
