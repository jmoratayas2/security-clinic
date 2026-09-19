package com.clinicas.security.repository;

import com.clinicas.security.entity.Modulo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {
    List<Modulo> findBySistemaId(Long idSistema);
    List<Modulo> findByModuloPadreIsNullAndSistemaId(Long idSistema);
    List<Modulo> findByModuloPadreId(Long idModuloPadre);
    boolean existsByNombreAndSistemaId(String nombre, Long idSistema);
}
