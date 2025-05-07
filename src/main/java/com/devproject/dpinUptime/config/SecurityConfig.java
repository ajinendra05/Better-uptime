package com.devproject.dpinUptime.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final UserDetailsService userDetailsService;

        public SecurityConfig(UserDetailsService userDetailsService) {
                this.userDetailsService = userDetailsService;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/register", "/login", "/validator/login",
                                                                "/static/**", "/css/**",
                                                                "/js/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/monitors").authenticated()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID"))
                                .rememberMe(remember -> remember
                                                .key("uniqueAndSecretKey")
                                                .tokenValiditySeconds(86400)
                                                .userDetailsService(userDetailsService))
                                .sessionManagement(session -> session
                                                .maximumSessions(1)
                                                .expiredUrl("/login?expired"))
                                .exceptionHandling(exceptions -> exceptions
                                                .accessDeniedPage("/access-denied"));

                return http.build();
        }

        // Link UserDetailsService + PasswordEncoder
        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
                AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
                auth
                                .userDetailsService(userDetailsService)
                                .passwordEncoder(passwordEncoder());
                return auth.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}

// package com.devproject.dpinUptime.config;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import
// org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import
// org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.core.userdetails.UserDetailsService;

// import com.devproject.dpinUptime.service.UserDetailsServiceImpl;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {
// @Autowired
// private UserDetailsServiceImpl userDetailsService;

// public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
// this.userDetailsService = userDetailsService;
// }

// @Bean
// public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
// Exception {
// http
// .authorizeHttpRequests(auth -> auth
// .requestMatchers("/", "/register", "/login", "/static/**", "/css/**",
// "/js/**")
// .permitAll()
// .requestMatchers("/admin/**").hasRole("ADMIN")
// .anyRequest().authenticated())
// .formLogin(form -> form
// .loginPage("/login")
// .loginProcessingUrl("/login")
// .defaultSuccessUrl("/dashboard", true)
// .failureUrl("/login?error=true")
// .permitAll())
// .userDetailsService(userDetailsService)
// .logout(logout -> logout
// .logoutUrl("/logout")
// .logoutSuccessUrl("/login?logout")
// .invalidateHttpSession(true)
// .deleteCookies("JSESSIONID")
// .logoutSuccessUrl("/"))
// .rememberMe(remember -> remember
// .key("uniqueAndSecretKey")
// .tokenValiditySeconds(86400) // 1 day
// ).sessionManagement(session -> session
// .maximumSessions(1)
// .expiredUrl("/login?expired"))
// .exceptionHandling(exceptions -> exceptions
// .accessDeniedPage("/access-denied"));
// return http.build();
// }

// @Bean
// public PasswordEncoder passwordEncoder() {
// return new BCryptPasswordEncoder();
// }
// }