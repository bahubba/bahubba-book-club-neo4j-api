package com.bahubba.bahubbabookclub.config;

import com.bahubba.bahubbabookclub.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/** Configures beans to be used within the application */
@Configuration
@PropertySource("classpath:application.yaml")
@RequiredArgsConstructor
public class AppConfig {

    private final UserRepo userRepo;

    /**
     * Creates a UserDetailsService for loading users by username
     *
     * @return UserDetailsService
     * @throws UsernameNotFoundException If user is not found
     */
    @Bean
    public UserDetailsService userDetailsService() throws UsernameNotFoundException {
        return username -> userRepo.findByUsernameAndDepartedIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Creates an AuthenticationProvider
     *
     * @return AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Creates an AuthenticationManager
     *
     * @param authConfig authentication configuration
     * @return AuthenticationConfiguration
     * @throws Exception If authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Creates a password encoder
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Custom Swagger configuration */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("BAHubba Book Club API")
                        .description("API for the BAHubba Book Club application")
                        .version("1.0.0")
                        .build());
    }
}
