package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.*;
import com.anup.restaurant_backend.security.JwtService;
import com.anup.restaurant_backend.service.AddressService;
import com.anup.restaurant_backend.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This test class checks all APIs of AddressController.
 * It simulates user requests like adding, fetching and deleting addresses.
 * No real database or security is used — everything is mocked.
 */
@WebMvcTest(
        controllers = AddressController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;



    /**
     * Tests fetching all addresses of a user.
     */
    @Test
    void get_addresses_success() throws Exception {

        when(addressService.getMyAddresses(any()))
                .thenReturn(List.of(new AddressResponseDto()));

        mockMvc.perform(get("/api/addresses")
                        .header("Authorization", "Bearer fake"))
                .andExpect(status().isOk());
    }

    /**
     * Tests adding a new address.
     */
    @Test
    void add_address_success() throws Exception {

        AddressResponseDto response = new AddressResponseDto();
        response.setId(1L);

        when(addressService.addAddress(any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer fake")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddressRequestDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    /**
     * Tests deleting an address.
     */
    @Test
    void delete_address_success() throws Exception {

        doNothing().when(addressService).deleteAddress(any(), any());

        mockMvc.perform(delete("/api/addresses/1")
                        .header("Authorization", "Bearer fake"))
                .andExpect(status().isOk())
                .andExpect(content().string("Address deleted successfully"));
    }
}