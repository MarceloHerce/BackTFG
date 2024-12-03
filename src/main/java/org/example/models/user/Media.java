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
@Table(name = "users_media", uniqueConstraints = {@UniqueConstraint(columnNames = {"media_id"})})
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="media_id", nullable = false)
    Long mediaId;

    @ManyToOne
    @JoinColumn(name = "x_user_id", referencedColumnName = "user_id")
    User user;

    @Column(name="media_reference")
    String mediaReference;

    @Override
    public String toString() {
        return "Media{" +
                "id='" + mediaId + "'" +
                "userId='" + user.getUsername() + "'" +
                "mediaReference='" + mediaReference + "'}";

    }
}
