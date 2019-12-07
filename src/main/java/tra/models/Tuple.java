package tra.models;

public class Tuple<A, B, C> {

    public final A first;
    public final B second;
    public final C third;

    public Tuple(A a, B b, C c) {
        this.first = a;
        this.second = b;
        this.third = c;
    }
}
