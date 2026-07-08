package com.kidsactivities.common.event;

public final class RabbitConstants {

    public static final String EXCHANGE = "kids.events";

    public static final String USER_REGISTERED = "user.registered";
    public static final String BOOKING_CONFIRMED = "booking.confirmed";
    public static final String BOOKING_CANCELLED = "booking.cancelled";

    private RabbitConstants() {
    }
}
