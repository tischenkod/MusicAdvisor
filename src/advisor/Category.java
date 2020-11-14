package advisor;

import java.util.ArrayList;
import java.util.List;

public class Category extends MAEntity{
    String id;
//    List<PlayList> playLists = new ArrayList<>();

    public Category(String id, String name) {
        super();
        this.id = id;
        this.name = name;
//        System.out.println("Creating category id=" + id + " name=" + name);
    }

}
