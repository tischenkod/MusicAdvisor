package advisor;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PlayList extends MAEntity{

    public PlayList(String name) {
        super(name);
    }

    public PlayList(JsonElement json) {
        JsonObject jo = (JsonObject) json;
        name = jo.get("name").getAsString();
        url = jo.get("external_urls").getAsJsonObject().get("spotify").getAsString();
    }

    @Override
    public String toString() {
        return String.format("%s%n%s%n", name, url);
    }

    public void printDetail() {
        System.out.println(this);
//        System.out.printf("%s%n", name);
//        System.out.println(url);
    }
}
