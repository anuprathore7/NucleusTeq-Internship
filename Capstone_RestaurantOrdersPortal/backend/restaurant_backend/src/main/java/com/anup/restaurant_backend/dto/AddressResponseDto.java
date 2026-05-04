package com.anup.restaurant_backend.dto;

/**
 * DTO for sending address data back to the client.
 */
public class AddressResponseDto {

    private Long id;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String fullAddress;

    public AddressResponseDto() {}

    public AddressResponseDto(Long id, String street, String city,
                              String state, String pincode) {
        this.id          = id;
        this.street      = street;
        this.city        = city;
        this.state       = state;
        this.pincode     = pincode;
        this.fullAddress = street + ", " + city + ", " + state + " - " + pincode;
    }

    public Long   getId()          { return id; }
    public String getStreet()      { return street; }
    public String getCity()        { return city; }
    public String getState()       { return state; }
    public String getPincode()     { return pincode; }
    public String getFullAddress() { return fullAddress; }

    public void setId(long id) {
        this.id = id;
    }
}