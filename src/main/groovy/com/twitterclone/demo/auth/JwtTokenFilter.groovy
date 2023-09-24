package com.twitterclone.demo.auth


import com.twitterclone.demo.repo.UsersRepo
import com.twitterclone.demo.service.AuthService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Slf4j
class JwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    UsersRepo usersRepo
    @Autowired
    AuthService authService

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (!(header && header.startsWith("Bearer "))) {
            if (!["/registration", "/login"].contains(request.getRequestURI())) {
                response.setStatus(403)
                log.warn("Detected request without 'Bearer' token. Request '{}'", request)
            }
            filterChain.doFilter(request, response)
            return
        }

        final String token = header.split(" ")[1].trim()

        if (!JwtTokenUtils.isTokenValid(token)) {
            response.setStatus(403)
            response.getWriter().write("Invalid token")
            log.warn("Invalid token detected. Request '{}'", request)
            filterChain.doFilter(request, response)
            return
        }

        // Check if user was logged out
        if (!authService.existsInCache(token)) {
            response.setStatus(401)
            response.getWriter().write("Please login")
            filterChain.doFilter(request, response)
            return
        }

        UserDetails userDetails = usersRepo
                .findByUsername(JwtTokenUtils.getUsernameFromToken(token))
                .orElse(null)

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails ? userDetails.getAuthorities() : [])

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request as HttpServletRequest))
        SecurityContextHolder.getContext().setAuthentication(authentication)
        filterChain.doFilter(request, response)
    }
}
