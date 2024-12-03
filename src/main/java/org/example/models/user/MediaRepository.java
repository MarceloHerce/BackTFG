package org.example.models.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MediaRepository extends JpaRepository<Media,Integer> {
    List<Media> findMediaByUserUserId(Long userId);

    @Query("SELECT m.mediaReference FROM Media m WHERE m.user.userId = :userId")
    List<String> findMediaReferencesByUserId(@Param("userId") Long userId);
}
