package com.twitterclone.demo.controller

import com.twitterclone.demo.controller.dto.UserDto
import com.twitterclone.demo.exception.exceptions.ValidationException
import com.twitterclone.demo.service.UsersService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UsersController {

    @Autowired
    private UsersService usersService;

    @PatchMapping("{userId}")
    def updateUser(@RequestBody UserDto userDto, @PathVariable("userId") String userId) {
        if (!userId) {
            throw new ValidationException("User ID shouldn't be empty")
        }
        usersService.updateUser(userDto, userId);
    }

    @DeleteMapping("{userId}")
    def deleteUser(@PathVariable("userId") String userId) {
        if (!userId) {
            throw new ValidationException("User ID shouldn't be empty")
        }
        usersService.deleteUser(userId);
    }

    @PutMapping("{userId}/subscribe/{followerId}")
    @ResponseStatus(HttpStatus.CREATED)
    def follow(@PathVariable("userId") String userId, @PathVariable("followerId") String followerId) {
        if (!userId || !followerId) {
            throw new ValidationException(String.format(
                    "%s shouldn't be empty", userId ? "Follower ID" : "User ID"))
        }
        usersService.follow(userId, followerId);
    }

    @DeleteMapping("{userId}/subscribe/{followerId}")
    def unfollow(@PathVariable("userId") String userId, @PathVariable("followerId") String followerId) {
        if (!userId || !followerId) {
            throw new ValidationException(String.format(
                    "%s shouldn't be empty", userId ? "Follower ID" : "User ID"))
        }
        usersService.unfollow(userId, followerId);
    }
}
