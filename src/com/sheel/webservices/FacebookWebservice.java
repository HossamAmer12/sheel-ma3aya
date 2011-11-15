package com.sheel.webservices;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.sheel.datastructures.FacebookUser;

/**
 * This class is used as a web service to interact with facebook for requests
 * regarding the user
 * 
 * @author passant
 *
 */

public class FacebookWebservice {
	
	/*
	 * --------------------------------------- Enumerations ------------------------------------------------
	 *  
	 */
	
	enum FacebookUserProperties{
		id,
		first_name,
		middle_name,
		last_name,
		gender,
		verified,
		email		
	}
	
	/*
	 * --------------------------------------- Constants ---------------------------------------------------
	 * 
	 */
	
	/**
	 * Constant used for tracing purposes "class name (package name)"
	 */
	private final String TAG_CLASS_PACKAGE = "FacebookWebService (com.sheel.webservices): ";
	/**
	 * Constant identifying the app ID in facebook
	 */
	private final String APP_ID = "301637916526853";
	
	/* Not grouped in an enumeration to avoid writing code for 
	 * passing their values. (class implementation)
	 * Check URL: 
	 * http://javahowto.blogspot.com/2006/10/custom-string-values-for-enum.html
	 */
	
	/**
	 * Shared preferences key for user access token
	 */
	private final String SP_ACCESS_TOKEN = "access_token";
	/**
	 * Shared preferences key for user access token expiry duration
	 */
	private final String SP_ACCESS_TOKEN_EXPIRY = "access_expiry";
	
	/*
	 * --------------------------------------- Instance parameters ------------------------------------------
	 * 
	 */
	
	/**
	 * Instance of facebook for accessing all the features
	 */
	Facebook facebook = new Facebook(APP_ID);
	/**
	 * Asynchronous API requests to avoid blocking the UI thread
	 */
	AsyncFacebookRunner asyncFacebookRunner = new AsyncFacebookRunner(facebook);
	/**
	 * Different settings saved for different apps in android settings
	 */
	SharedPreferences sharedPreferences;
	/**
	 * Data structure for holding information about the facebook user
	 */	
	FacebookUser fbUser = null;

	
	/*
	 * --------------------------------------- Constructors ------------------------------------------
	 * 
	 */
	
	/**
	 * Gets the facebook user (data structure) storing all data that was received about the user
	 * @return data structure having all information retrieved so far about user
	 */
	public FacebookUser getFacebookUser(){
		return fbUser;
	}// end getFacebookUser
	
	public void login (Activity parentActivity){
		
		/*
		 * ########################################################## 
		 * # If the user is already logged in -> use his credentials#
		 * ##########################################################
		 */
		
		// Get the shared preferences of the user from the activity
	/*	sharedPreferences = parentActivity.getPreferences(Context.MODE_PRIVATE);

		Log.e(TAG_CLASS_PACKAGE, "login: sharedPreferences: " + sharedPreferences);

		// Search for user access token and its expiry duration
		String accessToken = sharedPreferences.getString(SP_ACCESS_TOKEN, null);
		long accessTokenExpiry = sharedPreferences.getLong(
				SP_ACCESS_TOKEN_EXPIRY, -1);

		Log.e(TAG_CLASS_PACKAGE, "login: accessToken( " + accessToken + ")  expiry("
				+ accessTokenExpiry + ")");

		if (accessToken != null && accessTokenExpiry >= 0) {
			Log.e(TAG_CLASS_PACKAGE, "login: " + "FB session is working");
			facebook.setAccessToken(accessToken);
			facebook.setAccessExpires(accessTokenExpiry);
			getUserInfoForApp();
			Log.e(TAG_CLASS_PACKAGE, "login: " + "FB token setting is done");			
		}// end if : user already logged in -> pass token + expiry
		*/
		
		sharedPreferences = parentActivity.getPreferences(Context.MODE_PRIVATE);
		
		/*
		 * ##############################################################
		 * # If the user is not logged in (session expired) -> request  #
		 * # authorization 												#
		 * ##############################################################
		 */
		if (!facebook.isSessionValid()) {
			Log.e(TAG_CLASS_PACKAGE, "login: " + "FB session expired");

			facebook.authorize(parentActivity, new DialogListener() {
				// @Override
				public void onComplete(Bundle values) {

					/*
					 * Called when the loginning in for FB + APP is successful
					 * -> basic data of user is there
					 */
					Log.e(TAG_CLASS_PACKAGE, "login : onComplete");
					// Log.e(ERROR_TAG,"Welcome "+name);

					/*
					 * Edit the shared preferences and add to them the user
					 * access token and its expiry date
					 */
					// TODO why do we need this
					
					
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(SP_ACCESS_TOKEN, facebook.getAccessToken());
					editor.putLong(SP_ACCESS_TOKEN_EXPIRY,
							facebook.getAccessExpires());
					editor.commit();
					getUserInfoForApp();
					
					tester_filterOffersFromFriends();
				}// end onComplete:

				public void onFacebookError(FacebookError error) {
					Log.e(TAG_CLASS_PACKAGE, "onFacebookError");
				}// end onFacebookError

				public void onError(DialogError e) {
					Log.e(TAG_CLASS_PACKAGE, "onError " + e.getMessage());
				}// end onError

				public void onCancel() {
					Log.e(TAG_CLASS_PACKAGE, "onCancel");
				}// end onCancel
			});

		}// end if : facebook session expired -> login
	
		
	}// end login
	
