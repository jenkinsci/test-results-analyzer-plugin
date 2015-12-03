package pkg;
import java.util.ArrayList;

class MySequence implements Sequence {

    /** Returns an empty sequence. */
    public static MySequence create() {
        return new MySequence();
    }

    /** Returns a sequence that consists of the given objects. */
    public static MySequence create(Object... os) {
        MySequence mySeq = MySequence.create();
        for (Object o : os) {
            mySeq.insert(o);
        }
        return mySeq;
    }

    /* [TODO]: You should implement this constructor */
    private MySequence() {
        this.elements = new ArrayList<Object>();
    }

    /* Feel free to add private fields/methods here */
    private ArrayList<Object> elements;

    /* Interface implementations. */
    /* [TODO]: You should implement these methods */

    public void insert(Object o) {
        //ordering is based on insertions only
        //duplicates are not allowed
        if(this.elements.contains(o))
            return;

        this.elements.add(o);
    }

    public void delete(Object o) {
        this.elements.remove(o);
    }

    public boolean has(Object o) {
        return this.elements.contains(o);
    }

    public Object[] getElements() {
        return this.elements.toArray();
    }
}
