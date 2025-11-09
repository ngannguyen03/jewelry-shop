package com.jeweleryshop.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jeweleryshop.backend.entity.Banner;

public interface BannerRepository extends JpaRepository<Banner, Long> {
}
