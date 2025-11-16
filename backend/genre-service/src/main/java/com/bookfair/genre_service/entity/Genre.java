package com.bookfair.genre.entity;

import com.bookfair.genre.dto.UserSnapshot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "genres")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;

    private Long userId;

    private String genreName;

    @Column(columnDefinition = "jsonb")
    private String userSnapshot; // stored as JSON string

    private LocalDateTime createdAt;
}