	public void logout(Activity parentActivity){
		asyncFacebookRunner.logout(parentActivity.getApplicationContext(), new RequestListener(){

			@Override
			public void onComplete(String response, Object state) {
				Log.e(TAG_CLASS_PACKAGE,"logout: onComplete");
				
			}

			@Override
			public void onIOException(IOException e, Object state) {
				Log.e(TAG_CLASS_PACKAGE,"logout: onIOException");
				
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
				Log.e(TAG_CLASS_PACKAGE,"logout: onFileNotFoundException");
				
				
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				Log.e(TAG_CLASS_PACKAGE,"logout: onMalformedURLException");
				
				
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
				Log.e(TAG_CLASS_PACKAGE,"logout: onFacebookError");
				
				
			}
			
		});
		
	}// end logout
	
	public void getUserInfoForApp(){
		// Create list of required Information from user object
		ArrayList<String> requiredInformation = new ArrayList<String>();
					
		requiredInformation.add(FacebookUserProperties.id.name());
		requiredInformation.add(FacebookUserProperties.first_name.name());
		requiredInformation.add(FacebookUserProperties.middle_name.name());
		requiredInformation.add(FacebookUserProperties.last_name.name());
		requiredInformation.add(FacebookUserProperties.gender.name());
		requiredInformation.add(FacebookUserProperties.verified.name());
		requiredInformation.add(FacebookUserProperties.email.name());		
		
		getUserInfoFor(requiredInformation);
	}// end getUserInfoForApp
	
	public void getUserInfoFor(ArrayList<String> userPropertiesRequired){
		
		if (facebook.isSessionValid()){
			
			// TODO needs optimization (see how to pass bundle for the method)
			
			// TODO choosing using properties is not enabled yet IMPORTANT
			/*String parameters = "?field=";
			int size = userPropertiesRequired.size();
			if ( size > 0){
				for (int i=0 ; i<size ; i++){
					parameters += userPropertiesRequired.get(i) + ",";
				}// end for : concatinate different options
			}// end if : check it has elements
			parameters = parameters.substring(0,parameters.length()-1);
			
			System.out.println("Parameters: " + parameters);*/
			
			// Generate an asynchronous call to avoid freezing the GUI 
			asyncFacebookRunner.request("me",new RequestListener(){

				public void onComplete(String response, Object state) {
					Log.e(TAG_CLASS_PACKAGE,"getUserInfoFor: onComplete : response : " + response);
					
					//analyze data and return an easy-to-handle object
					fbUser = new FacebookUser(response);
					System.out.println(fbUser);
					
					//getUserFriends();
				
									
				}// end onComplete
				
				public void onIOException(IOException e, Object state) {
					// TODO Auto-generated method stub
					
				}// end onIOException

				public void onFileNotFoundException(FileNotFoundException e,Object state) {
					// TODO Auto-generated method stub
					
				}// end onFileNotFoundException
				
				public void onMalformedURLException(MalformedURLException e,
						Object state) {
					// TODO Auto-generated method stub
					
				}// end onMalformedURLException

				public void onFacebookError(FacebookError e, Object state) {
					// TODO Auto-generated method stub
					
				}// end onFacebookError		
			});	
					
			
		}// end if : session is still valid
		else{
			Log.e(TAG_CLASS_PACKAGE,"getUserInfoFor: session has expired");
		}
	
	}// end getUserInfoFor
	
