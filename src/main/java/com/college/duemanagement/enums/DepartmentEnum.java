package com.college.duemanagement.enums;

import lombok.Getter;

@Getter
public enum DepartmentEnum {
    LIBRARY("Library"),
    TRAINING_AND_PLACEMENT("Training & Placement"),
    SPORTS("Sports"),
    OFFICE("Office"),
//    HOD("HOD"),
    IES_LIBRARY("IES Library"),
    TRANSPORT("Transport"),
    HOSTEL("Hostel"),
    ACCOUNTS("Account Section"),
    STUDENT_SECTION("Student Section"),
    CSE("CSE"),
    ECE("ECE"),
    EEE("EEE"),
    MECHANICAL("MECHANICAL"),
    CIVIL("CIVIL");

    private final String displayName;

    DepartmentEnum(String displayName) {
        this.displayName = displayName;
    }

}