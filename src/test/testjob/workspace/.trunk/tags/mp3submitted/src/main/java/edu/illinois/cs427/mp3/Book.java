package edu.illinois.cs427.mp3;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * This class contains the information needed to represent a book.
 */
public final class Book extends Element {
    private String title;
    private String author;

    /**
     * Builds a book with the given title and author.
     *
     * @param title the title of the book
     * @param author the author of the book
     */
    public Book(String title, String author) {
        this.title = title;
	this.author = author;
    }

    /**
     * Builds a book from the string representation, 
     * which contains the title and author of the book. 
     *
     * @param stringRepresentation the string representation
     */
    public Book(String stringRepresentation) {
        // TODO implement this
	try
	{
		JSONObject j = new JSONObject(stringRepresentation);
		this.title = j.getJSONObject("book").getString("title");
		this.author = j.getJSONObject("book").getString("author");
	}
	catch(Exception e)
	{

	}
    }

    /**
     * Returns the string representation of the given book.
     * The representation contains the title and author of the book.
     *
     * @return the string representation
     */
    public String getStringRepresentation() {
        // TODO implement this

	try
	{
		JSONObject j = new JSONObject();
		j.put("title", this.title);
		j.put("author", this.author);
		JSONObject js = new JSONObject();
		js.put("book", j);
        	return js.toString();
	}
	catch(Exception e)
	{

	}
	return "";
    }

    /**
     * Returns all the collections that this book belongs to directly and indirectly.
     * Consider the following. 
     * (1) Computer Science is a collection. 
     * (2) Operating Systems is a collection under Computer Science. 
     * (3) The Linux Kernel is a book under Operating System collection. 
     * Then, getContainingCollections method for The Linux Kernel should return a list 
     * of these two collections (Operating Systems, Computer Science), starting from 
     * the direct collection to more indirect collections.
     *
     * @return the list of collections
     */
    public List<Collection> getContainingCollections() {
        // TODO implement this
	ArrayList<Collection> l = new ArrayList<Collection>();
	Collection c = this.getParentCollection();

	while(c != null)
	{
		System.out.println(c.getName());
		l.add(c);
		c = c.getParentCollection();
	}
        return l;
    }

    /**
     * Returns the title of the book.
     *
     * @return the title
     */
    public String getTitle() {
        // TODO implement this
        return this.title;
    }

    /**
     * Returns the author of the book.
     *
     * @return the author
     */
    public String getAuthor() {
        // TODO implement this
        return this.author;
    }
}
