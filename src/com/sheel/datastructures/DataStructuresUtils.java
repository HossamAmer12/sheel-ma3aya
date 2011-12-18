/**
 * 
 */
package com.sheel.datastructures;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Class used to group set of static methods used
 * to manipulate different data structures such
 * as removing duplicates and sorting
 * 
 * @author 
 *		Passant El.Agroudy (passant.elagroudy@gmail.com)
 *
 */
public final class DataStructuresUtils {
	
	private DataStructuresUtils() {
		
	}// end constructor
	
	/**
	 * Used to remove all elements of <code>main</code> from <code>needsFiltering</code>
	 * @param main
	 * 		Reference hashtable that will be kept as is
	 * @param needsFiltering
	 * 		Hashtable that will get filtered (elements existing in main will be removed)
	 * @author 
	 * 		Passant El.Agroudy (passant.elagroudy@gmail.com)
	 */
	public static void removeDuplicates(Hashtable<String, OfferDisplay> main , Hashtable<String, OfferDisplay> needsFiltering){
		
		Iterator contentIt = main.keySet().iterator();
		
		while(contentIt.hasNext()){
			String key = (String)contentIt.next();
			
			if (needsFiltering.containsKey(key)){
				needsFiltering.remove(key);
			}// end if : needsFiltering has duplicate from main -> remove
			
		}// end while: remove all elements in main from needsFiltering
		
	}// end removeDuplicates
	
	/**
	 * Used to remove all elements of <code>main</code> from <code>needsFiltering</code>
	 * <br><br><b>IMPORTANT: The method is generic and can be used with any objects
	 * inside the arraylist. However, they must override {@link Object#equals(Object)}
	 * method</b>
	 * @param main
	 * 		Reference arraylist that will be kept as is
	 * @param needsFiltering
	 * 		Reference that will get filtered (elements existing in main will be removed)
	 * 		<br><b>IMPORTANT: The method will delete elements from <code>needsFiltering
	 * 		</code></b>
	 * @author 
	 * 		Passant El.Agroudy (passant.elagroudy@gmail.com)
	 */
	public static void removeDuplicates(ArrayList<?> main, ArrayList<?> needsFiltering){
		
		if (needsFiltering.containsAll(main)) {
			needsFiltering.removeAll(main);
		}// end if: of elements in main is in needsFiltering -> remove
	}// end removeDuplicates
	

}// end class