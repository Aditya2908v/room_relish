package org.example.roomrelish.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults());
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "/api/v1/customer/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/customer/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/customer/hello").hasAuthority("USER")
                .requestMatchers(HttpMethod.POST, "/api/v1/customer/addCard").hasAuthority("USER")
                .requestMatchers(HttpMethod.GET, "/api/v1/customer/customers").hasAuthority("USER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/customer/update").hasAuthority("USER")
                .requestMatchers(HttpMethod.GET, "/api/v1/customer/profile-details").hasAuthority("USER")
                .requestMatchers(HttpMethod.GET, "/api/v1/customer/favouriteHotels").hasAuthority("USER")
                .requestMatchers(HttpMethod.POST, "/api/v1/customer/favouriteHotels/add").hasAuthority("USER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/customer/favouriteHotels/delete").hasAuthority("USER")
                .requestMatchers(HttpMethod.GET, "/api/v1/customer/recent").hasAuthority("USER")

                //Hotel Controller
                .requestMatchers(HttpMethod.POST, "/api/v1/hotel/createHotel").hasAuthority("USER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/hotel/updateHotel").hasAuthority("USER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/hotel/deleteHotel").hasAuthority("USER")
                .requestMatchers(HttpMethod.GET, "/api/v1/hotel/hello").hasAuthority("USER")
                .requestMatchers(HttpMethod.GET, "/api/v1/hotel/hotels").hasAuthority("USER")
                .requestMatchers(HttpMethod.GET, "/api/v1/hotel/hotel").hasAuthority("USER")
                .requestMatchers(HttpMethod.GET, "/api/v1/hotel/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/hotel/reviews").hasAuthority("USER")
                .requestMatchers(HttpMethod.POST, "/api/v1/hotel/addReview").hasAuthority("USER")
                .requestMatchers(HttpMethod.POST, "/api/v1/hotel/addRoom").hasAuthority("USER")

                //graphql
                .requestMatchers("/graphql").permitAll()
                .requestMatchers("/graphiql").permitAll()

                //for swagger ui
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()

                //Booking Controller
                .requestMatchers(HttpMethod.POST, "/api/v1/booking/bookingDetails").hasAuthority("USER")
                //Payment Controller
                .requestMatchers(HttpMethod.POST, "api/v1/payment/pay").hasAuthority("USER")
                .requestMatchers(HttpMethod.GET, "api/v1/payment/myBookings").hasAuthority("USER")
                .requestMatchers(HttpMethod.DELETE, "api/v1/payment/deleteMyBooking").hasAuthority("USER")
                .anyRequest().authenticated());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout.logoutUrl("/api/v1/customer/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((
                                (request, response, authentication) -> SecurityContextHolder.clearContext()
                        ))
                );
        return http.build();
    }
}
