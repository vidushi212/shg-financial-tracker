package com.shg.security;

import com.shg.model.SHGMember;
import org.springframework.stereotype.Service;

/**
 * Service for role-based access control
 * Handles authorization for different operations based on member roles
 */
@Service
public class RoleAuthorizationService {

    /**
     * Check if member can create transactions
     * PRESIDENT, SECRETARY, ACCOUNTANT, TREASURER can create
     */
    public boolean canCreateTransaction(SHGMember member) {
        if (member == null) return false;
        MemberRole role = MemberRole.fromString(member.getRole());
        return role == MemberRole.PRESIDENT || 
               role == MemberRole.SECRETARY || 
               role == MemberRole.ACCOUNTANT ||
               role == MemberRole.TREASURER;
    }

    /**
     * Check if member can view all transactions
     * Only PRESIDENT and ACCOUNTANT can view all
     */
    public boolean canViewAllTransactions(SHGMember member) {
        if (member == null) return false;
        MemberRole role = MemberRole.fromString(member.getRole());
        return role == MemberRole.PRESIDENT || 
               role == MemberRole.ACCOUNTANT ||
               role == MemberRole.TREASURER;
    }

    /**
     * Check if member can view own transactions only
     * All roles except PRESIDENT can view their own
     */
    public boolean canViewOwnTransactions(SHGMember member) {
        return member != null;
    }

    /**
     * Check if member can approve transactions
     * Only PRESIDENT and ACCOUNTANT can approve
     */
    public boolean canApproveTransactions(SHGMember member) {
        if (member == null) return false;
        MemberRole role = MemberRole.fromString(member.getRole());
        return role == MemberRole.PRESIDENT || 
               role == MemberRole.ACCOUNTANT;
    }

    /**
     * Check if member can manage members (add/remove/edit)
     * Only PRESIDENT and SECRETARY can manage members
     */
    public boolean canManageMembers(SHGMember member) {
        if (member == null) return false;
        MemberRole role = MemberRole.fromString(member.getRole());
        return role == MemberRole.PRESIDENT || 
               role == MemberRole.SECRETARY;
    }

    /**
     * Check if member can view reports
     * PRESIDENT, ACCOUNTANT, TREASURER can view all reports
     * SECRETARY can view group reports
     * MEMBER can view own reports only
     */
    public boolean canViewAllReports(SHGMember member) {
        if (member == null) return false;
        MemberRole role = MemberRole.fromString(member.getRole());
        return role == MemberRole.PRESIDENT || 
               role == MemberRole.ACCOUNTANT ||
               role == MemberRole.TREASURER;
    }

    /**
     * Check if member can generate reports
     * Only PRESIDENT, ACCOUNTANT, TREASURER can generate
     */
    public boolean canGenerateReports(SHGMember member) {
        if (member == null) return false;
        MemberRole role = MemberRole.fromString(member.getRole());
        return role == MemberRole.PRESIDENT || 
               role == MemberRole.ACCOUNTANT ||
               role == MemberRole.TREASURER;
    }

    /**
     * Check if member can monitor activities (view dashboards, statistics)
     * Only PRESIDENT can fully monitor; others can monitor their area
     */
    public boolean canMonitor(SHGMember member) {
        if (member == null) return false;
        MemberRole role = MemberRole.fromString(member.getRole());
        return role == MemberRole.PRESIDENT || 
               role == MemberRole.ACCOUNTANT;
    }

    /**
     * Check if member is an administrator (full access)
     * Only PRESIDENT has full admin access
     */
    public boolean isAdministrator(SHGMember member) {
        if (member == null) return false;
        MemberRole role = MemberRole.fromString(member.getRole());
        return role == MemberRole.PRESIDENT;
    }

    /**
     * Check if member can modify a given member's data
     */
    public boolean canModifyMember(SHGMember currentUser, SHGMember targetMember) {
        if (currentUser == null || targetMember == null) return false;
        
        // User can only modify their own data, or if they're an admin
        if (currentUser.getId().equals(targetMember.getId())) {
            return true;
        }
        
        // Only PRESIDENT and SECRETARY can modify others
        MemberRole userRole = MemberRole.fromString(currentUser.getRole());
        MemberRole targetRole = MemberRole.fromString(targetMember.getRole());
        
        // Cannot modify someone with higher role
        if (userRole.isHigherOrEqualTo(targetRole)) {
            return userRole == MemberRole.PRESIDENT || 
                   (userRole == MemberRole.SECRETARY && targetRole != MemberRole.PRESIDENT);
        }
        
        return false;
    }

    /**
     * Check if member can delete transactions
     * Only PRESIDENT and ACCOUNTANT can delete
     */
    public boolean canDeleteTransaction(SHGMember member) {
        if (member == null) return false;
        MemberRole role = MemberRole.fromString(member.getRole());
        return role == MemberRole.PRESIDENT || 
               role == MemberRole.ACCOUNTANT;
    }

    /**
     * Get role level for authorization
     */
    public int getRoleLevel(SHGMember member) {
        if (member == null) return 0;
        MemberRole role = MemberRole.fromString(member.getRole());
        return role.getLevel();
    }

    /**
     * Check if action is allowed based on role levels
     */
    public boolean hasPermissionLevel(SHGMember member, int requiredLevel) {
        if (member == null) return false;
        MemberRole role = MemberRole.fromString(member.getRole());
        return role.hasPermissionLevel(requiredLevel);
    }
}
