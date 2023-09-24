package com.twitterclone.demo.controller

import com.twitterclone.demo.controller.dto.CommentDto
import com.twitterclone.demo.controller.dto.PostDto
import com.twitterclone.demo.exception.exceptions.ValidationException
import com.twitterclone.demo.service.PostsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("posts")
class PostsController {

    @Autowired
    PostsService postsService

    @PostMapping("create/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    def createPost(@RequestBody PostDto postDto, @PathVariable("userId") String userId) {
        if (!userId) {
            throw new ValidationException("User ID shouldn't be empty")
        }
        return postsService.createPost(postDto, userId)
    }

    @PatchMapping("{postId}")
    def updatePost(@RequestBody PostDto postDto, @PathVariable("postId") String postId) {
        if (!postId) {
            throw new ValidationException("Post ID shouldn't be empty")
        }
        postsService.updatePost(postDto, postId)
    }

    @DeleteMapping("{postId}")
    def deletePost(@PathVariable("postId") String postId) {
        if (!postId) {
            throw new ValidationException("Post ID shouldn't be empty")
        }
        postsService.deletePost(postId);
    }

    @PutMapping("{postId}/like/{userId}")
    def likePost(@PathVariable("postId") String postId, @PathVariable("userId") String userId) {
        if (!postId) {
            throw new ValidationException("Post ID shouldn't be empty")
        }
        if (!userId) {
            throw new ValidationException("User ID shouldn't be empty")
        }
        postsService.likePost(postId, userId)
    }

    @PostMapping("{postId}/comment/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    def commentPost(@RequestBody CommentDto commentDto, @PathVariable("postId") String postId, @PathVariable("userId") String userId) {
        if (!postId) {
            throw new ValidationException("Post ID shouldn't be empty")
        }
        if (!userId) {
            throw new ValidationException("User ID shouldn't be empty")
        }
        return postsService.commentPost(commentDto, postId, userId)
    }

    @GetMapping("{postId}/comments")
    def getComments(@PathVariable("postId") String postId) {
        if (!postId) {
            throw new ValidationException("Post ID shouldn't be empty")
        }
        postsService.getCommentsForPost(postId)
    }

    @GetMapping("myFeed")
    def getMyFeed() {
        return postsService.getMyFeed()
    }

    @GetMapping("feed/{userId}")
    def getUserFeed(@PathVariable("userId") String userId) {
        if (!userId) {
            throw new ValidationException("User ID shouldn't be empty")
        }
        return postsService.getUserFeed(userId);
    }
}
