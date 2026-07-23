package com.innowise.userservice.testdata;

import java.time.LocalDate;

public final class UserTestConstants {

    public static final Long USER_ID = 1L;

    public static final String USER_NAME = "Ivan";
    public static final String USER_SURNAME = "Ivanov";

    public static final String OLD_EMAIL = "email@test.com";
    public static final String NEW_EMAIL = "newemail@test.com";

    public static final LocalDate USER_BIRTH_DATE = LocalDate.of(1998, 10,1);

    public static final Boolean ACTIVE = true;
    public static final Boolean INACTIVE = false;

    private UserTestConstants() {}
}
