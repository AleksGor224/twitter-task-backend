package com.twitterclone.demo.config

import com.twitterclone.demo.auth.JwtTokenFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    JwtTokenFilter tokenFilter

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        corsConfiguration.addAllowedMethod(HttpMethod.POST);
        corsConfiguration.addAllowedMethod(HttpMethod.GET)
        corsConfiguration.addAllowedMethod(HttpMethod.PUT)
        corsConfiguration.addAllowedMethod(HttpMethod.PATCH)
        corsConfiguration.addAllowedMethod(HttpMethod.DELETE)
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.POST, "/login", "/registration").permitAll()
                .antMatchers(HttpMethod.POST, "/logout").authenticated()
                .antMatchers(HttpMethod.PATCH, "/users/*").authenticated()
                .antMatchers(HttpMethod.DELETE, "/users").authenticated()
                .antMatchers(HttpMethod.PUT, "/users/*/subscribe/").authenticated()
                .antMatchers(HttpMethod.DELETE, "/users/*/subscribe/").authenticated()
                .antMatchers(HttpMethod.POST, "/posts/create/*").authenticated()
                .antMatchers(HttpMethod.PATCH, "/posts/*").authenticated()
                .antMatchers(HttpMethod.DELETE, "/posts/*").authenticated()
                .antMatchers(HttpMethod.PUT, "/posts/*/like/*").authenticated()
                .antMatchers(HttpMethod.POST, "/posts/*/comment/*").authenticated()
                .antMatchers(HttpMethod.GET, "/posts/comments").authenticated()
                .antMatchers(HttpMethod.GET, "/posts/myFeed").authenticated()
                .antMatchers(HttpMethod.GET, "/posts/feed/*").authenticated()
                .antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/security", "/swagger-ui.html", "/webjars/**").permitAll()
                .anyRequest().denyAll()
                .and()
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
    }
}