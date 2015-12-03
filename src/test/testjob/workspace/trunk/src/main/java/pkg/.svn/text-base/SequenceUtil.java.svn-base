package pkg;

public class SequenceUtil {
    /** [TODO]: The method should return a Sequence of objects
     *  that are in both of the two argument Sequence objects.
     */
    public static Sequence intersect(Sequence a, Sequence b) {
        Sequence c = MySequence.create();
        Object[] arrayA = a.getElements();

        for(int i=0; i<arrayA.length; i++)
        {
            Object obj = arrayA[i];

            if(b.has(obj))
                c.insert(obj);
        }
        return c;
    }
}
