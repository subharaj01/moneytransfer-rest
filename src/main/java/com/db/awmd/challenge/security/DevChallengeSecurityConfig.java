package com.db.awmd.challenge.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class DevChallengeSecurityConfig extends WebSecurityConfigurerAdapter {

	/* 
	 * this is required when you have actuator + rest + swagger and you have implemented spring basic security to protect all endpoints
	 * swagger urls should not be authenticated and actuator + rest can be authenticated as required 
	 * 
	 * (non-Javadoc)
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
	 */
	@Override
    protected void configure(HttpSecurity http) throws Exception {
		
		 //this will make only swagger-ui authenticated, rest swagger urls can be accessed without authentication
		 http.csrf().disable()
		 .authorizeRequests()
         .antMatchers(
                         "/v2/api-docs",
                          "/swagger-resources", 
                         "/swagger-resources/configuration/ui", 
                         "/swagger-resources/configuration/security")
         .permitAll();
		 
		 //use below when you dont want role based but only user password based authentication for actuator and rest endpoints
		 /*http.csrf().disable()
		 .authorizeRequests()
		 .anyRequest()
		 .authenticated()
		 .and()
		 .formLogin()
		 .and()
		 .httpBasic();*/
		 
		 //use below when you  want role based authorization as well as  user + password based authentication for rest and actuator endpoints
		 http.csrf().disable()
		 .authorizeRequests()
		 .antMatchers("/swagger-ui.html")
		 .hasRole("REST_API_ACCESS")
		 .antMatchers("/v1/accounts")
		 .hasRole("REST_API_ACCESS")
		 .antMatchers("/v1/accounts/*")
		 .hasRole("REST_API_ACCESS")
		 .antMatchers("/actuator/*")
		 .hasRole("ACTADMIN")
		 .anyRequest().denyAll()
		 .and()
		 .formLogin()
		 .and()
		 .httpBasic();
    }
}
