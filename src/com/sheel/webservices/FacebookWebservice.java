package com.sheel.webservices;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.acl.Owner;
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.FaceDetector.Face;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.sheel.app.NewUserActivity;
import com.sheel.app.SheelMaaayaClient;
import com.sheel.datastructures.FacebookUser;
import com.sheel.datastructures.OfferDisplay;
import com.sheel.datastructures.enums.OwnerFacebookStatus;
import com.sheel.datastructures.enums.SharedValuesBetweenActivities;
import com.sheel.listeners.AppDialogListener;
import com.sheel.listeners.AppRequestListener;
import com.sheel.listeners.OffersFilterListener;


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
	FacebookUser fbUser = new FacebookUser();

	
	/*
	 * --------------------------------------- Constructors ------------------------------------------
	 * 
	 */
	
	/**
	 * Default constructor
	 */
	public FacebookWebservice(){
		
	}// end constructor
	
	public FacebookWebservice (String facebookId, String accessToken , long expiryTime){
		// Set access token and expiry time
		this.facebook.setAccessToken(accessToken);
		this.facebook.setAccessExpires(expiryTime);
		
		// Set user ID
		this.fbUser = new FacebookUser(facebookId);
	}// end constructor
	/*
	 * --------------------------------------- Logic ------------------------------------------
	 * 
	 */
	
	/**
	 * Gets the facebook user (data structure) storing all data that was received about the user
	 * 
	 * @return 
	 * 		data structure having all information retrieved so far about user. If not initialized,
	 * 		it will return null
	 */
	public FacebookUser getFacebookUser(){
		return fbUser;
	}// end getFacebookUser
	
	/**
	 * Returns session status (whether user is still logged in 
	 * or not)
	 * 
	 * @return
	 *		<ul>
	 * 			<li><code>true</code>: session is valid</li>
	 * 			<li><code>false</code>: session has expired and you should log in</li>
	 * 		</ul>	
	 */
	public boolean isSessionValid(){
		return facebook.isSessionValid();
	}// end isSessionValid
	
	/**
	 * Returns facebook access token for generating issues on HTTP
	 * @return
	 */
	public String getUserAccessToken(){
		return facebook.getAccessToken();
	}// end getUserAccessToken
	
	/**
	 * Returns after how many milliseconds will the session expire
	 * @return
	 */
	public long getUserAccessTokenExpiryTime(){
		return facebook.getAccessExpires();
	}// end getUserAccessTokenExpiryTime
	
	/**
	 * Open login window in browser for signing-in facebook and app and optionally
	 * retrieve user basic information. If user has facebook app on the mobile, it
	 * will do auto single-sign on. It guarantees the basic permissions needed for 
	 * the app the first time user registers. Permission covered is: email 
	 * 
	 * @param parentActivity
	 * 		activity that the user will be diverted to after signing-in
	 * @param getUserInfo
	 * 		<ul>
	 * 			<li><code>true</code>: retrieve user basic information
	 * 			after signing in. See 
	 * 			{@link FacebookWebservice#getUserInformation(boolean)} for more 
	 * 			information about retrieved data.</li>
	 * 			<li><code>false</code>: just sign in without retrieving data</li>
	 * 		</ul>
	 * @param isInfoForApp
	 * 		<ul>
	 * 			<li><code>true</code>: retrieve user basic information
	 * 			related to APP ONLY. See 
	 * 			{@link FacebookWebservice#getUserInformation(boolean)} for more 
	 * 			information about retrieved data.</li>
	 * 			<li><code>false</code>: retrieve all public information of the user</li>
	 * 		</ul>	
	 */
	public void login(Activity parentActivity ,final boolean getUserInfo , final boolean isInfoForApp){
		
		/**
		 * Inner class for listening to different 
		 * events relevant to login process dialog
		 * 
		 * @author passant
		 *
		 */
		class LoginListener extends AppDialogListener{
					
			@Override
			public void onComplete(Bundle values) {
				Log.e(TAG_CLASS_PACKAGE,"login2: onComplete: Login successful" );
				if (getUserInfo){
					Log.e("passant: ", "getUserInformation");
					getUserInformation(isInfoForApp);					
				}// end if : 1st time to log in -> get user data
				
			}// end onComplete	
		
		}// end class
		
		if (!facebook.isSessionValid()){
			
			Log.e(TAG_CLASS_PACKAGE,"Login2: session expired");
			
			//String[] permissions = new String[]{"email","user_about_me"};
			String[] permissions = new String[]{"email"};
			facebook.authorize(parentActivity, permissions, new LoginListener());
			
		}// end if : session is ended -> non access token -> request new one
	
	}// end login
	
	/**
	 * Ends the current session of user in the app
	 * 
	 * @param parentActivity
	 * 		activity where the logout is called upon to retrieve context
	 */
	public void logout(Activity parentActivity){
		asyncFacebookRunner.logout(parentActivity.getApplicationContext(),new AppRequestListener());
	}// end logout
	
	/**
	 * Ends the current session of user in the app
	 * 
	 * @param c
	 * 		application context
	 */
	public void logout(Context c){
		asyncFacebookRunner.logout(c,new AppRequestListener());
	}// end logout
	
	/**
	 * Used to retrieve user basic information. User must be signed in
	 * and having active access_token for the method to perform properly.
	 * The information is loaded and can be retrieved using
	 * {@link FacebookWebservice#getFacebookUser()}
	 * 
	 * @param isForApp
	 * 		<ul>
	 * 			<li><code>true</code>: retrieve user basic information
	 * 			related to APP ONLY. See {@link FacebookUser} for more 
	 * 			information about retrieved data.</li>
	 * 			<li><code>false</code>: retrieve all public information 
	 * 			of the user</li>
	 * 		</ul>	
	 */
	public void getUserInformation(boolean isForApp){
		
		class BasicInfoListener extends AppRequestListener{
			
			Semaphore dataIsReceived = new Semaphore(0);
			
			@Override
			public void onComplete(String response, Object state) {
				Log.e(TAG_CLASS_PACKAGE,"getUserInformation: onComplete: LoggedIn user response=" + response);
				fbUser = new FacebookUser(response,true);
				Log.e(TAG_CLASS_PACKAGE,"getUserInformation: onComplete: LoggedIn user=" + fbUser);
				dataIsReceived.release();
			}// end onComplete
			
			public Semaphore getSemaphore(){
				return this.dataIsReceived;
			}
		}// end class
		
		if (facebook.isSessionValid()){
			String fields="";
			
			if (isForApp){
				fields="?fields=id,first_name,middle_name,last_name,gender,verified,email&";
			}// end if: get only needed parameters for the app
			
			BasicInfoListener listener = new BasicInfoListener();
			asyncFacebookRunner.request("me"+fields, listener);
			blockThreadUntilAllOffersAreProcessed(listener.getSemaphore());
		}// end if : get information if session is valid
		
	}// end getUserInformationForApp
		
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
	 * 		Hashtable of search results having owners who are friends with app
	 * 		user. If no owners who are friends were found, list is returned
	 * 		empty.
	 *		Hash table where:
	 * 			<ul>
	 * 				<li>the <code>key</code> is Facebook ID of offer owner</li>
	 * 				<li>the <code>value</code> is object representing offer 
	 * 				and user details needed to display a search result. By default
	 * 				it will check its facebook status to
	 * 				@link {@link OwnerFacebookStatus#FRIEND}</li>
	 * 			</ul> 
	 */
	public Hashtable<String,OfferDisplay>  filterOffersFromFriends(Hashtable<String,OfferDisplay> offersFromUsers){
		
		/**
		 * Inner class for listening to different actions while requesting
		 * friendship status between the user and an owner of an offer
		 * 
		 * @author passant
		 *
		 */
		class FriendShipStatusCheckListener extends OffersFilterListener{

			/**
			 * Constructor
			 * @param offersFromUsers
			 * 		Hash table where:
			 * 		<ul>
			 * 			<li>the <code>key</code> is Facebook ID of requested offer owner</li>
			 * 			<li>the <code>value</code> is object representing offer</li>
			 * 		</ul> 
			 * @param classPackageName
			 * 		class name (package name) where listener is called. It is used
			 * 		for tracing purposes in the log messages	
			 * @param methodName
			 * 		method name where the listener is used. It is used
			 * 		for tracing purposes in the log messages	 
			 */
			public FriendShipStatusCheckListener(Hashtable<String, OfferDisplay> offersFromUsers,
					String classPackageName, String methodName) {
				super(offersFromUsers, classPackageName, methodName);
				
			}// end constructor
			
			@Override
			public void processRequest (JSONObject parsedResponse, Object state) {
				// get data relevant to mutual friends
				JSONObject receivedDataOfFriend = extractDataJsonObject(parsedResponse);
				if (receivedDataOfFriend != null){	
					try{
						// Get owner facebook ID currently checked if a friend or not
						String ownerId = receivedDataOfFriend.getString("id");
						// Get the OfferDisplay of that owner and save in result
						addFilteredOfferDisplay(ownerId,OwnerFacebookStatus.FRIEND);
						generateLogMessage(": processRequest: New offer from friend added ->ownerID:" + ownerId);
					}catch(JSONException e){
						generateLogMessage("processRequest: Error: could not get id ");
						e.printStackTrace();
					}// end catch
							
				}/* end if : if both are friends => FB returns FB ID of checked friend mapped to his/her name */
			}// end processRequest			
		}// end class
		
		// Used for tracing purposes
		String methodName = "filterOffersFromFriends (Hashtable<String,OfferDisplay> offersFromUsers)";
		
		// Create new listener for friends of friends status
		FriendShipStatusCheckListener friendshipStatusCheckListener = new FriendShipStatusCheckListener(offersFromUsers,TAG_CLASS_PACKAGE,methodName);
			
		/* Send HTTP request. Graph API path: me/friends/friendId.  
		 * The only permissions needed for operation to be performed is <access_token>
		 * to be granted 
		 */
		requestOfferFilteringByRelationBetweenAppUserAnd(offersFromUsers, "friends", friendshipStatusCheckListener, false);
		
		blockThreadUntilAllOffersAreProcessed(friendshipStatusCheckListener.getSemaphore());
		
		return friendshipStatusCheckListener.getFilteredOffers();
	}// end filterOffersFromOwnersWithMutualFriends
		
	/**
	 * This method is used to filter offers coming from offer owners with mutual 
	 * friends with the app user from a more generic set of offers. This method
	 * will issue (N) HTTP requests for checking whether owners have mutual
	 * friends with the user or not.  (N) is the number of owners to be checked.
	 * 
	 * @param offersFromUsers
	 * 		Hash table where:
	 * 			<ul>
	 * 				<li>the <code>key</code> is Facebook ID of offer owner</li>
	 * 				<li>the <code>value</code> is object representing offer 
	 * 				and user details needed to display a search result</li>
	 * 			</ul> 
	 * @return
	 * 		Hashtable of search results having owners with common friends with app
	 * 		user. If no owners with mutual friends were found, list is returned
	 * 		empty.
	 *		Hash table where:
	 * 			<ul>
	 * 				<li>the <code>key</code> is Facebook ID of offer owner</li>
	 * 				<li>the <code>value</code> is object representing offer 
	 * 				and user details needed to display a search result. By default
	 * 				it will check its facebook status to
	 * 				@link {@link OwnerFacebookStatus#FRIEND_OF_FRIEND}</li>
	 * 			</ul> 
	 */	
	public Hashtable<String,OfferDisplay>  filterOffersFromOwnersWithMutualFriends (Hashtable<String,OfferDisplay> offersFromUsers){
		
		/**
		 * Inner class for listening to different actions while requesting
		 * mutual friends between the user and an owner of an offer
		 * 
		 * @author passant
		 *
		 */
		class MutualFriendsCheckListener extends OffersFilterListener{

			/**
			 * Constructor
			 * @param offersFromUsers
			 * 		Hash table where:
			 * 		<ul>
			 * 			<li>the <code>key</code> is Facebook ID of requested offer owner</li>
			 * 			<li>the <code>value</code> is object representing offer</li>
			 * 		</ul> 
			 * @param classPackageName
			 * 		class name (package name) where listener is called. It is used
			 * 		for tracing purposes in the log messages	
			 * @param methodName
			 * 		method name where the listener is used. It is used
			 * 		for tracing purposes in the log messages	 
			 */
			public MutualFriendsCheckListener(Hashtable<String, OfferDisplay> offersFromUsers,
					String classPackageName, String methodName) {
				super(offersFromUsers, classPackageName, methodName);
				
			}// end constructor
			
			@Override
			public void processRequest(JSONObject parsedResponse, Object state) {
				// get data relevant to mutual friends
				JSONObject receivedDataOfMutualFriends = extractDataJsonObject(parsedResponse);
				if (receivedDataOfMutualFriends != null){	
					// Get owner ID currently checked for mutual friends
					String ownerId = (String)state;
					generateLogMessage(": onComplete: has mutual friends ownerId: " + ownerId);
					// Get the OfferDisplay of that owner and save mutual friends
					getOfferDisplayBy(ownerId).setFacebookExtraInfo(receivedDataOfMutualFriends);
					addFilteredOfferDisplay(ownerId,OwnerFacebookStatus.FRIEND_OF_FRIEND);
					generateLogMessage(": onComplete: Extra info set for ownerId " + ownerId);		
				}// end if : if owner and user have common friends => FB returns mutual friends between both
			}// end processRequest			
		}// end class
		
		// Used for tracing purposes
		String methodName = "filterOffersFromOwnersWithMutualFriends (Hashtable<String,OfferDisplay> offersFromUsers)";
		
		// Create new listener for friends of friends status
		MutualFriendsCheckListener mutualFriendsCheckListener = new MutualFriendsCheckListener(offersFromUsers,TAG_CLASS_PACKAGE,methodName);
			
		/* Send HTTP request. Graph API path: me/mutualfriends/friendId.  
		 * The only permissions needed for operation to be performed is <access_token>
		 * to be granted 
		 */
		requestOfferFilteringByRelationBetweenAppUserAnd(offersFromUsers, "mutualfriends", mutualFriendsCheckListener, true);
		
		blockThreadUntilAllOffersAreProcessed(mutualFriendsCheckListener.getSemaphore());
		
		return mutualFriendsCheckListener.getFilteredOffers();
	}// end filterOffersFromOwnersWithMutualFriends2
	
	/**
	 * IMPORTANT: This method must be invoked at the top of the calling
     * activity's onActivityResult() function or Facebook authentication will
     * not function properly! (FROMO FACEBOOK SDK)
     * It must be called in (onActivityResult) method "overriden method
     * in activity handling authentication
     * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void authorizeCallback(int requestCode, int resultCode, Intent data){
		facebook.authorizeCallback(requestCode, resultCode, data);
	}// end authorizeCallback
	
	/**
	 * Used to filter all elements of <code>needsFiltering</code> from <code>main</code>
	 * @param main
	 * 		Reference hashtable that will be kept as is
	 * @param needsFiltering
	 * 		Hashtable that will get filtered (elements existing in main will be removed)
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
	 * IMPORTANT: Before leaving any activity, you must call this  method to pass
	 * the important details about session and user 
	 * 
	 * @param currentActivity
	 * 		Activity that is about to be left
	 * @param typeOfNextActivity
	 * 		Class type of new activity you intend to navigate to. Write activity name
	 * 		then call .getClass() method
	 * @param currentWebService
	 * 		Webservice in the current activity that data will be taken from 
	 * 
	 * @return
	 * 		Intent containing data needed for the next activity. Use 
	 * 		{@link Activity#startActivity(Intent)} to start the new activity
	 */
	public static Intent SetSessionInformationBetweenActivities (Activity currentActivity , Class<?> typeOfNextActivity, FacebookWebservice currentWebService){

		   Intent mIntent = new Intent(currentActivity, typeOfNextActivity);
			// Pass variable to detailed view activity using the intent
			mIntent.putExtra(SharedValuesBetweenActivities.userFacebookId.name(), currentWebService.fbUser.getUserId());
			mIntent.putExtra(SharedValuesBetweenActivities.userAccessToken.name(), currentWebService.getUserAccessToken());
			mIntent.putExtra(SharedValuesBetweenActivities.accessTokenExpiry.name(), currentWebService.getUserAccessTokenExpiryTime());
			
			return mIntent;
	}// end SetSessionInformationBetweenActivities
	
	/**
	 * Helper method for acquiring a semaphore with the try and 
	 * catch needed for handling exceptions
	 * 
	 * @param s
	 * 	Semaphore that will trying acquiring a permit
	 */
	private void blockThreadUntilAllOffersAreProcessed(Semaphore s){
		try {
			/* Since no permits are available -> thread will sleep till .release() is called
			 * .release() won't be called until all offers are processed 
			 * (look at onComplete method in the inner class)
			 * 
			 * This is done to allow parallel processing of results but force sequential order
			 * for result returning
			 */
			s.acquire();
		} catch (InterruptedException e) {
			Log.e(TAG_CLASS_PACKAGE,"blockThreadUntilAllOffersAreProcessed: semaphore was interrupted");
			e.printStackTrace();
		}// end catch
	}// end blockThreadUntilAllOffersAreProcessed
	
	/**
	 * Helper method used as to perform the same connection status 
	 * request between the app user and set of owners for offers
	 * to filter them. The method does the REQUEST part only, while
	 * the listener does the FILTERING PART
	 * 
	  * @param offersFromUsers
	 * 		Hash table where:
	 * 			<ul>
	 * 				<li>the <code>key</code> is Facebook ID of offer owner</li>
	 * 				<li>the <code>value</code> is object representing offer 
	 * 				and user details needed to display a search result</li>
	 * 			</ul> 
	 * @param filterName
	 * 		Name of connection that will be used as the path in the
	 * 		graph API. 
	 * 		<br><br>The format is:<code>me/FILTER_NAME/ownerFbId</code>,
	 *  	where:
	 * 		me= fixed keyword representing signed-in user
	 * 		friendId= facebook ID of offer owner to be checked
	 * 		<br><br>Available options are: 
	 * 		<code>friends</code>,<code>mutualfriends</code><br><br>
	 * @param listener
	 * 		Listener that handles the results upon receiving them from 
	 * 		the facebook server. It should contain all the filtering logic
	 * @param isSendOwnerIdInState
	 * 			<ul>
	 * 				<li><code>True</code>: send owner ID in the state variable
	 * 				to be accessible on receiving a response</li>
	 * 				<li><code>False</code>: leave state variable null</li>
	 * 			</ul> 
	 */
	private void requestOfferFilteringByRelationBetweenAppUserAnd(
			Hashtable<String, OfferDisplay> offersFromUsers, 
			String filterName, OffersFilterListener listener,
			boolean isSendOwnerIdInState){
		
		// Get an iterator to loop the set of owners IDs for checking
		Iterator<String> ownersIdsIterator = offersFromUsers.keySet().iterator();
	
		while(ownersIdsIterator.hasNext()){
			
			// Get owner ID
			String currentOwnerFacebookId = ownersIdsIterator.next();
			
			/* Issue an HTTP request to check if an offer owner and app user have
			 * a certain relation/connection			 
			 */
			if (isSendOwnerIdInState){
				asyncFacebookRunner.request("me/"+filterName+ "/"+currentOwnerFacebookId, listener, currentOwnerFacebookId);
			}// end if: send ownerID in state
			else{
				asyncFacebookRunner.request("me/"+filterName+ "/"+currentOwnerFacebookId, listener);
			}// end if: leave state null
			
		}// end while : check IDs of all offer owners
	}// end requestOfferFilteringByRelationBetweenAppUserAnd
	
	/**
	 * Helper method used to parse some responses from facebook server.
	 * Some responses are formatted as 2 arrays: one containing data and 
	 * another containing pagination information. The method extracts the
	 * data part and returns it as a JSONObject
	 * 
	 * @param receviedResponse
	 * 		the response received from the facebook server, parsed to
	 * 		a JSONObject
	 * 
	 * @return
	 * 		data object. If not found (no data was transfered) or 
	 * 		in case of exceptions, it will return null
	 */
	private JSONObject extractDataJsonObject(JSONObject receviedResponse){
		
		// Get friend data	
		JSONArray responseData;
		try {
			Log.e("passant", "extractDataJsonObject receivedResponse: " + receviedResponse);
			responseData = receviedResponse.getJSONArray("data");
		
			if (responseData.length() > 0){
				return responseData.getJSONObject(0);				
			}// end if: array has any data -> make it an object
			else{
				return null;
			}// end else: no data was sent
		} catch (JSONException e) {
			Log.e(TAG_CLASS_PACKAGE,"extractDataJsonObject(JSONObject receviedResponse): error in JSON parsing");
			e.printStackTrace();
			return null;
		}// end catch
				
	}// end extractDataJsonObject
	
	
	// _________________________________ will take code snippets from it____
	
	private void loginOld (Activity parentActivity){
		
		/*
		 * ########################################################## 
		 * # If the user is already logged in -> use his credentials#
		 * ##########################################################
		 */
		
	/*	// Get the shared preferences of the user from the activity
		sharedPreferences = parentActivity.getPreferences(Context.MODE_PRIVATE);

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
		
		
		sharedPreferences = parentActivity.getPreferences(Context.MODE_PRIVATE); */
		
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
					 * Called when the Log in for FB + APP is successful
					 * -> basic data of user is there
					 */
					Log.e(TAG_CLASS_PACKAGE, "login : onComplete");
					// Log.e(ERROR_TAG,"Welcome "+name);

					/*
					 * Edit the shared preferences and add to them the user
					 * access token and its expiry date
					 * 
					 * It is used to avoid showing the user a transition
					 * dialog between facebook and app during the same 
					 * session
					 */
					
				/*	SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(SP_ACCESS_TOKEN, facebook.getAccessToken());
					editor.putLong(SP_ACCESS_TOKEN_EXPIRY,
							facebook.getAccessExpires());
					editor.commit();*/
					//getUserInfoForApp();

					methodTester();
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
		else{
			//tester_filterOffersFromOwnersWithMutualFriends();
			methodTester();
		}
		
	}// end login
	
	// ________________________________TESTING METHODS________________
	
	private void methodTester(){
		tester_filterOffersFromFriends();
		//tester_filterOffersFromOwnersWithMutualFriends();
	}
	
	private void tester_filterOffersFromFriends(){
		
		// Input list: cannot make value = null -> exception
		Hashtable<String, OfferDisplay>offersFromUsers = new Hashtable<String, OfferDisplay>();
		offersFromUsers.put("32529", new OfferDisplay("32529","ofr1",OwnerFacebookStatus.UNRELATED)); 	// friend
		offersFromUsers.put("48304588",new OfferDisplay("48304588","ofr2",OwnerFacebookStatus.UNRELATED)); 	// friend
		offersFromUsers.put("58215973",new OfferDisplay("58215973","ofr3",OwnerFacebookStatus.UNRELATED)); 	// friend
		offersFromUsers.put("1207059", new OfferDisplay("1207059","ofr4",OwnerFacebookStatus.UNRELATED)); 	// non friend
		
		// Expected output : Arraylist having 32529 , 48304588 , 58215973
		// STATUS : SUCCESSFUL
		
		/* Comments: CANNOT TEST ON YOUR OWN ACCOUNTS : user- dependent */
		
		
		// Execute method
		Hashtable<String, OfferDisplay> friendsIds  = filterOffersFromFriends(offersFromUsers);
		
		// Display results
		//String friendsIdsForDisplay = "friends IDs retrieved: ";		
		//for (int i = 0 ; i < friendsIds.size() ; i++)
			//friendsIdsForDisplay += friendsIds.get(i) + " , ";
		
		//Log.e(TAG_CLASS_PACKAGE, "tester_filterOffersFromFriends: " + friendsIdsForDisplay);
		System.out.println("Number of offers from friends: " + friendsIds.size());
		System.out.println("Friends results: " + friendsIds);
	}// end tester_filterOffersFromFriends
	
	private void tester_filterOffersFromOwnersWithMutualFriends(){
		
		// Input list
		OwnerFacebookStatus fbStatus = OwnerFacebookStatus.FRIEND_OF_FRIEND;
		String usrId1 = "32529"; 	// will have mutual friends
		String usrId2 = "48304588";	// will have mutual friends
		String usrId3 = "1207059";	// no mutual friends
		
		OfferDisplay ofr1 = new OfferDisplay(usrId1,"1",fbStatus);
		OfferDisplay ofr2 = new OfferDisplay(usrId2,"2",fbStatus);
		OfferDisplay ofr3 = new OfferDisplay(usrId1,"3",fbStatus);
		
		Hashtable<String, OfferDisplay> offersFromUsers = new Hashtable<String, OfferDisplay>();
		offersFromUsers.put(usrId1, ofr1);
		offersFromUsers.put(usrId2, ofr2);
		offersFromUsers.put(usrId3, ofr3);
		
		// Expected output : Arraylist having ofr1 , ofr2 
		// STATUS : SUCCESSFUL
		
		/* Comments: CANNOT TEST ON YOUR OWN ACCOUNTS : user- dependent
		 * 
		 * Bugs:
		 *  	1) Sync as result is returned before processing all
		 *  	offers -> FIXED
		 *  	2) Data should be extracted from response and not the whole
		 *  	response including pagination -> FIXED
		 *  	
		 */
		
		// call method
		Hashtable<String, OfferDisplay> result = filterOffersFromOwnersWithMutualFriends(offersFromUsers);
		
		Log.e(TAG_CLASS_PACKAGE,"tester_filterOffersFromOwnersWithMutualFriends: result retrieved ");
		System.out.println("Result is: " + result);
		//for (int i=0 ; i<result.size(); i++)
			//Log.e(TAG_CLASS_PACKAGE,"tester_filterOffersFromOwnersWithMutualFriends: OfferDisplay: " + result.get(i));
			//System.out.println(result.get(i));
		
	}// end tester_filterOffersFromOwnersWithMutualFriends
	
	public void tester_removeDuplicates(){
		
		// Input	
		Hashtable<String, OfferDisplay> main = new Hashtable<String, OfferDisplay>();
		main.put("1", new OfferDisplay());
		main.put("2", new OfferDisplay());
		
		Hashtable<String, OfferDisplay> filter = new Hashtable<String, OfferDisplay>();
		filter.put("1", new OfferDisplay());
		filter.put("2", new OfferDisplay());
		filter.put("3", new OfferDisplay("myOwner", "myOffer",OwnerFacebookStatus.FRIEND ));
		
		// Expected output : filter having only 1 element (last one) 
				// STATUS : SUCCESS
				
				/* Comments:
				 *  	
				 */
		
		// call method
		removeDuplicates(main, filter);
		
		for (int i=0 ; i<filter.size() ; i++)
			System.out.println("Result: " + filter);
		
	}
	
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
	
	private void tester2(){
		
		class Client2 extends SheelMaaayaClient{

			@Override
			public void doSomething() {
				// TODO Auto-generated method stub
				
			}
			
		}// end class
		
		Client2 c= new Client2();
		c.runHttpRequest("path");
	}// end tester2

	
	
}// end class