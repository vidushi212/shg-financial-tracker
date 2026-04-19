package com.shg.service;

import com.shg.model.SHGGroup;
import com.shg.repository.SHGGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SHGGroupService {
    
    @Autowired
    private SHGGroupRepository shgGroupRepository;
    
    public SHGGroup createSHGGroup(SHGGroup shgGroup) {
        shgGroup.setCreatedAt(LocalDateTime.now());
        shgGroup.setUpdatedAt(LocalDateTime.now());
        if (shgGroup.getStatus() == null || shgGroup.getStatus().isBlank()) {
            shgGroup.setStatus("ACTIVE");
        }
        if (shgGroup.getTotalBalance() == null) {
            shgGroup.setTotalBalance(0.0);
        }
        if (shgGroup.getMonthlyContribution() == null) {
            shgGroup.setMonthlyContribution(0.0);
        }
        return shgGroupRepository.save(shgGroup);
    }
    
    public Optional<SHGGroup> getSHGGroupById(Long id) {
        return shgGroupRepository.findById(id);
    }
    
    public Optional<SHGGroup> getSHGGroupByName(String name) {
        return shgGroupRepository.findByName(name);
    }
    
    public List<SHGGroup> getAllSHGGroups() {
        return shgGroupRepository.findAll();
    }
    
    public SHGGroup updateSHGGroup(Long id, SHGGroup updatedGroup) {
        return shgGroupRepository.findById(id).map(group -> {
            group.setName(updatedGroup.getName());
            group.setDescription(updatedGroup.getDescription());
            group.setLocation(updatedGroup.getLocation());
            group.setMonthlyContribution(updatedGroup.getMonthlyContribution());
            if (updatedGroup.getStatus() != null && !updatedGroup.getStatus().isBlank()) {
                group.setStatus(updatedGroup.getStatus());
            }
            group.setUpdatedAt(LocalDateTime.now());
            return shgGroupRepository.save(group);
        }).orElseThrow(() -> new RuntimeException("SHG Group not found"));
    }
    
    public void deleteSHGGroup(Long id) {
        shgGroupRepository.deleteById(id);
    }
    
    public void updateGroupBalance(Long shgGroupId, Double amount) {
        shgGroupRepository.findById(shgGroupId).ifPresent(group -> {
            group.setTotalBalance(group.getTotalBalance() + amount);
            group.setUpdatedAt(LocalDateTime.now());
            shgGroupRepository.save(group);
        });
    }
}