	public void getUserFriends(){
		asyncFacebookRunner.request("me/friends", new RequestListener(){

			public void onComplete(String response, Object state) {
				Log.e(TAG_CLASS_PACKAGE,"getUserFriends: onComplete : response : " + response);
				
				// Parse the JSON string to key-value map
				try {
					JSONObject parsedValues = new JSONObject(response);
					
					Iterator it = parsedValues.keys();
					
						//Map<String , String> dic = new Map<String, String>();
						
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
								
			}// end onComplete
			
			public void onIOException(IOException e, Object state) {
				// TODO Auto-generated method stub
				
			}// end onIOException

			public void onFileNotFoundException(FileNotFoundException e,Object state) {
				// TODO Auto-generated method stub
				
			}// end onFileNotFoundException
			
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				// TODO Auto-generated method stub
				
			}// end onMalformedURLException

			public void onFacebookError(FacebookError e, Object state) {
				// TODO Auto-generated method stub
				
			}// end onFacebookError		
		});
	}// end getUserFriends
	
	/**
	 * This method is filter offers coming from user's friends from a more
	 * generic set of offers. This method assumes the list of user friends
	 * is already retrieved.
	 * 
	 * @param userFriends
	 * 		JSON string representing user friends. see @link {@link FacebookWebservice#getUserFriends()}
	 * 
	 * @param offersFromUsers 
	 * 		Hash table where:
	 * 			<ul>
	 * 				<li>the <code>key</code> is Facebook ID of requested offer owner</li>
	 * 				<li>the <code>value</code> is object representing offer</li>
	 * 			</ul> 
	 * @return
	 * 		list containing Facebook IDs of offer owners who are friends with 
	 * 		the user. In case all owners are NOT friends with the user, the list
	 * 		is returned empty (size=0). This list can be used later to index the 
	 * 		offers (in the input hash table for example) and retrieve them for
	 * 		displaying.
	 * 
	 */
	public ArrayList<String> filterOffersFromFriends(JSONObject userFriends,Hashtable<String,Object> offersFromUsers){
		
		// List of result of owners in friends of the user
		ArrayList<String> ownersIdsFromFriends = new ArrayList<String>();
		
		// List of facebook IDs of offer owners without duplicates
		Set<String> ownersIds= offersFromUsers.keySet();
		
		// Get an iterator to loop the set of owners IDs for checking
		Iterator<String> ownersIdsIterator = ownersIds.iterator();
		
		while(ownersIdsIterator.hasNext()){
			
			// Owner ID to be checked in the iteration
			String currentOwnerFacebookId = ownersIdsIterator.next();
			if (userFriends.has(currentOwnerFacebookId)){
				ownersIdsFromFriends.add(currentOwnerFacebookId);
			}// end if : offer owner ID exists in friends of user -> add to output list
			
		}// end while : check IDs of all offer owners		
		
		return ownersIdsFromFriends;
	}// end filterOffersFromFriends
	
