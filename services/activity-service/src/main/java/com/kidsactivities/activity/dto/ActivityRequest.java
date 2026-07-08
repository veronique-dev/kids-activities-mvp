package com.kidsactivities.activity.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotBlank(message = "Le résumé est obligatoire")
    @Size(max = 500)
    private String description;

    @NotBlank(message = "Les détails sont obligatoires")
    @Size(max = 5000)
    private String details;

    @NotBlank(message = "Les prérequis sont obligatoires")
    @Size(max = 2000)
    private String prerequisites;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date doit être dans le futur")
    private LocalDateTime startDateTime;

    @NotBlank(message = "Le lieu est obligatoire")
    private String location;

    @NotNull(message = "La capacité maximale est obligatoire")
    @Min(value = 1, message = "La capacité doit être au moins 1")
    private Integer maxCapacity;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix ne peut pas être négatif")
    private BigDecimal price;

    @NotNull(message = "Le catalogue est obligatoire")
    private Long catalogId;

    @NotNull(message = "La date limite d'inscription est obligatoire")
    private LocalDateTime registrationDeadline;

    private boolean active = true;
}
