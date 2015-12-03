package edu.illinois.cs427.mp3;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.*;
public class CollectionTest {
    @Test
    public void testRestoreCollection1() {
        Collection a =new Collection("collect");
	a.addElement(new Book("title","author"));
	Collection b = new Collection("inside_collect");
	b.addElement(new Book("dumm","dummy"));
	a.addElement(b);
	String str= a.getStringRepresentation();
	Collection c = Collection.restoreCollection(str);
	assertEquals(c.getElements().size(),2);
	assertEquals(((Collection)c.getElements().get(1)).getElements().size(),1);
	Book book = (Book)c.getElements().get(0);
	assertTrue(book.getTitle().equals("title"));
	assertTrue(book.getAuthor().equals("author"));
    }
    @Test
    public void testGetStringRepresentation1() {
        //TODO implement this
	 Collection a =new Collection("collect");
	a.addElement(new Book("title","author"));
	Collection b = new Collection("inside_collect");
	b.addElement(new Book("dumm","dummy"));
	a.addElement(b);
	String str= a.getStringRepresentation();
	try{
	JSONObject j = new JSONObject(str);
	assertTrue(j.has("collection"));
	JSONObject js = j.getJSONObject("collection");
	assertTrue(js.has("elements"));
	JSONArray arr = j.getJSONArray("elements");
	assertEquals(arr.length(), 2);
	}catch(Exception e){
	}
    }
    @Test
    public void testAddElement1() {
        //TODO implement this
	Collection a =new Collection("collect");
	a.addElement(new Book("title","author"));
	assertEquals(1, a.getElements().size());
	Book book = (Book)a.getElements().get(0);
	assertTrue(book.getTitle().equals("title"));
	assertTrue(book.getAuthor().equals("author"));
    }
    @Test
    public void testDeleteElement1() {
       	Collection a =new Collection("collect");
	Book b = new Book("title","author");
	a.addElement(b);
	assertTrue(a.deleteElement(b));
	assertEquals(0, a.getElements().size());
    }
}
