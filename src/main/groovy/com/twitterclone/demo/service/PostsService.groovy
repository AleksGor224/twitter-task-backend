package com.twitterclone.demo.service

import com.twitterclone.demo.controller.dto.*
import com.twitterclone.demo.exception.exceptions.ForbiddenException
import com.twitterclone.demo.exception.exceptions.PostNotFoundException
import com.twitterclone.demo.repo.PostsRepo
import com.twitterclone.demo.repo.entities.Comment
import com.twitterclone.demo.repo.entities.Post
import com.twitterclone.demo.repo.entities.User
import com.twitterclone.demo.utils.ApiUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

import java.util.stream.Collectors

@Service
class PostsService {

    @Autowired
    PostsRepo postsRepo
    @Autowired
    UsersService usersService

    Map createPost(PostDto postDto, String userId) {
        def user = usersService.checkIfUserExistsOrThrow(userId)
        long time = System.currentTimeMillis()

        def newPost = new Post(
                postId: UUID.randomUUID(),
                owner: user,
                data: postDto.data,
                createDate: time,
                lastUpdate: time
        )

        user.posts.add(newPost)
        usersService.updateUserEntity(user)

        def postId = postsRepo.save(newPost).postId

        return [id: postId] as Map
    }

    void updatePost(PostDto postDto, String postId) {
        Post post = getPostOrThrow(postId)
        boolean hasChanges = false

        if (postDto.data) {
            post.data = postDto.data
            hasChanges = true
        }

        if (hasChanges) {
            post.lastUpdate = System.currentTimeMillis()
            postsRepo.save(post)
        }
    }


    void deletePost(String postId) {
        Post post = getPostOrThrow(postId)
        User user = post.owner

        user.posts.remove(post)
        usersService.updateUserEntity(user)

        postsRepo.delete(post)
    }

    List<PostViewDto> getMyFeed() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName()
        User user = usersService.findUserByUsername(currentUsername)
                .orElseThrow { new ForbiddenException("Please check your credentials and try again") }

        List<PostViewDto> res = []

        user.getFollowers().forEach { follower ->
            res.addAll(postsRepo.findAllWithCommentsAndLikesByOwnerId(follower.getUserId()).collect { post ->
                def comments = post.comments.collect { comment ->
                    new CommentViewDto(
                            data: comment.data,
                            ownerUsername: comment.owner.username,
                            ownerId: comment.owner.userId,
                            createDate: ApiUtils.convertEpochToHumanDate(comment.createDate)
                    )
                }

                def likes = post.likedBy.collect { like ->
                    new UserViewDto(
                            username: like.username,
                            userId: like.userId
                    )
                }

                new PostViewDto(
                        data: post.data,
                        ownerId: post.owner.userId,
                        ownerUsername: post.owner.username,
                        createData: ApiUtils.convertEpochToHumanDate(post.createDate),
                        lastUpdate: ApiUtils.convertEpochToHumanDate(post.createDate),
                        likedBy: likes,
                        comments: comments
                )
            })
        }

        return res
    }

    List<PostViewDto> getUserFeed(String userId) {
        usersService.checkIfUserExistsOrThrow(userId)
        return postsRepo.findAllWithoutCommentsAndLikesByOwnerId(userId).stream()
                .map(e ->
                        new PostViewDto(
                                data: e.getData(),
                                ownerId: e.getOwner().getUserId(),
                                ownerUsername: e.getOwner().getUsername(),
                                createData: ApiUtils.convertEpochToHumanDate(e.getCreateDate()),
                                lastUpdate: ApiUtils.convertEpochToHumanDate(e.getCreateDate())))
                .collect(Collectors.toList())
    }

    void likePost(String postId, String userId) {
        Post post = getPostOrThrow(postId)
        User user = usersService.checkIfUserExistsOrThrow(userId)
        if (post.getLikedBy().contains(user)) {
            post.getLikedBy().remove(user)
        } else {
            post.getLikedBy().add(user)
        }
        postsRepo.save(post)
    }

    Map commentPost(CommentDto commentDto, String postId, String userId) {
        Post post = getPostOrThrow(postId)
        User user = usersService.checkIfUserExistsOrThrow(userId)
        Comment comment = new Comment(commentId: UUID.randomUUID(), data: commentDto.getData(),
                owner: user, post: post, createDate: System.currentTimeMillis())
        post.getComments().add(comment)
        postsRepo.save(post)
        return Collections.singletonMap("id", comment.getCommentId())
    }

    List<CommentViewDto> getCommentsForPost(String postId) {
        Post post = getPostOrThrow(postId)
        return post.getComments().stream()
                .map(e -> new CommentViewDto(
                        data: e.getData(), ownerUsername: e.getOwner().getUsername(),
                        ownerId: e.getOwner().getUserId(), createDate: e.getCreateDate()))
                .collect(Collectors.toList())
    }

    private Post getPostOrThrow(String postId) {
        Post post = postsRepo.findById(postId)
                .orElseThrow { new PostNotFoundException("Post with id '$postId' not found") }
        return post
    }
}
