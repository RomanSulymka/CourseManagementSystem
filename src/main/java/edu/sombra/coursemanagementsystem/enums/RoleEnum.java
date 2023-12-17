package edu.sombra.coursemanagementsystem.enums;

import lombok.Generated;
import lombok.Getter;

@Getter
public enum RoleEnum {
    ADMIN("ROLE_ADMIN"),
    INSTRUCTOR("ROLE_INSTRUCTOR"),
    STUDENT("ROLE_STUDENT");

    private final String authority;

    RoleEnum(String authority) {
        this.authority = authority;
    }
}
