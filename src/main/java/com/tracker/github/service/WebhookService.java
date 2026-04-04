package com.tracker.github.service;

import com.tracker.github.entity.Author;
import com.tracker.github.entity.Commit;
import com.tracker.github.repository.AuthorRepository;
import com.tracker.github.repository.CommitRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WebhookService {

    private final AuthorRepository authorRepository;
    private final CommitRepository commitRepository;
    private final SlackService slackService;

    public WebhookService(AuthorRepository authorRepository, CommitRepository commitRepository, SlackService slackService) {
        this.authorRepository = authorRepository;
        this.commitRepository = commitRepository;
        this.slackService = slackService;
    }

//    @Transactional
//    @SuppressWarnings("unchecked")
//    public void processPushEvent(Map<String, Object> payload) {
//        // Extract pusher info
//        Map<String, Object> pusher = (Map<String, Object>) payload.get("pusher");
//        String pusherName = (String) pusher.get("name");
//        String pusherEmail = (String) pusher.get("email");
//
//        // Extract sender username
//        Map<String, Object> sender = (Map<String, Object>) payload.get("sender");
//        String username = sender != null ? (String) sender.get("login") : pusherName;
//
//        // Extract repo name
//        Map<String, Object> repo = (Map<String, Object>) payload.get("repository");
//        String repoName = (String) repo.get("full_name");
//
//        // Save author
//        Author author = new Author(pusherName, pusherEmail, username);
//        author = authorRepository.save(author);
//
//        // Extract and save commits
//        List<Map<String, Object>> commitList = (List<Map<String, Object>>) payload.get("commits");
//        List<Commit> savedCommits = new ArrayList<>();
//
//        for (Map<String, Object> c : commitList) {
//            String commitId = (String) c.get("id");
//            String message = (String) c.get("message");
//            String url = (String) c.get("url");
//            String timestampStr = (String) c.get("timestamp");
//
//            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//
//            Commit commit = new Commit(commitId, message, url, timestamp, author);
//            savedCommits.add(commitRepository.save(commit));
//        }
//
//        // Send Slack notification
//        slackService.sendPushNotification(author, savedCommits, repoName);
//    }
@Transactional
@SuppressWarnings("unchecked")
public void processPushEvent(Map<String, Object> payload) {
    if (payload == null) {
        throw new IllegalArgumentException("Payload cannot be null");
    }

    // Extract pusher info safely
    Map<String, Object> pusher = (Map<String, Object>) payload.get("pusher");
    String pusherName = pusher != null ? (String) pusher.get("name") : "Unknown";
    String pusherEmail = pusher != null ? (String) pusher.get("email") : "unknown@example.com";

    // Extract sender username safely
    Map<String, Object> sender = (Map<String, Object>) payload.get("sender");
    String username = sender != null ? (String) sender.get("login") : pusherName;

    // Extract repo name safely
    Map<String, Object> repo = (Map<String, Object>) payload.get("repository");
    String repoName = repo != null ? (String) repo.get("full_name") : "Unknown repo";

    // Save author
    Author author = new Author(pusherName, pusherEmail, username);
    author = authorRepository.save(author);

    // Extract and save commits safely
    List<Map<String, Object>> commitList = (List<Map<String, Object>>) payload.get("commits");
    List<Commit> savedCommits = new ArrayList<>();
    if (commitList != null) {
        for (Map<String, Object> c : commitList) {
            String commitId = (String) c.get("id");
            String message = (String) c.get("message");
            String url = (String) c.get("url");
            String timestampStr = (String) c.get("timestamp");

            LocalDateTime timestamp = null;
            try {
                timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (Exception e) {
                timestamp = LocalDateTime.now(); // fallback
            }

            Commit commit = new Commit(commitId, message, url, timestamp, author);
            savedCommits.add(commitRepository.save(commit));
        }
    }

    // Send Slack notification
    slackService.sendPushNotification(author, savedCommits, repoName);
}



}
