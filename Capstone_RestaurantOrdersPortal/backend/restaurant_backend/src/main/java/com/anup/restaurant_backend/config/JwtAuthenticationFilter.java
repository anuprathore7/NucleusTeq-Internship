package com.anup.restaurant_backend.config;
import com.anup.restaurant_backend.security.JwtService;
import com.anup.restaurant_backend.security.CustomUserDetailsService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 *  This filter runs on EVERY request
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    private boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = jwtService.extractEmail(token);
            return email.equals(userDetails.getUsername());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        /**
         * Get Authorization header
         */
        final String authHeader = request.getHeader("Authorization");

        /**
         * If no token → skip
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        /**
         * Extract token
         */
        String token = authHeader.substring(7);

        /**
         *  Extract email from token
         */
        String email = jwtService.extractEmail(token);

        /**
         * If user not already authenticated
         */
        // STEP 4: If user not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            //  IMPORTANT: validate token
            if (isTokenValid(token, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }



        /**
         * Continue request
         */
        filterChain.doFilter(request, response);
    }
}