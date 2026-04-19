package com.shg.service;

import com.shg.model.SHGMember;
import com.shg.model.Transaction;
import com.shg.repository.SHGMemberRepository;
import com.shg.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SHGMemberService {
    
    @Autowired
    private SHGMemberRepository memberRepository;

    @Autowired
    private TransactionRepository transactionRepository;
    
    public SHGMember createMember(SHGMember member) {
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        member.setJoinedDate(LocalDateTime.now());
        if (member.getStatus() == null || member.getStatus().isBlank()) {
            member.setStatus("ACTIVE");
        }
        if (member.getSavingsAmount() == null) {
            member.setSavingsAmount(0.0);
        }
        if (member.getLoanAmount() == null) {
            member.setLoanAmount(0.0);
        }
        return memberRepository.save(member);
    }
    
    public Optional<SHGMember> getMemberById(Long id) {
        return memberRepository.findById(id);
    }
    
    public Optional<SHGMember> getMemberByUsername(String username) {
        return memberRepository.findByUsername(username);
    }
    
    public Optional<SHGMember> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
    
    public List<SHGMember> getMembersByShgGroupId(Long shgGroupId) {
        return memberRepository.findByShgGroupId(shgGroupId);
    }
    
    public List<SHGMember> getMembersByRole(String role) {
        return memberRepository.findByRole(role);
    }
    
    public List<SHGMember> getAllMembers() {
        return memberRepository.findAll();
    }
    
    public SHGMember updateMember(Long id, SHGMember updatedMember) {
        return memberRepository.findById(id).map(member -> {
            member.setFullName(updatedMember.getFullName());
            member.setEmail(updatedMember.getEmail());
            member.setPhoneNumber(updatedMember.getPhoneNumber());
            member.setRole(updatedMember.getRole());
            if (updatedMember.getStatus() != null && !updatedMember.getStatus().isBlank()) {
                member.setStatus(updatedMember.getStatus());
            }
            member.setUpdatedAt(LocalDateTime.now());
            return memberRepository.save(member);
        }).orElseThrow(() -> new RuntimeException("Member not found"));
    }
    
    public void deleteMember(Long id) {
        for (Transaction transaction : transactionRepository.findByMemberId(id)) {
            transaction.setMember(null);
            transactionRepository.save(transaction);
        }
        memberRepository.deleteById(id);
    }
}
