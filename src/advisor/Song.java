package advisor;

import java.util.ArrayList;
import java.util.List;

public class Song extends MAEntity{
    public List<Performer> performers = new ArrayList<>();

    public Song(String name) {
        super(name);
    }

    public void printDetail() {
        System.out.printf("%s [%s", name, performers.get(0).name);
        performers.stream().skip(1).forEach(p -> System.out.printf(", %s", p.name));
        System.out.println("]");
    }
}
