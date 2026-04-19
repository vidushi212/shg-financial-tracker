package com.shg.controller;

import com.shg.model.SHGGroup;
import com.shg.model.SHGMember;
import com.shg.repository.SHGGroupRepository;
import com.shg.repository.SHGMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthApiController {

    private final SHGMemberRepository memberRepository;
    private final SHGGroupRepository groupRepository;

    public AuthApiController(SHGMemberRepository memberRepository,
                             SHGGroupRepository groupRepository) {
        this.memberRepository = memberRepository;
        this.groupRepository = groupRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.getOrDefault("username", "").trim();
        String password = payload.getOrDefault("password", "");

        Optional<SHGMember> member = memberRepository.findByUsername(username);
        if (member.isEmpty() || !member.get().getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password."));
        }

        String status = member.get().getStatus() == null
                ? "ACTIVE"
                : member.get().getStatus().trim().toUpperCase(Locale.ENGLISH);
        if (!"ACTIVE".equals(status) && !"APPROVED".equals(status)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Your account is not active yet. Please contact the administrator."));
        }

        return ResponseEntity.ok(toAuthPayload(member.get()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String username = payload.getOrDefault("username", "").trim();
        String password = payload.getOrDefault("password", "");
        String fullName = payload.getOrDefault("fullName", "").trim();
        String email = payload.getOrDefault("email", "").trim();
        String role = normalizeRole(payload.getOrDefault("role", "Member"));

        if (username.isEmpty() || password.length() < 4 || fullName.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Full name, username, and password are required."));
        }

        if (memberRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Username already exists."));
        }

        SHGGroup group = groupRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No SHG group available for registration."));

        SHGMember member = new SHGMember(username, password, fullName, role, group);
        member.setEmail(email.isEmpty() ? null : email);
        member.setStatus("Broker".equalsIgnoreCase(role) ? "PENDING" : "ACTIVE");
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        member.setJoinedDate(LocalDateTime.now());

        SHGMember saved = memberRepository.save(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAuthPayload(saved));
    }

    private Map<String, Object> toAuthPayload(SHGMember member) {
        String normalizedRole = member.getRole().toLowerCase(Locale.ENGLISH);
        return Map.of(
                "id", member.getId(),
                "username", member.getUsername(),
                "fullName", member.getFullName(),
                "role", normalizedRole,
                "status", member.getStatus() == null ? "ACTIVE" : member.getStatus(),
                "email", member.getEmail() == null ? "" : member.getEmail(),
                "landingPage", resolveLandingPage(normalizedRole),
                "token", "auth-token-" + member.getId());
    }

    private String resolveLandingPage(String role) {
        switch (role) {
            case "admin":
                return "/admin/brokers";
            case "accountant":
            case "treasurer":
                return "/finance/accountant";
            case "government officer":
                return "/advisory/schemes";
            case "broker":
                return "/advisory/investments";
            default:
                return "/dashboard";
        }
    }

    private String normalizeRole(String rawRole) {
        String value = rawRole == null ? "" : rawRole.trim().toLowerCase(Locale.ENGLISH);
        switch (value) {
            case "president":
                return "President";
            case "treasurer":
                return "Treasurer";
            case "secretary":
                return "Secretary";
            case "accountant":
                return "Accountant";
            case "broker":
                return "Broker";
            case "admin":
                return "Admin";
            case "govt_officer":
            case "government officer":
                return "Government Officer";
            default:
                return "Member";
        }
    }
}
