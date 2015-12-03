package edu.illinois.cs427.mp3;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.*;
public class LibraryTest {
    @Test
    public void testLibraryConstructorFromFile1() {
	Library l = new Library("src/test/resources/temp.txt");
	assertEquals(l.getCollections().size(),2);
    }
    @Test
    public void testSaveLibraryToFile1() {
        Library l =new Library("src/test/resources/temp.txt");
	l.saveLibraryToFile("src/test/resources/temp2.txt");
	Library l2 = new Library("src/test/resources/temp2.txt");
	assertEquals(l2.getCollections().size(),2);
    }
}
