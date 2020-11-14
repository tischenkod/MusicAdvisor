package advisor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

public class Performer extends MAEntity{
    public Set<Song> songs = new HashSet<>();

    public Performer(String name) {
        super(name);
    }

    public Performer(JsonElement json) {
        JsonObject jo = (JsonObject) json;
        name = jo.get("name").getAsString();
    }
}
