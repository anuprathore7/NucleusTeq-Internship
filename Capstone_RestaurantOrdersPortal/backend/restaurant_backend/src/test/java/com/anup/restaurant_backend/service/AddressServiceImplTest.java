package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.AddressRequestDto;
import com.anup.restaurant_backend.entity.Address;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.repository.AddressRepository;
import com.anup.restaurant_backend.repository.UserRepository;
import com.anup.restaurant_backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for AddressServiceImpl.
 *
 * This class verifies all address-related operations such as:
 * fetching addresses, adding new ones, and deleting them.
 * It also ensures that only the owner of an address can delete it.
 */
class AddressServiceImplTest {

    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @InjectMocks private AddressServiceImpl addressService;

    private UserEntity user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new UserEntity();
        user.setId(1L);
        user.setEmail("test@mail.com");

        when(jwtService.extractEmail(anyString())).thenReturn(user.getEmail());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    }

    /**
     * Should return list of addresses for logged-in user.
     */
    @Test
    void getMyAddresses_success() {
        Address address = new Address();
        address.setId(1L);
        address.setCity("Delhi");
        address.setUser(user);

        when(addressRepository.findByUserId(1L)).thenReturn(List.of(address));

        var result = addressService.getMyAddresses("Bearer token");

        assertEquals(1, result.size());
        assertEquals("Delhi", result.get(0).getCity());
    }

    /**
     * Should successfully add a new address.
     */
    @Test
    void addAddress_success() {
        AddressRequestDto dto = new AddressRequestDto();
        dto.setStreet("Street");
        dto.setCity("City");
        dto.setState("State");
        dto.setPincode("123456");

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(i -> i.getArgument(0));

        var result = addressService.addAddress(dto, "Bearer token");

        assertEquals("City", result.getCity());
    }

    /**
     * Should delete address when user owns it.
     */
    @Test
    void deleteAddress_success() {
        Address address = new Address();
        address.setId(1L);
        address.setUser(user);

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        addressService.deleteAddress(1L, "Bearer token");

        verify(addressRepository).deleteById(1L);
    }

    /**
     * Should throw error when deleting someone else's address.
     */
    @Test
    void deleteAddress_notAuthorized() {
        UserEntity other = new UserEntity();
        other.setId(99L);

        Address address = new Address();
        address.setUser(other);

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.deleteAddress(1L, "Bearer token"));

        assertTrue(ex.getMessage().contains("not authorized"));
    }
}