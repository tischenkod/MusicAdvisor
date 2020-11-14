package advisor;

import java.io.IOException;

public class Main {


    public static void main(String[] args) throws IOException {

        MusicDatabase database = new MusicDatabase();
        Menu menu = new Menu();

//        args = new String[]{"-page", "3"};

        if (args.length % 2 == 0) {
            for (int i = 0; i < args.length / 2; i++) {
                switch (args[i * 2]) {
                    case "-access":
                        OAuth.accessServer = args[i * 2 + 1];
                        break;
                    case "-resource":
                        OAuth.resourceServer = args[i * 2 + 1];
                        break;
                    case "-page":
                        database.itemsPerPage = Integer.parseInt(args[i * 2 + 1]);
                        break;
                    default:
                        System.out.println("Invalid parameter: " + args[i * 2]);
                }
            }
        }

        menu.items.add(new MenuItem("prev", s -> {
            if (OAuth.checkAuth()) {
                database.step(-1);
            }
            return true;
        }));

        menu.items.add(new MenuItem("next", s -> {
            if (OAuth.checkAuth()) {
                database.step(1);
            }
            return true;
        }));

        menu.items.add(new MenuItem("new", s -> {
            if (OAuth.checkAuth()) {
                database.newReleases(0);
            }
            return true;
        }));

        menu.items.add(new MenuItem("featured", s -> {
            if (OAuth.checkAuth()) {
                database.featured(0);
            }
            return true;
        }));

        menu.items.add(new MenuItem("categories", s -> {
            if (OAuth.checkAuth()) {
                database.categories(0);
            }
            return true;
        }));

        menu.items.add(new MenuItem("playlists", (String s) -> {
            if (OAuth.checkAuth()) {
                database.categoryPlayLists(0, s);
            }
            return true;
        }));

        menu.items.add(new MenuItem("auth", s -> {
            OAuth.authenticate();
            return true;
        }));

        menu.items.add(new MenuItem("exit", s -> {
            if (OAuth.checkAuth()) {
                database.exit();
                return false;
            }
            return true;
        }));

        menu.run();

    }
}
