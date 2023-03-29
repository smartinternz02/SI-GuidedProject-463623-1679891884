package com.nithin.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig{
	
	

    @Bean
     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	
        http    
                .authorizeHttpRequests((authz) -> authz
                           .requestMatchers("/").permitAll()
                                .requestMatchers("/Register").permitAll()
                                .requestMatchers("/Login").permitAll()
                                .requestMatchers("/home").permitAll()
                                .requestMatchers("/loginHome").permitAll()
                                .requestMatchers("/fprice").permitAll()
                                .requestMatchers("/dprice").permitAll()
                                .requestMatchers("/routes").permitAll()
                                .requestMatchers("/cntrys").permitAll()
                                .requestMatchers("/currs").permitAll()
                                .requestMatchers("/places").permitAll()        
                );
        return http.build();
    }
}