	/**
	 * This method is filter offers coming from user's friends from a more
	 * generic set of offers. This method will issue (N) HTTP requests for checking
	 * whether owners are friends with user or not. N is the number of owners to
	 * be checked
	 * 
	 * @param offersFromUsers 
	 * 		Hash table where:
	 * 			<ul>
	 * 				<li>the <code>key</code> is Facebook ID of requested offer owner</li>
	 * 				<li>the <code>value</code> is object representing offer</li>
	 * 			</ul> 
	 * @return
	 * 		list containing Facebook IDs of offer owners who are friends with 
	 * 		the user. In case all owners are NOT friends with the user, the list
	 * 		is returned empty (size=0). This list can be used later to index the 
	 * 		offers (in the input hash table for example) and retrieve them for
	 * 		displaying.
	 * 
	 */
	public ArrayList<String> filterOffersFromFriends(Hashtable<String,Object> offersFromUsers){		
	
		/**
		 * Inner class for listening to different actions while requesting
		 * friendship status between the user and an owner of an offer
		 * 
		 * @author passant
		 *
		 */
		class FriendShipStatusCheckListener implements RequestListener{
			
			//_____________________ Constants ____________________________
			
			/**
			 * Used for tracing purposes
			 */
			private final String METHOD_NAME = "filterOffersFromFriends(Hashtable<String,Object> offersFromUsers)";

			
			//_____________________ Instance parameters __________________			
						
			/**
			 *  List of result of owners in friends of the user
			 */
			private ArrayList<String> ownersIdsFromFriends = new ArrayList<String>();
			
			/**
			 * Total number of offers that must be processed. It is used for scheduling
			 * reasons to know when to allow return of result from the method
			 */
			private int totalNumberOfOffersRemaining =-1;
			/**
			 * Semaphore to indicate when to wake up the method thread and allow it 
			 * to return results. It is used to prevent returning of uncompleted result
			 * from the method because the thread executing the HTTP calls hasn't 
			 * finished yet
			 */
			private final Semaphore waitForAllOffersProcessing = new Semaphore(0);
			
			//_____________________ Different Actions _____________________
			
			public void onComplete(String response, Object state) {
				
				try {
					totalNumberOfOffersRemaining --;
					
					// Contains friend data and paging data
					JSONObject receivedData = new JSONObject(response);
					// Get friend data	
					JSONArray friendData = receivedData.getJSONArray("data");
					
					if (friendData.length() > 0){
						// If friends -> array has one JSONObject entry
						JSONObject friendDataDetails = friendData.getJSONObject(0);
						// Get the facebook ID of the friend. To get the name -> use "name"
						String friendFacebookId = friendDataDetails.getString("id");
						// Add to results list
						ownersIdsFromFriends.add(friendFacebookId);	
						
					}/* end if : if both are friends 
					  * 		=>
					  * FB returns FB ID of checked friend mapped to his/her name
					  */
				
					if (totalNumberOfOffersRemaining == 0){					
						waitForAllOffersProcessing.release();
					}// end if : all offers are processed -> release semaphore to return result
				} catch (JSONException e) {
					Log.e(TAG_CLASS_PACKAGE,METHOD_NAME+":onComplete: Exception in parsing received data");
					e.printStackTrace();
				}// end catch
				
			}// end onComplete
			public void onIOException(IOException e, Object state) {
				Log.e(TAG_CLASS_PACKAGE,METHOD_NAME+":onIOException");
				e.printStackTrace();					
			}// end onIOException

			public void onFileNotFoundException(FileNotFoundException e,Object state) {
				Log.e(TAG_CLASS_PACKAGE,METHOD_NAME+":onFileNotFoundException");
				e.printStackTrace();				
			}// end onFileNotFoundException

			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				Log.e(TAG_CLASS_PACKAGE,METHOD_NAME+":onMalformedURLException");
				e.printStackTrace();				
			}// end onMalformedURLException

			public void onFacebookError(FacebookError e, Object state) {
				Log.e(TAG_CLASS_PACKAGE,METHOD_NAME+":onFacebookError");
				e.printStackTrace();					
			}// end onFacebookError	
			
		}// end class: FriendShipStatusCheckListener	
		
		
		// List of facebook IDs of offer owners without duplicates
		Set<String> ownersIds= offersFromUsers.keySet();
		
		// Get an iterator to loop the set of owners IDs for checking
		Iterator<String> ownersIdsIterator = ownersIds.iterator();
		
		// Create new listener for friendship status
		FriendShipStatusCheckListener friendshipStatusCheckListener = new FriendShipStatusCheckListener();
		/* Set number of offers that should be processed to signal when result should 
		 * be returned
		 */
		friendshipStatusCheckListener.totalNumberOfOffersRemaining = ownersIds.size();
		
		while(ownersIdsIterator.hasNext()){
			
			/* Issue an HTTP request to check if user and owner are friends or not 
			 * The request format to the graph API of facebook is : <me/friends/friendId>
			 * where:
			 * 		me= fixed keyword representing signed-in user
			 * 		friendId= facebook ID of offer owner to be checked
			 * The only permissions needed for operation to be performed is <access_token>
			 * to be granted
			 */
			asyncFacebookRunner.request("me/friends/"+ownersIdsIterator.next(), friendshipStatusCheckListener);	
						
		}// end while : check IDs of all offer owners		
		
