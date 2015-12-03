package edu.illinois.cs427.mp3;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Represents a collection of books or (sub)collections.
 */
public final class Collection extends Element {
    List<Element> elements;
    private String name;
    
    /**
     * Creates a new collection with the given name.
     *
     * @param name the name of the collection
     */
    public Collection(String name) {
        // TODO implement this
	this.name = name;
	this.elements = new ArrayList<Element>();
    }

    /**
     * Restores a collection from its given string representation.
     *
     * @param stringRepresentation the string representation
     */
    public static Collection restoreCollection(String stringRepresentation) {
        // TODO implement this
	System.out.println(stringRepresentation);
	try{
	JSONObject js = new JSONObject(stringRepresentation);
	JSONObject j = new JSONObject(js.getString("collection"));
	Collection c = new Collection(j.getString("name"));
	System.out.println("a");
	JSONArray arr = j.getJSONArray("elements");
	System.out.println(arr.length());
	for(int i=0; i<arr.length(); i++)
	{
		JSONObject obj = arr.getJSONObject(i);
		System.out.println("c");
		if(obj.has("book"))
		{
			System.out.println("book");
			Book b = new Book(obj.toString());
			c.addElement(b);
		}
		else
		{	
			System.out.println("collection");
			//JSONObject in = new JSONObject(obj.get("collection"));
			Collection inc = restoreCollection(obj.toString());
			c.addElement(inc);
		}
	}
	return c;
	}catch(Exception e){}
	System.out.println("error");
	return null;
    }

    /**
     * Returns the string representation of a collection. 
     * The string representation should have the name of this collection, 
     * and all elements (books/subcollections) contained in this collection.
     *
     * @return string representation of this collection
     */
    public String getStringRepresentation() {
        // TODO implement this

	try{
	JSONObject js = new JSONObject();
	JSONObject j = new JSONObject();
	j.put("name", this.name);
	ArrayList<JSONObject> c = new ArrayList<JSONObject>();

	for(int i=0; i<this.elements.size(); i++)
	{
		Element e = elements.get(i);
		JSONObject temp;
		if(e instanceof Book)
		{
			temp = new JSONObject(((Book)elements.get(i)).getStringRepresentation());
		}
		else
		{
			temp = new JSONObject(((Collection)elements.get(i)).getStringRepresentation());
		}
		c.add(temp);
	}
	j.put("elements", c);
	js.put("collection", j);
	return js.toString();
	}catch(Exception e){}
	return "";
    }

    /**
     * Returns the name of the collection.
     *
     * @return the name
     */
    public String getName() {
        // TODO implement this
        return this.name;
    }
    
    /**
     * Adds an element to the collection.
     * If parentCollection of given element is not null,
     * do not actually add, but just return false.
     * (explanation: if given element is already a part of  
     * another collection, you should have deleted the element 
     * from that collection before adding to another collection)
     *
     * @param element the element to add
     * @return true on success, false on fail
     */
    public boolean addElement(Element element) {
        // TODO implement this
	if(element.getParentCollection() != null)
		return false;

	element.setParentCollection(this);
	this.elements.add(element);
	return true;
    }
    
    /**
     * Deletes an element from the collection.
     * Returns false if the collection does not have 
     * this element.
     * Hint: Do not forget to clear parentCollection
     * of given element 
     *
     * @param element the element to remove
     * @return true on success, false on fail
     */
    public boolean deleteElement(Element element) {
        // TODO implement this
        if(this.elements.contains(element))
	{
		this.elements.remove(element);
		element.setParentCollection(null);
		return true;
	}
	return false;
    }
    
    /**
     * Returns the list of elements.
     * 
     * @return the list of elements
     */
    public List<Element> getElements() {
        // TODO implement this
        return elements;
    }
}
