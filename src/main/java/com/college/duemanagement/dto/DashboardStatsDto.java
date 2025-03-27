package com.college.duemanagement.dto;

import java.util.List;

public class DashboardStatsDto {
    private long totalStudents;
    private long totalUsers;
    private long totalDepartments;
    private long totalDues;
    private List<DepartmentStatDto> departmentStats;
    private List<ActivityDto> recentActivities;

    // Getters and Setters
    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public long getTotalDues() {
        return totalDues;
    }

    public void setTotalDues(long totalDues) {
        this.totalDues = totalDues;
    }

    public List<DepartmentStatDto> getDepartmentStats() {
        return departmentStats;
    }

    public void setDepartmentStats(List<DepartmentStatDto> departmentStats) {
        this.departmentStats = departmentStats;
    }

    public List<ActivityDto> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<ActivityDto> recentActivities) {
        this.recentActivities = recentActivities;
    }
} 