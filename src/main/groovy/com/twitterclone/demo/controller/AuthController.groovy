package com.twitterclone.demo.controller

import com.twitterclone.demo.controller.dto.UserDto
import com.twitterclone.demo.exception.exceptions.ValidationException
import com.twitterclone.demo.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class AuthController {

    @Autowired
    AuthService authService

    @PostMapping("/login")
    def login(HttpServletRequest request, HttpServletResponse response) {
        authService.login(request, response)
    }

    @PostMapping("/logout")
    def logout(HttpServletRequest request) {
        authService.logout(request)
    }

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    def createUser(@RequestBody UserDto newUserDto) {
        if (!newUserDto.getUsername() || !newUserDto.getPassword()) {
            throw new ValidationException(String.format(
                    "%s shouldn't be empty", newUserDto.getUsername() ? "Password" : "Username"))
        }
        return authService.registration(newUserDto);
    }
}