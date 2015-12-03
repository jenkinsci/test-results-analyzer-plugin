package pkg;

/** Type to represent a sequence of objects. */
public interface Sequence {
    /** Adds the given element to the sequence. */
    public void insert(Object o);
    /** Removes the given element from the sequence. */
    public void delete(Object o);
    /** Returns true if the sequence contains the given object. */
    public boolean has(Object o);
    /** Returns the objects in the sequence. */
    public Object[] getElements();
}
