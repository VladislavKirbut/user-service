package com.innowise.userservice.testdata;

import java.time.LocalDate;

public final class PaymentCardTestConstants {

    public static final Long CARD_ID = 1L;

    public static final String CARD_NUMBER = "1111111111111111";
    public static final String CARD_HOLDER = "IVAN IVANOV";

    public static final Boolean CARD_ACTIVE = true;
    public static final Boolean CARD_INACTIVE = false;

    public static final LocalDate CARD_EXPIRATION_DATE = LocalDate.of(2030, 3,5);

    public static final Long MIN_COUNT_CARD = 0L;
    public static final Long MAX_COUNT_CARD = 5L;

    private PaymentCardTestConstants() {}
}
