package com.smart.config;

import org.springframework.context.annotation.Bean;
//import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;



@Configuration
@EnableWebSecurity
public class MyConfig{
    @Bean
    UserDetailsService getUserDetailService() {
		return new UserDetailServiceimpl();
	}

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
    	return new BCryptPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
    	DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider();
    	daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
    	daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
    	return daoAuthenticationProvider;
    }
    
    ///configure method
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
    	return config.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
    	http
    	.authorizeHttpRequests(auth -> auth
    			.requestMatchers("/admin/**").hasRole("ADMIN")
    			.requestMatchers("/user/**").hasRole("USER")
    			.requestMatchers("/**").permitAll()
    )
    			.formLogin()
    			.loginPage("/signin")
    			.loginProcessingUrl("/dologin")
    			.defaultSuccessUrl("/user/index")
//    			.failureUrl("/login-fail")
    			.and()
    			.csrf().disable();
    	return http.build();
    }
}
