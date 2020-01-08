package tra.models;

public class Tuple<A, B, C, D, E, F, G> {

    public A first;
    public B second;
    public C third;
    public D forth;
    public E fifth;
    public F sixth;
    public G seventh;

    public Tuple(A a, B b, C c, D d, E fifth, F sixth, G seventh) {
        this.first = a;
        this.second = b;
        this.third = c;
        this.forth = d;
        this.fifth = fifth;
        this.sixth = sixth;
        this.seventh = seventh;
    }
}