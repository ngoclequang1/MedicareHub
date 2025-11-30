package fit.se2.medicarehub;

import javax.sql.DataSource;

import fit.se2.medicarehub.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider(passwordEncoder()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/auth/**", "/terms","/css/**", "/js/**", "/images/**", "/layouts/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/doctor/**").hasAuthority("ROLE_DOCTOR")
                        .requestMatchers("/patient/**").hasAuthority("ROLE_PATIENT")
                .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }



    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        manager.setUsersByUsernameQuery(
                "SELECT username, password, enabled FROM users WHERE username = ?"
        );
        manager.setAuthoritiesByUsernameQuery(
                "SELECT u.username, r.role_name FROM users u JOIN roles r ON u.roleID = r.roleID WHERE u.username = ?"
        );
        manager.setCreateUserSql(
                "INSERT INTO users (username, password, enabled) VALUES (?, ?, ?)"
        );
        manager.setUpdateUserSql(
                "UPDATE users SET password = ?, enabled = ? WHERE username = ?"
        );
        manager.setDeleteUserSql(
                "DELETE FROM users WHERE username = ?"
        );
        return manager;
    }
}
