package edu.illinois.cs427.mp3;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.*;

public class BookTest {
    @Test
    public void testBookConstructor1() {
        //TODO implement this
	Book b = new Book("title", "author");
	assertTrue(b.getTitle().equals("title"));
	assertTrue(b.getAuthor().equals("author"));
    }

    @Test
    public void testGetStringRepresentation1() {
        //TODO implement this
	try
	{
		Book b = new Book("title", "author");
		String s = b.getStringRepresentation();
		JSONObject j = new JSONObject(s);
		assertEquals("title", j.getJSONObject("book").getString("title"));
		assertEquals("author", j.getJSONObject("book").getString("author"));
	}
	catch(Exception e)
	{

	}
    }

    @Test
    public void testGetContainingCollections1() {
        //TODO implement this

	Book b = new Book("title", "author");
	assertEquals(null, b.getParentCollection());
	Collection c = new Collection("collection");
	c.addElement(b);
	assertEquals(1, b.getContainingCollections().size());
    }
}
