package com.twitterclone.demo.service

import com.twitterclone.demo.controller.dto.UserDto
import com.twitterclone.demo.exception.exceptions.UserNotFoundException
import com.twitterclone.demo.repo.UsersRepo
import com.twitterclone.demo.repo.entities.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UsersService {

    @Autowired
    UsersRepo usersRepo;
    final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()

    public static final String ID = "id"

    Map<String, String> createUser(UserDto newUserDto) {
        def passEncoded = passwordEncoder.encode(newUserDto.password)
        def time = System.currentTimeMillis()

        def user = new User(
                userId: UUID.randomUUID().toString(),
                username: newUserDto.username,
                password: passEncoded,
                registrationDate: time,
                lastUpdate: time
        )

        def id = usersRepo.save(user).userId

        return [id: id] as Map
    }


    void updateUser(UserDto userDto, String userId) {
        boolean hasChanges = false
        User user = checkIfUserExistsOrThrow(userId)

        if (userDto.username) {
            user.username = userDto.username
            hasChanges = true
        }

        if (userDto.password) {
            user.password = userDto.password
            hasChanges = true
        }

        if (hasChanges) {
            user.lastUpdate = System.currentTimeMillis()
            usersRepo.save(user)
        }
    }


    void deleteUser(String userId) {
        User user = checkIfUserExistsOrThrow(userId)
        usersRepo.delete(user)
    }

    void follow(String userId, String followerId) {
        User user = checkIfUserExistsOrThrow(userId)
        User follower = checkIfUserExistsOrThrow(followerId, Type.FOLLOWER)

        user.followers << follower
        follower.subscribers << user

        usersRepo.saveAll([user, follower])
    }

    void unfollow(String userId, String followerId) {
        User user = checkIfUserExistsOrThrow(userId)
        User follower = checkIfUserExistsOrThrow(followerId, Type.FOLLOWER)

        user.followers.remove(follower)
        follower.subscribers.remove(user)

        usersRepo.saveAll([user, follower])
    }

    void updateUserEntity(User user) {
        usersRepo.save(user)
    }

    User checkIfUserExistsOrThrow(String userId, Type type) {
        return getUserOrThrow(userId, type)
    }


    User checkIfUserExistsOrThrow(String userId) {
        return getUserOrThrow(userId, Type.USER)
    }

    Optional<User> findUserByUsername(String username) {
        return usersRepo.findByUsername(username)
    }

    private User getUserOrThrow(String userId, Type type) {
        User user = usersRepo.findById(userId)
                .orElseThrow { new UserNotFoundException("${type.name()} with id '$userId' not found") }
        return user
    }

    enum Type {
        USER, FOLLOWER, SUBSCRIBER
    }
}
