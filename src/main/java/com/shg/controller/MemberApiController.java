package com.shg.controller;

import com.shg.model.SHGMember;
import com.shg.model.SHGGroup;
import com.shg.service.DashboardService;
import com.shg.service.SHGMemberService;
import com.shg.repository.SHGGroupRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
public class MemberApiController {

    private final DashboardService dashboardService;
    private final SHGMemberService memberService;
    private final SHGGroupRepository groupRepository;

    public MemberApiController(DashboardService dashboardService, SHGMemberService memberService, SHGGroupRepository groupRepository) {
        this.dashboardService = dashboardService;
        this.memberService = memberService;
        this.groupRepository = groupRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getMembers() {
        return ResponseEntity.ok(dashboardService.getDashboardMembers());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addMember(@RequestBody Map<String, Object> payload) {
        try {
            SHGGroup group = groupRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No SHG group found"));
            
            SHGMember member = new SHGMember();
            member.setUsername((String) payload.get("username"));
            member.setPassword((String) payload.get("password"));
            member.setFullName((String) payload.get("fullName"));
            member.setEmail((String) payload.getOrDefault("email", ""));
            member.setPhoneNumber((String) payload.getOrDefault("phoneNumber", ""));
            member.setRole((String) payload.getOrDefault("role", "MEMBER"));
            member.setStatus("ACTIVE");
            member.setShgGroup(group);
            
            SHGMember created = memberService.createMember(member);
            return ResponseEntity.ok(Map.of("success", true, "message", "Member added successfully", "id", created.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMember(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        try {
            Long memberId = Long.parseLong(id);
            SHGMember member = new SHGMember();
            member.setFullName((String) payload.get("fullName"));
            member.setEmail((String) payload.getOrDefault("email", ""));
            member.setPhoneNumber((String) payload.getOrDefault("phoneNumber", ""));
            member.setRole((String) payload.getOrDefault("role", "MEMBER"));
            
            SHGMember updated = memberService.updateMember(memberId, member);
            return ResponseEntity.ok(Map.of("success", true, "message", "Member updated successfully", "id", updated.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMember(@PathVariable String id) {
        try {
            Long memberId = Long.parseLong(id);
            memberService.deleteMember(memberId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Member deleted successfully", "id", id));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
