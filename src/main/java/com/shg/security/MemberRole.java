package com.shg.security;

/**
 * Enum representing different roles within an SHG group
 */
public enum MemberRole {
    PRESIDENT("President", 5),           // Full access including monitoring
    SECRETARY("Secretary", 4),           // Can manage members and documents
    ACCOUNTANT("Accountant", 3),         // Can manage finances and reports
    TREASURER("Treasurer", 3),           // Financial management, same as accountant
    MEMBER("Member", 1);                 // Basic member with limited access

    private final String displayName;
    private final int level;             // Permission level (higher = more permissions)

    MemberRole(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Check if this role has at least the given permission level
     */
    public boolean hasPermissionLevel(int requiredLevel) {
        return this.level >= requiredLevel;
    }

    /**
     * Check if this role has at least the same or higher level as another role
     */
    public boolean isHigherOrEqualTo(MemberRole other) {
        return this.level >= other.level;
    }

    /**
     * Get role from string
     */
    public static MemberRole fromString(String role) {
        if (role == null) {
            return MEMBER;
        }
        try {
            return MemberRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEMBER;
        }
    }
}
