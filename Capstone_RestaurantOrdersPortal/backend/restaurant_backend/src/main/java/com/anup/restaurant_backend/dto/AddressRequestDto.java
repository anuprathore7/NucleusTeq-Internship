package com.anup.restaurant_backend.dto;

/**
 * DTO for receiving address data from the client when adding or updating an address.
 */
public class AddressRequestDto {

    private String street;
    private String city;
    private String state;
    private String pincode;

    public AddressRequestDto() {}

    public String getStreet()  { return street; }
    public String getCity()    { return city; }
    public String getState()   { return state; }
    public String getPincode() { return pincode; }

    public void setStreet(String street)   { this.street = street; }
    public void setCity(String city)       { this.city = city; }
    public void setState(String state)     { this.state = state; }
    public void setPincode(String pincode) { this.pincode = pincode; }
}