		try {
			
			/* Since no permits are available -> thread will sleep till .release() is called
			 * .release() won't be called until all offers are processed 
			 * (look at onComplete method in the inner class)
			 * This is done to force sequential order for result returning
			 */
			friendshipStatusCheckListener.waitForAllOffersProcessing.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// end catch: in case of interruptions to the thread	
		
		// Return result list when all offers are analyzed
		return friendshipStatusCheckListener.ownersIdsFromFriends;
	}// end filterOffersFromFriends
	
	/**
	 * This method is used to filter offers coming from friends of user friends 
	 * from a more generic set of offers. This method will issue (N) HTTP requests 
	 * for checking whether owners have mutual friends with the user or not. 
	 * N is the number of owners to be checked. 
	 * 
	 * @param offersFromUsers
	 * 		Hash table where:
	 * 			<ul>
	 * 				<li>the <code>key</code> is Facebook ID of offer owner</li>
	 * 				<li>the <code>value</code> is object representing offer</li>
	 * 			</ul> 
	 * @return
	 * 		Hash table containing
	 * 		<ul>
	 * 				<li>the <code>key</code> is Facebook ID of requested offer owner</li>
	 * 				<li>the <code>value</code> is JSONObject representing the mutual friends
	 * 				between the user and the offer owner. 
	 * 				<br>To know the number of mutual friends between the user and an offer
	 * 				owner, use {@link JSONObject#length()}  method. 
	 * 				<br>To know the names of mutual friends between the user and an offer 
	 * 				owner, use {@link JSONObject#keys()}  method, then loop on them and call
	 * 				{@link JSONObject#getString(String)} method </li>
	 * 		</ul>
	 * 		In case no friends of friends were found in the input hash table, the hash table
	 * 		is returned empty (size=0). This map can be used later to index the 
	 * 		offers (in the input hash table for example) and retrieve them for
	 * 		displaying.
	 */
	public Hashtable<String, JSONObject> filterOffersFromFriendsOfFriends(Hashtable<String,Object> offersFromUsers){
			
		/**
		 * Inner class for listening to different actions while requesting
		 * mutual friends between the user and an owner of an offer
		 * 
		 * @author passant
		 *
		 */
		class MutualFriendsCheckListener implements RequestListener{
			
			//_____________________ Constants ____________________________
			
			/**
			 * Used for tracing purposes
			 */
			private final String METHOD_NAME = "filterOffersFromFriendsOfFriends(Hashtable<String,Object> offersFromUsers)";

			//_____________________ Instance parameters __________________
			
			// List of result of owners in friends of the user
			private Hashtable<String, JSONObject> ownersFromFriendsOfFriends = new Hashtable<String, JSONObject>();
			// Current Owner ID to be checked for friendship status with user
			private String currentOwnerFacebookId="";
			
			//_____________________ Different Actions _____________________
			
			public void onComplete(String response, Object state) {
				
				try {
					JSONObject receivedDataOfMutualFriends = new JSONObject(response);
					
					if (receivedDataOfMutualFriends.length()>0){
						ownersFromFriendsOfFriends.put(currentOwnerFacebookId, receivedDataOfMutualFriends);
					}// end if : if owner is a friend of user's friend => FB returns mutual friends between both 
				} catch (JSONException e) {
					Log.e(TAG_CLASS_PACKAGE,METHOD_NAME+":onComplete: Exception in parsing received data");
					e.printStackTrace();
				}// end catch
				
			}// end onComplete

			public void onIOException(IOException e, Object state) {
				Log.e(TAG_CLASS_PACKAGE,METHOD_NAME+":onIOException");
				e.printStackTrace();					
			}// end onIOException
			
			public void onFileNotFoundException(FileNotFoundException e,Object state) {
				Log.e(TAG_CLASS_PACKAGE,METHOD_NAME+":onFileNotFoundException");
				e.printStackTrace();				
			}// end onFileNotFoundException
			
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				Log.e(TAG_CLASS_PACKAGE,METHOD_NAME+":onMalformedURLException");
				e.printStackTrace();				
			}// end onMalformedURLException
			
			public void onFacebookError(FacebookError e, Object state) {
				Log.e(TAG_CLASS_PACKAGE,METHOD_NAME+":onFacebookError");
				e.printStackTrace();					
			}// end onFacebookError	
						
		}// end class: MutualFriendsCheckListener	
		
		
		// List of Facebook IDs of offer owners without duplicates
		Set<String> ownersIds= offersFromUsers.keySet();
		
