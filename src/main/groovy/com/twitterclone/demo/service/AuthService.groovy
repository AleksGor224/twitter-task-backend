package com.twitterclone.demo.service

import com.twitterclone.demo.auth.JwtTokenUtils
import com.twitterclone.demo.controller.dto.UserDto
import com.twitterclone.demo.exception.exceptions.UnauthorizedException
import com.twitterclone.demo.repo.entities.User
import org.springframework.http.HttpHeaders
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Service
class AuthService {

    private Set<String> jwtCache;
    private ScheduledExecutorService executorService;
    final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()
    UsersService usersService

    AuthService(UsersService usersService) {
        this.usersService = usersService
        this.executorService = Executors.newScheduledThreadPool(1)
        this.jwtCache = new HashSet<>()
    }

    @PostConstruct
    void init() {
        executorService.schedule(() -> {
            Thread.currentThread().setName("Jwt cache cleaner")
            jwtCache.forEach(token -> {
                if (!JwtTokenUtils.isTokenValid(token)) {
                    jwtCache.remove(token)
                }
            })
        }, Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)
    }

    @PreDestroy
    void destroy() {
        executorService.shutdown()
    }

    void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (!(header && header.startsWith("Basic "))) {
            throw new UnauthorizedException("Basic token is required for authorization reason")
        }

        final String token = header.split(" ")[1].trim()
        String[] tokens = new String(token.decodeBase64()).split(":")

        User user = usersService.findUserByUsername(tokens[0])
                .orElseThrow { new UnauthorizedException("Username and/or password are incorrect") }

        if (!passwordEncoder.matches(tokens[1], user.getPassword())) {
            throw new UnauthorizedException("Username and/or password are incorrect")
        }

        String generatedToken = JwtTokenUtils.generateToken(user.getUsername())
        jwtCache.add(generatedToken)

        response.setHeader(HttpHeaders.AUTHORIZATION, generatedToken)
    }


    void logout(HttpServletRequest request) {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!header || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException("Bearer token is required for logout request")
        }

        final String token = header.split(" ")[1].trim();
        if (jwtCache.contains(token)) {
            jwtCache.remove(token)
        }
    }

    Map registration(UserDto userDto) {
        return usersService.createUser(userDto)
    }

    boolean existsInCache(String token) {
        return jwtCache.contains(token)
    }
}
