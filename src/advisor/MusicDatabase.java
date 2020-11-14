package advisor;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

enum MDAction {
    mdNew,
    mdFeatured,
    mdCategories,
    mdPlaylists,
}

public class MusicDatabase {

    private final List<MAEntity> fetchedList = new ArrayList<>();

    class PaginationInfo {
        boolean custom;
        String prevURL;
        String nextURL;
        int total;
        int offset;
        int limit;
        int getPageNo() {
            return offset / itemsPerPage + 1;
        }

        int getTotalPages() {
            return total / itemsPerPage + ((total % itemsPerPage) > 0 ? 1 : 0);
        }

        @Override
        public String toString() {
            return format("---PAGE %d OF %d---", getPageNo(), getTotalPages());
//            return format("---PAGE %d OF %d---(%d)", getPageNo(), getTotalPages(), itemsPerPage);
        }

        public boolean step(int direction) {
            if (direction == 0)
                return false;
            if (direction > 0 && offset + limit >= total)
                return false;
            if (direction < 0 && offset - limit < 0)
                return false;
            offset = direction > 0 ? offset + limit : offset - limit;
            return true;
        }
    }

    public static int itemsPerPage = 5;

    List<Category> categoriesCache = new ArrayList<>();

    MDAction prevAction;
    PaginationInfo pagination;

    public MusicDatabase() {
    }

    PaginationInfo updatePagination(JsonElement json) {
        PaginationInfo paginationInfo;
        try {
            JsonObject jo = (JsonObject) json;
            paginationInfo = new PaginationInfo();
            try {
                paginationInfo.prevURL = jo.get("previous").getAsString();
            } catch (UnsupportedOperationException e) {
                paginationInfo.prevURL = null;
            }
            try {
                paginationInfo.nextURL = jo.get("next").getAsString();
            } catch (UnsupportedOperationException e) {
                paginationInfo.nextURL = null;
            }
            paginationInfo.offset = jo.get("offset").getAsInt();
            paginationInfo.total = jo.get("total").getAsInt();
            paginationInfo.custom = jo.get("limit").getAsInt() != itemsPerPage;
            paginationInfo.limit = itemsPerPage;
        } catch (ClassCastException e) {
            System.out.println(e);
            paginationInfo = null;
        }
        return paginationInfo;
    }

    String url(String directURL, int direction) {
        if (direction == 0) {
            return directURL;
        } else {
//            System.out.println("Direction = " + direction);
            return direction < 0 ? pagination.prevURL : pagination.nextURL;
        }
    }

    private void printRange() {
        fetchedList.stream().skip(pagination.offset).limit(itemsPerPage).forEach(System.out::println);
    }

    public void newReleases(int direction) {
        prevAction = MDAction.mdNew;
        if (direction != 0 && pagination != null && pagination.custom) {
            if (pagination.step(direction)) {
                printRange();
                System.out.println(pagination);
            } else {
                System.out.println("No more pages.");
            }
            return;
        }
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + OAuth.getAccessToken())
                .header("limit", Integer.toString(itemsPerPage))
                .uri(URI.create(url(OAuth.resourceServer + "/v1/browse/new-releases?limit=" + itemsPerPage, direction)))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonObject("albums");
            pagination = updatePagination(jo);
            JsonArray jArray = jo.get("items").getAsJsonArray();
            if (pagination.custom) {
                fetchedList.clear();
                for (JsonElement item :
                        jArray) {
                    fetchedList.add(new Album(item));
                }
                printRange();
            } else {
                for (JsonElement item :
                        jArray) {
                    new Album(item).printDetail();
                    System.out.println();
                }
            }
            System.out.println(pagination);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void featured(int direction) {
        prevAction = MDAction.mdFeatured;
        if (direction != 0 && pagination != null && pagination.custom) {
            if (pagination.step(direction)) {
                printRange();
                System.out.println(pagination);
            } else {
                System.out.println("No more pages.");
            }
            return;
        }
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + OAuth.getAccessToken())
                .header("limit", Integer.toString(itemsPerPage))
                .uri(URI.create(url(OAuth.resourceServer + "/v1/browse/featured-playlists?limit=" + itemsPerPage, direction)))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonObject("playlists");
            pagination = updatePagination(jo);
            JsonArray jArray = jo.get("items").getAsJsonArray();
            if (pagination.custom) {
                fetchedList.clear();
                for (JsonElement item :
                        jArray) {
                    fetchedList.add(new PlayList(item));
                }
                printRange();
            } else {
                for (JsonElement item :
                        jArray) {
                    new PlayList(item).printDetail();
                    System.out.println();
                }
            }
            System.out.println(pagination);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void fillInCategoriesCache() {
        categoriesCache.clear();
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.nextURL = OAuth.resourceServer + "/v1/browse/categories";
        do {
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + OAuth.getAccessToken())
                    .uri(URI.create(paginationInfo.nextURL))
                    .GET()
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonObject("categories");
                paginationInfo = updatePagination(jo);
                JsonArray jArray = jo.get("items").getAsJsonArray();

                for (JsonElement item :
                        jArray) {
                    jo = (JsonObject) item;
                    categoriesCache.add(new Category(jo.get("id").getAsString(), jo.get("name").getAsString()));
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } while (paginationInfo.nextURL != null);

    }

