package com.kidsactivities.booking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "L'activité est obligatoire")
    private Long activityId;

    @NotBlank(message = "Le prénom de l'enfant est obligatoire")
    private String childName;

    @NotNull(message = "L'âge de l'enfant est obligatoire")
    @Min(value = 1, message = "L'âge minimum est 1 an")
    @Max(value = 18, message = "L'âge maximum est 18 ans")
    private Integer childAge;
}
