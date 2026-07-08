package com.kidsactivities.activity.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "catalogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Catalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 8)
    private String emoji;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int sortOrder;
}
