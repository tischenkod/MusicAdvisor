package advisor;

import java.util.function.Function;

public class MenuItem {
    String name;
    Function<String, Boolean> action;

    public MenuItem(String name, Function<String, Boolean> action) {
        this.name = name;
        this.action = action;
    }

}
