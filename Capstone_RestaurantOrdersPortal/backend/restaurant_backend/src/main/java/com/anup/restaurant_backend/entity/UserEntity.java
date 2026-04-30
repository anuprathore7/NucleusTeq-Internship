package com.anup.restaurant_backend.entity;

import com.anup.restaurant_backend.enums.UserRole;
import jakarta.persistence.*;

import java.util.List;

/**
 * Entity representing a registered user stored in the "users" table.
 * Covers both customers and restaurant owners.
 */
@Entity
@Table(name = "users")
public class UserEntity {

    /** Primary key, auto-incremented by the database. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    /** Unique email address used for login and identification. */
    @Column(unique = true)
    private String email;

    private String password;
    private String phone;

    /** Role of the user: either CUSTOMER or RESTAURANT_OWNER. */
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /** Wallet balance used for paying orders. */
    private Double walletBalance;

    /** All delivery addresses saved by this user. */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;

    /** Default constructor required by JPA. */
    public UserEntity() {}

    /** Parameterized constructor for creating a user with all fields. */
    public UserEntity(Long id, String firstName, String lastName, String email,
                      String password, String phone, UserRole role, Double walletBalance) {
        this.id            = id;
        this.firstName     = firstName;
        this.lastName      = lastName;
        this.email         = email;
        this.password      = password;
        this.phone         = phone;
        this.role          = role;
        this.walletBalance = walletBalance;
    }

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getFirstName()                 { return firstName; }
    public void setFirstName(String firstName)   { this.firstName = firstName; }

    public String getLastName()                  { return lastName; }
    public void setLastName(String lastName)     { this.lastName = lastName; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public String getPassword()                  { return password; }
    public void setPassword(String password)     { this.password = password; }

    public String getPhone()                     { return phone; }
    public void setPhone(String phone)           { this.phone = phone; }

    public UserRole getRole()                    { return role; }
    public void setRole(UserRole role)           { this.role = role; }

    public Double getWalletBalance()             { return walletBalance; }
    public void setWalletBalance(Double w)       { this.walletBalance = w; }

    public List<Address> getAddresses()          { return addresses; }
    public void setAddresses(List<Address> a)    { this.addresses = a; }
}