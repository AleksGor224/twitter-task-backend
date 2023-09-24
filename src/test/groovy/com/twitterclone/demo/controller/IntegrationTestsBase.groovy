package com.twitterclone.demo.controller

import com.twitterclone.demo.auth.JwtTokenFilter
import com.twitterclone.demo.auth.JwtTokenUtils
import com.twitterclone.demo.controller.dto.UserDto

import com.twitterclone.demo.repo.PostsRepo
import com.twitterclone.demo.repo.UsersRepo
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import java.net.http.HttpHeaders

@SpringBootTest
abstract class IntegrationTestsBase extends Specification {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    UsersRepo usersRepo;
    @Autowired
    PostsRepo postsRepo;
    @Autowired
    JwtTokenFilter tokenFilter

    def passEncoder = new BCryptPasswordEncoder()

    MockMvc mockMvc
    MockMvc mockMvcWithFilter

    def setup() {
        mockMvcWithFilter = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilter(tokenFilter).build();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    def cleanup() {
        usersRepo.deleteAll()
        postsRepo.deleteAll()
    }

    Map userRegistrationAndLogin(UserDto userDto) {
        def res = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/registration")
                        .content(JsonOutput.toJson(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
        )

        def extractedMap = getResultAsMap(res)
        assert UUID.fromString(extractedMap.id as String)
        return extractedMap
    }

    void updateAndVerifyUser(UUID userId, String newUsername, String newPassword, long date, String oldUsername, String oldPassword) {
        def userDto = new UserDto(newUsername, newPassword)

        def res = mockMvc.perform(
                MockMvcRequestBuilders
                        .patch("/users/${userId}")
                        .content(JsonOutput.toJson(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
        )

        res.andExpect(MockMvcResultMatchers.status().isOk())

        def entity = usersRepo.findById(userId as String).orElse(null)
        assert entity != null

        if (oldUsername != null) {
            entity.username == oldUsername
        } else {
            entity.username == userDto.username
        }
        if (oldPassword != null) {
            entity.password == oldPassword
        } else {
            entity.password == userDto.password
        }
        entity.userId
        entity.registrationDate < date
        entity.lastUpdate >= date
    }

    static Map getResultAsMap(ResultActions res) {
        def jsonResponse = res.andReturn().getResponse().getContentAsString()
        return new JsonSlurper().parseText(jsonResponse) as Map
    }
}
