package com.anup.restaurant_backend.entity;
import com.anup.restaurant_backend.enums.UserRole;
import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "users")
// It represents "users" table in database
public class UserEntity {

    @Id // using this we make primary id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // using this it will be incremented automatically.
    //  this is primary key it will auto Increment
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    //  It ensures no duplicate emails
    private String email;

    private String password;

    private String phone;

    @Enumerated(EnumType.STRING)
    private UserRole role;
    // It will be the role weather it is User or Admin

    private Double walletBalance;
    //  here users Balance will be generated.

    //  One user → many addresses
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;
    //  Default constructor (required by JPA)
    public UserEntity() {
    }

    //  Parameterized constructor (optional but useful)
    public UserEntity(Long id, String firstName, String lastName, String email,
                String password, String phone, UserRole role, Double walletBalance) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.walletBalance = walletBalance;
    }

    //  GETTERS AND SETTERS (manually written)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }

    public List<Address> getAddresses() { return addresses; }
}