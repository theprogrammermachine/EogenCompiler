package tra.models;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public List<Pair<List<Object>, Action>> subNodes = new ArrayList<>();
    public String name;

    public Node(String name) {
        this.name = name;
    }

    public Node next(List<Object> subNodes, Action action) {
        this.subNodes.add(new Pair<>(subNodes, action));
        return this;
    }
}
