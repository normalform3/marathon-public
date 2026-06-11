package org.example.marathonservice.constants;

public final class MqConstant {
    public static final String REGISTRATION_EXCHANGE = "marathon.registration.exchange";
    public static final String REGISTRATION_QUEUE = "marathon.registration.queue";
    public static final String REGISTRATION_DEAD_LETTER_EXCHANGE = "marathon.registration.dlx";
    public static final String REGISTRATION_DEAD_LETTER_QUEUE = "marathon.registration.dlq";
    public static final String REGISTRATION_ROUTING_KEY = "registration.create";
    public static final String REGISTRATION_DEAD_LETTER_ROUTING_KEY = "registration.create.dlq";

    private MqConstant() {
    }
}