    public void categories(int direction) {
        prevAction = MDAction.mdCategories;
        if (direction != 0 && pagination != null && pagination.custom) {
            if (pagination.step(direction)) {
                printRange();
                System.out.println(pagination);
            } else {
                System.out.println("No more pages.");
            }
            return;
        }
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + OAuth.getAccessToken())
                .uri(URI.create(url(OAuth.resourceServer + "/v1/browse/categories?limit=" + itemsPerPage, direction)))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonObject("categories");
            pagination = updatePagination(jo);
            JsonArray jArray = jo.get("items").getAsJsonArray();
            if (pagination.custom) {
                fetchedList.clear();
                for (JsonElement item :
                        jArray) {
                    jo = (JsonObject) item;
//                    System.out.println("Adding category id=" + jo.get("id").getAsString() + " name=" + jo.get("name").getAsString());
                    Category category = new Category(jo.get("id").getAsString(), jo.get("name").getAsString());
//                    System.out.println("Created category " + category);
                    fetchedList.add(category);
                }
                printRange();
            } else {
                for (JsonElement item :
                        jArray) {
                    jo = (JsonObject) item;
                    System.out.println(new Category(jo.get("id").getAsString(), jo.get("name").getAsString()));
                }
            }
            System.out.println(pagination);
//            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void categoryPlayLists(int direction, String categoryName) {
        prevAction = MDAction.mdPlaylists;
        if (direction != 0 && pagination != null && pagination.custom) {
            if (pagination.step(direction)) {
                printRange();
                System.out.println(pagination);
            } else {
                System.out.println("No more pages.");
            }
            return;
        }
        Category category;
        String catId = "";
        if (direction == 0) {
            if ((category = categoryByName(categoryName)) == null) {
                fillInCategoriesCache();
                category = categoryByName(categoryName);
            }

            if (category == null) {
                System.out.println("Unknown category name.");
                return;
            }
            catId = category.id;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + OAuth.getAccessToken())
                .header("limit", Integer.toString(itemsPerPage))
                .uri(URI.create(url(format("%s/v1/browse/categories/%s/playlists?limit=%d", OAuth.resourceServer, catId, itemsPerPage), direction)))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject playlists = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonObject("playlists");
            pagination = updatePagination(playlists);
            if (playlists == null) {
                System.out.println("No playlists item");
                System.out.println(response.body());
                return;
            }
            JsonArray jArray = playlists.get("items").getAsJsonArray();
            if (pagination.custom) {
                fetchedList.clear();
                for (JsonElement item :
                        jArray) {
                    fetchedList.add(new PlayList(item));
                }
                printRange();
            } else {
                for (JsonElement item :
                        jArray) {
                    new PlayList(item).printDetail();
                }
            }
            System.out.println(pagination);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Category categoryByName(String categoryName) {
        if (categoryName == null) {
            return null;
        }
        for (Category category :
                categoriesCache) {
            if (category.name.equals(categoryName)) {
                return category;
            }
        }
        return null;
    }

    public void exit() {
        System.out.println("---GOODBYE!---");
    }

    void step(int direction) {
        if (direction == 0) {
            System.out.println("Invalid direction!");
            return;
        }
        if (pagination == null) {
            System.out.println("No more pages.");
            return;
        }
        if ((direction < 0 && pagination.prevURL == null && !pagination.custom) ||
                (direction > 0 && pagination.nextURL == null && !pagination.custom)) {
            System.out.println("No more pages.");
            return;
        }
        if (prevAction == null) {
            System.out.println("No more pages.");
            return;
        }
        switch (prevAction) {
            case mdNew:
                newReleases(direction);
                break;
            case mdFeatured:
                featured(direction);
                break;
            case mdCategories:
                categories(direction);
                break;
            case mdPlaylists:
                categoryPlayLists(direction, null);
        }
    }
}
