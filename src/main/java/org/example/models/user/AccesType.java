package org.example.models.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "access_type", uniqueConstraints = {@UniqueConstraint(columnNames = {"access_type_id"})})
public class AccesType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_type_id", nullable = false)
    private Long accessTypeId;

    @Column(nullable = false)
    private String description;

    public static final String GOOGLE_LOGIN = "Google Login";
    public static final String WEB_LOGIN = "Web Login";
    public static final String GOOGLE_AND_WEB_LOGIN = "Google and Web Login";
}
