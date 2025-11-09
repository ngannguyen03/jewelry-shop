package com.jeweleryshop.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jeweleryshop.backend.entity.OtpToken;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findByUsername(String username);

    void deleteByUsername(String username);
}
