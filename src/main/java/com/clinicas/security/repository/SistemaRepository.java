package com.clinicas.security.repository;

import com.clinicas.security.entity.Sistema;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SistemaRepository extends JpaRepository<Sistema, Long> {
    Optional<Sistema> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
