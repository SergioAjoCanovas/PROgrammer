package com.programmer.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED
                )
            )
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of(
                    "http://127.0.0.1:5500",
                    "http://localhost:5500"
                ));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // Permitir acceso a la API de autenticación y recursos estáticos
                .requestMatchers("/api/auth/**", "/login", "/signUp", "/Style/**", "/Img/**", "/uploads/**").permitAll()
                // El resto de la aplicación requiere autenticación
                .anyRequest().authenticated() 
            )

            // CAMBIO: Se habilita formLogin para gestionar la redirección automática
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/api/auth/login") // Debe coincidir con el th:action de tu HTML
                .defaultSuccessUrl("/main", true)      // Redirección cuando el login es correcto
                .failureUrl("/login?error=auth")      // Redirección cuando falla
                .permitAll()
            )
            
            .formLogin(form -> form.disable()) // ELIMINAR ESTA LÍNEA si existía en tu código original
            .httpBasic(basic -> basic.disable())
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(oAuth2LoginSuccessHandler)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}