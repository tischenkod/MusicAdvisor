package advisor;

public class MAEntity {
    String name;
    String url;

    public MAEntity() {
    }

    public MAEntity(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
