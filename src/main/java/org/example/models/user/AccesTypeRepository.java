package org.example.models.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccesTypeRepository extends JpaRepository<AccesType, Long> {
    AccesType findByDescription(String description);
}
