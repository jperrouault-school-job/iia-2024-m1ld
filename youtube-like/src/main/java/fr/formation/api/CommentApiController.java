package fr.formation.api;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.formation.exception.UserNotFoundException;
import fr.formation.exception.VideoNotFoundException;
import fr.formation.model.Comment;
import fr.formation.model.User;
import fr.formation.model.Video;
import fr.formation.repo.CommentRepository;
import fr.formation.repo.UserRepository;
import fr.formation.repo.VideoRepository;
import fr.formation.request.CreateCommentRequest;
import fr.formation.response.CommentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/comment/{videoId}")
@RequiredArgsConstructor
@Log4j2
public class CommentApiController {
    private final CommentRepository repository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<CommentResponse> findAllByVideoId(@PathVariable String videoId) {
        log.debug("Finding all comments for video {} ...", videoId);

        return this.repository.findAllByVideoId(videoId)
            .stream()
            .map(comment -> CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .date(comment.getDate())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getName())
                .build()
            )
            .toList()
        ;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public String create(@PathVariable String videoId, @Valid @RequestBody CreateCommentRequest request, Authentication authentication) {
        log.debug("Creating comment for video {} from user {} ...", videoId, authentication.getPrincipal());

        Video video = this.videoRepository.findById(videoId).orElseThrow(VideoNotFoundException::new);
        User user = this.userRepository.findById(authentication.getPrincipal().toString()).orElseThrow(UserNotFoundException::new);
        Comment comment = new Comment();

        BeanUtils.copyProperties(request, comment);

        comment.setDate(LocalDateTime.now());
        comment.setVideo(video);
        comment.setUser(user);

        this.repository.save(comment);

        log.debug("Comment {} created!", comment.getId());

        return comment.getId();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(@PathVariable String id, @PathVariable String videoId) {
        log.debug("Deleting comment {} from video {} ...", id, videoId);

        this.repository.deleteById(id);

        log.debug("Comment {} deleted!", id);
    }
}
