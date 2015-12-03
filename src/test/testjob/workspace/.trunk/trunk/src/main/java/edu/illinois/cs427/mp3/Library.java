package edu.illinois.cs427.mp3;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Container class for all the collections (that eventually contain books).
 * Its main responsibility is to save the collections to a file and restore them from a file.
 */
public final class Library {
    private List<Collection> collections;

    /**
     * Builds a new, empty library.
     */
    public Library() {
        // TODO implement this
        this.collections = new ArrayList<Collection>();
    }

    /**
     * Builds a new library and restores its contents from the given file.
     *
     * @param fileName the file from where to restore the library.
     */
    public Library(String fileName) {
        // TODO implement this
	this.collections = new ArrayList<Collection>();
	String data = "";
	try
	{
		String line;
		FileReader f = new FileReader(fileName);
		BufferedReader br = new BufferedReader(f);

		while((line = br.readLine()) != null)
		{
			data = data + line;
		}
		br.close();
		System.out.println("data: "+data);
		JSONObject j = new JSONObject(data);

		JSONArray arr = j.getJSONArray("library");
		System.out.println(arr.length());
		for(int i=0; i<arr.length(); i++)
		{
			JSONObject obj = arr.getJSONObject(i);
			Collection c = Collection.restoreCollection(obj.toString());
			this.collections.add(c);
		}
	}
	catch(Exception e)
	{
		//rip
	}
    }

    /**
     * Saved the contents of the library to the given file.
         *
     * @param fileName the file where to save the library
     */
    public void saveLibraryToFile(String fileName) {
        // TODO implement this
	try
	{
   	 	JSONObject js = new JSONObject();
		ArrayList<JSONObject> c = new ArrayList<JSONObject>(); 

		for(int i=0; i<this.collections.size(); i++)
		{
			JSONObject temp = new JSONObject(collections.get(i).getStringRepresentation());
			c.add(temp);
		}
		js.put("library", c);
		String str = js.toString();


		FileWriter f = new FileWriter(fileName);
		BufferedWriter bw = new BufferedWriter(f);
		bw.write(str);
		bw.close();
	}
	catch(Exception e)
	{

	}
    }

    /**
     * Returns the collections contained in the library.
     *
     * @return library contained elements
     */
    public List<Collection> getCollections() {
        // TODO implement this
        return this.collections;
    }
}
