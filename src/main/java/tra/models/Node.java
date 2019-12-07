package tra.models;

import java.util.ArrayList;
import java.util.List;

class Node {
    List<Pair<List<Object>, Action>> subNodes = new ArrayList<>();
    String name;

    Node(String name) {
        this.name = name;
    }

    Node next(List<Object> subNodes, Action action) {
        this.subNodes.add(new Pair<>(subNodes, action));
        return this;
    }
}
