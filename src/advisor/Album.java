package advisor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Album extends MAEntity{
    public List<Performer> performers = new ArrayList<>();

    public Album(String name) {
        super(name);
    }

    public Album(JsonElement json) {
        JsonObject jo = (JsonObject) json;
        name = jo.get("name").getAsString();
        for (JsonElement item :
                jo.get("artists").getAsJsonArray()) {
            performers.add(new Performer(item));
        }
        url = jo.get("external_urls").getAsJsonObject().get("spotify").getAsString();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%s%n[%s", name, performers.get(0).name));
        performers.stream().skip(1).forEach(p -> stringBuilder.append(String.format(", %s", p.name)));
        stringBuilder.append("]\n");
        stringBuilder.append(url);
        return stringBuilder.toString();
    }

    public void printDetail() {
        System.out.println(this);
//        System.out.printf("%s%n[%s", name, performers.get(0).name);
//        performers.stream().skip(1).forEach(p -> System.out.printf(", %s", p.name));
//        System.out.println("]");
//        System.out.println(url);
    }
}



