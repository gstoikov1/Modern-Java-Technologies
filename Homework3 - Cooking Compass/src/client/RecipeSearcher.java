package client;

import client.recipe.Recipe;
import client.recipe.criterion.CuisineType;
import client.recipe.criterion.DietLabel;
import client.recipe.criterion.DishType;
import client.recipe.criterion.HealthLabel;
import client.recipe.criterion.MealType;
import client.exception.InternalServerErrorException;
import client.exception.UnauthorizedAccessException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RecipeSearcher implements RecipeSearcherAPI {
    private String applicationID;
    private String applicationKey;
    private final Gson gson;
    private HttpClient httpClient;

    private static final String URI_PREFIX = "https://api.edamam.com/api/recipes/v2?type=public";

    private static final int ERROR_CODE_UNAUTHORIZED_ACCESS = 401;
    private static final int ERROR_CODE_INTERNAL_SERVER_ERROR_FROM = 500;
    private static final int ERROR_CODE_INTERNAL_SERVER_ERROR_TO = 505;

    public RecipeSearcher(String applicationID, String applicationKey) {
        this.applicationID = applicationID;
        this.applicationKey = applicationKey;
        gson = new GsonBuilder()
            .registerTypeAdapter(CuisineType.class, new Recipe.CuisineTypeDeserializer())
            .registerTypeAdapter(DishType.class, new Recipe.DishTypeDeserializer())
            .registerTypeAdapter(HealthLabel.class, new Recipe.HealthLabelDeserializer())
            .registerTypeAdapter(MealType.class, new Recipe.MealTypeDeserializer())
            .registerTypeAdapter(DietLabel.class, new Recipe.DietLabelDeserializer())
            .create();
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMinutes(1)).build();
    }

    @Override
    public List<Recipe> searchRecipeSingleKeyword(String keyword)
        throws UnauthorizedAccessException, InternalServerErrorException {
        if (keyword == null) {
            throw new IllegalArgumentException("keyword was null");
        }
        return searchRecipe(Set.of(keyword), null, null);
    }

    @Override
    public List<Recipe> searchRecipeSingleMealType(MealType mealType)
        throws UnauthorizedAccessException, InternalServerErrorException {
        if (mealType == null) {
            throw new IllegalArgumentException("mealType was null");
        }

        return searchRecipe(null, Set.of(mealType), null);

    }

    @Override
    public List<Recipe> searchRecipeSingleHealthLabel(HealthLabel healthLabel)
        throws UnauthorizedAccessException, InternalServerErrorException {
        if (healthLabel == null) {
            throw new IllegalArgumentException("healthLabel was null");
        }

        return searchRecipe(null, null, Set.of(healthLabel));
    }

    @Override
    public List<Recipe> searchRecipeKeywords(Collection<String> keywords)
        throws UnauthorizedAccessException, InternalServerErrorException {
        if (keywords == null) {
            throw new IllegalArgumentException("keywords was null");
        }

        return searchRecipe(Set.copyOf(keywords), null, null);
    }

    @Override
    public List<Recipe> searchRecipeMealTypes(Collection<MealType> mealTypes)
        throws UnauthorizedAccessException, InternalServerErrorException {
        if (mealTypes == null) {
            throw new IllegalArgumentException("mealTypes was null");
        }

        return searchRecipe(null, Set.copyOf(mealTypes), null);
    }

    @Override
    public List<Recipe> searchRecipeHealthLabels(Collection<HealthLabel> healthLabels)
        throws UnauthorizedAccessException, InternalServerErrorException {
        if (healthLabels == null) {
            throw new IllegalArgumentException("healthLabels was null");
        }

        return searchRecipe(null, null, Set.copyOf(healthLabels));
    }

    @Override
    public List<Recipe> searchRecipeMealTypeHealthLabel(Collection<MealType> mealTypes,
                                                        Collection<HealthLabel> healthLabels)
        throws UnauthorizedAccessException, InternalServerErrorException {
        if (mealTypes == null) {
            throw new IllegalArgumentException("mealTypes was null");
        }
        if (healthLabels == null) {
            throw new IllegalArgumentException("healthLabels was null");
        }

        return searchRecipe(null, Set.copyOf(mealTypes), Set.copyOf(healthLabels));
    }

    @Override
    public List<Recipe> searchRecipeAllCriterion(Collection<String> keywords, Collection<MealType> mealTypes,
                                                 Collection<HealthLabel> healthLabels)
        throws UnauthorizedAccessException, InternalServerErrorException {
        if (keywords == null) {
            throw new IllegalArgumentException("keywords was null");
        }

        if (mealTypes == null) {
            throw new IllegalArgumentException("mealTypes was null");
        }

        if (healthLabels == null) {
            throw new IllegalArgumentException("healthLabels was null");
        }

        return searchRecipe(Set.copyOf(keywords), Set.copyOf(mealTypes), Set.copyOf(healthLabels));
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private String createRequestString(Collection<String> keywords, Collection<MealType> mealTypes,
                                       Collection<HealthLabel> healthLabels) {

        StringBuilder request =
            new StringBuilder(URI_PREFIX).append("&app_id=").append(applicationID).append("&app_key=")
                .append(applicationKey);

        if (keywords != null) {
            for (String k : keywords) {
                request.append("&q=").append(k);
            }
        }

        if (mealTypes != null) {
            for (MealType m : mealTypes) {
                request.append("&mealType=").append(m.getType());
            }
        }
        if (healthLabels != null) {
            for (HealthLabel h : healthLabels) {
                request.append("&health=").append(h.getLabel());
            }
        }
        return request.toString();
    }

    private List<Recipe> getNextPageResults(JsonObject jsonBody)
        throws UnauthorizedAccessException, InternalServerErrorException {
        JsonObject links = jsonBody.getAsJsonObject("_links");
        List<Recipe> recipes = null;

        if (links != null && links.has("next")) {
            recipes = new ArrayList<>();

            String nextPage = links.getAsJsonObject("next").get("href").toString().replace("\"", "");
            HttpResponse<String> response = null;
            try {
                response = sendRequest(nextPage);
            } catch (IOException e) {
                throw new UncheckedIOException("There was an I/O problem", e);
            } catch (InterruptedException e) {
                throw new RuntimeException("Action was interrupted", e);
            }
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray hitsArray = jsonObject.getAsJsonArray("hits");

            for (JsonElement el : hitsArray) {
                Recipe r = gson.fromJson(el.getAsJsonObject().get("recipe"), Recipe.class);
                recipes.add(r);
            }
        }
        return recipes;
    }

    private List<Recipe> extractRecipesFromResponse(HttpResponse<String> response)
        throws UnauthorizedAccessException, InternalServerErrorException {
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray hitsArray = jsonObject.getAsJsonArray("hits");
        List<Recipe> recipes = new ArrayList<>();
        for (JsonElement el : hitsArray) {
            Recipe r = gson.fromJson(el.getAsJsonObject().get("recipe"), Recipe.class);
            recipes.add(r);
        }
        List<Recipe> recipesFromSecondPage = getNextPageResults(jsonObject);

        if (recipesFromSecondPage != null) {
            recipes.addAll(recipesFromSecondPage);
        }

        return recipes;
    }

    private HttpResponse<String> sendRequest(String request)
        throws IOException, InterruptedException, UnauthorizedAccessException, InternalServerErrorException {
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(request)).build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == ERROR_CODE_UNAUTHORIZED_ACCESS) {
            throw new UnauthorizedAccessException("Combination of ApplicationID and ApplicationKey was not valid");
        } else if (response.statusCode() >= ERROR_CODE_INTERNAL_SERVER_ERROR_FROM &&
            response.statusCode() <= ERROR_CODE_INTERNAL_SERVER_ERROR_TO) {
            throw new InternalServerErrorException("There was a problem with the server");
        }

        return response;
    }

    private List<Recipe> searchRecipe(Collection<String> keywords, Collection<MealType> mealTypes,
                                      Collection<HealthLabel> healthLabels)
        throws UnauthorizedAccessException, InternalServerErrorException {

        String requestString = createRequestString(keywords, mealTypes, healthLabels);

        HttpResponse<String> response = null;
        try {
            response = sendRequest(requestString);
        } catch (IOException e) {
            throw new UncheckedIOException("There was an I/O problem", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Action was interrupted", e);
        }
        return extractRecipesFromResponse(response);
    }
}
