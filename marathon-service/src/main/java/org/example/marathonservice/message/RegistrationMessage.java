package org.example.marathonservice.message;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RegistrationMessage implements Serializable {
    private Long registrationId;
    private Long userId;
    private Long raceId;
    private LocalDateTime requestTime;
    private String idempotentKey;
}