		// Get an iterator to loop the set of owners IDs for checking
		Iterator<String> ownersIdsIterator = ownersIds.iterator();
		
		// Create new listener for friendship status
		MutualFriendsCheckListener mutualFriendsCheckListener = new MutualFriendsCheckListener();
		
		while(ownersIdsIterator.hasNext()){
			
			// Current Owner ID to be checked for friendship status with user
			mutualFriendsCheckListener.currentOwnerFacebookId= ownersIdsIterator.next();
			// String representing relation evaluation request in Facebook graph API format (friends or not)
			String userOwnerRelationRequest = "me/mutualfriends/"+mutualFriendsCheckListener.currentOwnerFacebookId;
			// Issue an HTTP request to check if user has mutual friends with owner or not 
			asyncFacebookRunner.request(userOwnerRelationRequest, mutualFriendsCheckListener);			
		}// end while : check IDs of all offer owners		
		
		return mutualFriendsCheckListener.ownersFromFriendsOfFriends;
	}// end filterOffersFromFriendsOfFriends
	
	
	private void tester_filterOffersFromFriends(){
		
		// Input list: cannot make value = null -> exception
		Hashtable<String, Object>offersFromUsers = new Hashtable<String, Object>();
		offersFromUsers.put("32529", new Object()); 	// friend
		offersFromUsers.put("48304588",new Object()); 	// friend
		offersFromUsers.put("58215973",new Object()); 	// friend
		offersFromUsers.put("1207059", new Object()); 	// non friend
		
		// Expected output : Arraylist having 32529 , 48304588 , 58215973
		// STATUS : SUCCESSFUL
		
		/* Comments: CANNOT TEST ON YOUR OWN ACCOUNTS : user- dependent */
		
		
		// Execute method
		ArrayList<String> friendsIds  = filterOffersFromFriends(offersFromUsers);
		
		// Display results
		String friendsIdsForDisplay = "friends IDs retrieved: ";		
		for (int i = 0 ; i < friendsIds.size() ; i++)
			friendsIdsForDisplay += friendsIds.get(i) + " , ";
		
		Log.e(TAG_CLASS_PACKAGE, "tester_filterOffersFromFriends: " + friendsIdsForDisplay);
		
	}// end tester_filterOffersFromFriends
	
	
	public void tester(){
		
		Hashtable<String, Object> x=null;
		boolean hasKey = x.containsKey("gjgdgf");
		boolean hasValue = x.containsValue("jhkjdf");
		boolean has = x.contains(new Object());
		
	
		
		/*RequestListener reqList = new RequestListener() {
			
			public ArrayList<String> myArray = new ArrayList<String>();
			
			@Override
			public void onMalformedURLException(MalformedURLException e, Object state) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onIOException(IOException e, Object state) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFileNotFoundException(FileNotFoundException e, Object state) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFacebookError(FacebookError e, Object state) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onComplete(String response, Object state) {
				myArray.add("HelloWorld");
				
			}
			
			
			public ArrayList<String> getMyArray(){
				return myArray;
			}
		}; */
		
		class MyRequestListener implements RequestListener{

			private ArrayList<String> myArray = new ArrayList<String>();
			
			public void onComplete(String response, Object state) {
				myArray.add("HelloWorld");
				
			}

			public void onIOException(IOException e, Object state) {
				// TODO Auto-generated method stub
				
			}

			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
				// TODO Auto-generated method stub
				
			}

			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				// TODO Auto-generated method stub
				
			}

			public void onFacebookError(FacebookError e, Object state) {
				// TODO Auto-generated method stub
				
			}
			
		}
		
		MyRequestListener reqList = new MyRequestListener();
		ArrayList<String> arr = reqList.myArray;
		
		
		
		
	}// end tester

}// end class