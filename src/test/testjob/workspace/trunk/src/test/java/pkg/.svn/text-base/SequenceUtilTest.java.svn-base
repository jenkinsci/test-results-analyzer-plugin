package pkg;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SequenceUtilTest {
    @Test
    public void testSanity() {
        //check that everything is at least basically functional
        assertTrue(true);
    }

    @Test
    public void failtest() {
        assertTrue(true);
    }

    @Test
    public void testInsert1() {
        Sequence seq = MySequence.create();
        Integer testObj = new Integer(5);
        seq.insert(testObj);
        assertTrue(seq.has(testObj));
    }

    @Test
    public void testInsert2() {
        Sequence seq = MySequence.create();
        Integer testObj = new Integer(5);
        seq.insert(testObj);
        seq.insert(testObj);
        Object[] testArray = seq.getElements();
        assertTrue(testArray.length == 1);
    }

    @Test
    public void testInsert3() {
        Sequence seq = MySequence.create();
        Integer testObj = new Integer(5);
        seq.insert(testObj);
        assertTrue(seq.has(new Integer(5)));
    }

    @Test
    public void testInsert4() {
        Sequence seq = MySequence.create();
        Integer testObj = new Integer(5);
        seq.insert(testObj);
        assertTrue(!seq.has(new Integer(6)));
    }

    @Test
    public void testInsert5() {
        //null is a valid object for this sequence
        Sequence seq = MySequence.create();
        Object n = null;
        assertTrue(!seq.has(n));
        seq.insert(n);
        assertTrue(seq.has(n));
    }

    @Test
    public void testRemove1() {
        Sequence seq = MySequence.create();
        Integer testObj = new Integer(5);
        seq.insert(testObj);
        seq.delete(testObj);
        assertTrue(!seq.has(testObj));
    }

    @Test
    public void testRemove2() {
        Sequence seq = MySequence.create();
        Integer testObj = new Integer(5);
        Integer otherObj = new Integer(6);
        seq.insert(testObj);
        seq.delete(otherObj);
        assertTrue(!seq.has(otherObj));
        assertTrue(seq.has(testObj));
    }

    @Test
    public void testRemove3() {
        Sequence seq = MySequence.create();
        Integer testObj0 = new Integer(0);
        Integer testObj1 = new Integer(1);
        Integer testObj2 = new Integer(2);
        Integer testObj3 = new Integer(3);
        Integer testObj4 = new Integer(4);
        Integer testObj5 = new Integer(5);
        Integer testObj6 = new Integer(6);
        Integer testObj7 = new Integer(7);
        seq.insert(testObj0);
        seq.insert(testObj1);
        seq.insert(testObj2);
        seq.insert(testObj3);
        seq.insert(testObj4);
        seq.insert(testObj5);
        seq.insert(testObj6);
        seq.insert(testObj7);
        assertTrue(seq.getElements().length == 8);
        seq.delete(testObj0);
        seq.delete(testObj1);
        seq.delete(testObj2);
        seq.delete(testObj3);
        seq.delete(testObj4);
        seq.delete(testObj5);
        seq.delete(testObj6);
        seq.delete(testObj7);
        assertTrue(seq.getElements().length == 0);
    }

    @Test
    public void testIntersect1() {
        Sequence seq1 = MySequence.create();
        Sequence seq2 = MySequence.create();
        seq1.insert(new Integer(5));
        seq2.insert(new Integer(6));
        Sequence seq3 = SequenceUtil.intersect(seq1, seq2);
        assertTrue(seq3.getElements().length == 0);
        seq1.insert(new Integer(6));
        Sequence seq4 = SequenceUtil.intersect(seq1, seq2);
        assertTrue(seq4.has(new Integer(6)));
    }


    @Test
    public void testIntersect2() {
        Sequence seq1 = MySequence.create();
        Sequence seq2 = MySequence.create();
        Integer testObj0 = new Integer(0);
        Integer testObj1 = new Integer(1);
        Integer testObj2 = new Integer(2);
        Integer testObj3 = new Integer(3);
        Integer testObj4 = new Integer(4);
        Integer testObj5 = new Integer(5);
        Integer testObj6 = new Integer(6);
        Integer testObj7 = new Integer(7);
        seq2.insert(testObj0);
        seq2.insert(testObj1);
        seq2.insert(testObj2);
        seq2.insert(testObj3);
        seq2.insert(testObj4);
        seq2.insert(testObj5);
        seq2.insert(testObj6);
        seq2.insert(testObj7);
        seq1.insert(testObj7);
        seq1.insert(testObj3);
        seq1.insert(testObj1);
        Sequence seq3 = SequenceUtil.intersect(seq1, seq2);
        Object[] arr = seq3.getElements();
        assertTrue(arr.length == 3);
        assertTrue(arr[0].equals(testObj7));
        assertTrue(arr[1].equals(testObj3));
        assertTrue(arr[2].equals(testObj1));
    }
}
