package com.sheel.datastructures;



public class Offer
{

	public Long id;
	
	/**
	 * Flight ID for the offer (If available)
	 */
	public Long flightId;
	
	/**
	 * User ID for the offer (If available)
	 * Normally, this is the ID of the user using the application,
	 * and it should not be needed since user-database interactions
	 * are recorded on the server side.
	 */
	public Long userId;

	public float pricePerKilogram;

    public float noOfKilograms;

    public int userStatus;  //1 for Extra weight and 0 for Less weight

    public String offerStatus; 
    
    //public String currency; //Will be done next sprint
    
    public Offer(float noOfKilograms,
    		float pricePerKilogram,
            int userStatus,
            String offerStatus) 
    {
    	this.noOfKilograms = noOfKilograms;
        this.pricePerKilogram = pricePerKilogram;
        this.userStatus = userStatus;
        this.offerStatus = offerStatus;

    } 

    public float getNoOfKilograms() {
        return noOfKilograms;
    }

    public void setNoOfKilograms(float noOfKilograms) {
        this.noOfKilograms = noOfKilograms;
    }

    public String getOfferStatus() {
        return offerStatus;
    }

    public void setOfferStatus(String offerStatus) {
        this.offerStatus = offerStatus;
    }

    public float getPricePerKilogram() {
        return pricePerKilogram;
    }

    public void setPricePerKilogram(float pricePerKilogram) {
        this.pricePerKilogram = pricePerKilogram;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }
    

	@Override
	public String toString() {
		return "Offer [id=" + id + ", noOfKilograms=" + noOfKilograms +
				", pricePerKilogram=" + pricePerKilogram +
				", userStatus=" + userStatus + ", offerStatus=" + offerStatus + "]";
	}
	
}