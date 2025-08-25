package com.zerosio.utility;

import net.dv8tion.jda.api.Permission;

import java.util.List;

public enum Rank {

    ADMIN("1396739232340643983", Permission.ADMINISTRATOR),
    MOD("1396772730833207427", Permission.BAN_MEMBERS),
    HELPER("1397065215404933120", Permission.MESSAGE_MANAGE),
    DEFAULT("", null); // No required permission

    private final String roleId;
    private final Permission requiredPermission;

    Rank(String roleId, Permission requiredPermission) {
        this.roleId = roleId;
        this.requiredPermission = requiredPermission;
    }

    public String getRoleId() {
        return roleId;
    }

    public Permission getRequiredPermission() {
        return requiredPermission;
    }

    public int getLevel() {
        return this.ordinal(); // Lower ordinal = higher rank
    }

    public boolean isAboveOrEqual(Rank other) {
        return this.getLevel() <= other.getLevel();
    }

    public boolean isBelowOrEqual(Rank other) {
        return this.getLevel() >= other.getLevel();
    }

    public boolean hasRank(Rank requiredRank) {
        return this.isAboveOrEqual(requiredRank);
    }

    public boolean isStaff() {
        return this.ordinal() <= HELPER.ordinal() && this != DEFAULT;
    }

    public String getFormatted() {
        return this == DEFAULT ? "Default" : this.name();
    }

    public static Rank getRankByRoleId(String roleId) {
        for (Rank rank : values()) {
            if (rank.getRoleId().equals(roleId)) return rank;
        }
        return DEFAULT;
    }

    public static Rank getHighestRankFromRoles(List<String> roleIds) {
        for (Rank rank : values()) {
            if (!rank.roleId.isEmpty() && roleIds.contains(rank.roleId)) {
                return rank;
            }
        }
        return DEFAULT;
    }

    public boolean hasPermission(net.dv8tion.jda.api.entities.Member member) {
        return requiredPermission == null || member.hasPermission(requiredPermission);
    }
}
