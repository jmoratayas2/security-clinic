package com.clinicas.security.repository;

import com.clinicas.security.entity.IntentoLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntentoLoginRepository extends JpaRepository<IntentoLogin, Long> {
    List<IntentoLogin> findByUsernameOrderByFechaHoraDesc(String username);
    List<IntentoLogin> findTop50ByOrderByFechaHoraDesc();
}
