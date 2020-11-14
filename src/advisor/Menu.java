package advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {
    List<MenuItem> items = new ArrayList<>();

    void run(){
        Scanner scanner = new Scanner(System.in);
        do {
//            System.out.print(">");
        } while (scanner.hasNext() && apply(scanner.nextLine().strip()));
    }

    private boolean apply(String command) {
        for (MenuItem item :
                items) {
            if (command.equals(item.name)) {
                return item.action.apply(null);
            }
            if (command.startsWith(item.name + " ")) {
                return item.action.apply(command.substring(item.name.length()).strip());
            }
        }
        return true;
    }
}
