package com.jeweleryshop.backend.service;

import com.jeweleryshop.backend.dto.PromotionDTO;
import com.jeweleryshop.backend.dto.PromotionRequestDTO;
import com.jeweleryshop.backend.entity.Promotion;
import com.jeweleryshop.backend.exception.AppException;
import com.jeweleryshop.backend.exception.DuplicateResourceException;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.PromotionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Transactional(readOnly = true)
    public Promotion validatePromotionCode(String code) {
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid promotion code."));

        if (promotion.getStartDate().isAfter(LocalDateTime.now())) {
            throw new AppException("This promotion has not started yet.");
        }

        if (promotion.getEndDate().isBefore(LocalDateTime.now())) {
            throw new AppException("This promotion has expired.");
        }

        if (promotion.getCurrentUsage() >= promotion.getMaxUsage()) {
            throw new AppException("This promotion has reached its usage limit.");
        }

        return promotion;
    }

    @Transactional
    public PromotionDTO createPromotion(PromotionRequestDTO requestDTO) {
        if (promotionRepository.findByCode(requestDTO.getCode()).isPresent()) {
            throw new DuplicateResourceException("Promotion code '" + requestDTO.getCode() + "' already exists.");
        }
        if (requestDTO.getStartDate().isAfter(requestDTO.getEndDate())) {
            throw new AppException("Start date must be before end date.");
        }

        Promotion promotion = new Promotion();
        mapDtoToEntity(requestDTO, promotion);

        return convertToDTO(promotionRepository.save(promotion));
    }

    @Transactional(readOnly = true)
    public Page<PromotionDTO> getAllPromotions(Pageable pageable) {
        return promotionRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional
    public PromotionDTO updatePromotion(Long id, PromotionRequestDTO requestDTO) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

        promotionRepository.findByCode(requestDTO.getCode()).ifPresent(p -> {
            if (!p.getId().equals(id)) {
                throw new DuplicateResourceException("Promotion code '" + requestDTO.getCode() + "' already exists.");
            }
        });

        mapDtoToEntity(requestDTO, promotion);
        return convertToDTO(promotionRepository.save(promotion));
    }

    @Transactional
    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Promotion not found with id: " + id);
        }
        // Consider business logic: what if a promotion is active?
        // For now, we allow deletion.
        promotionRepository.deleteById(id);
    }

    @Transactional
    public void incrementUsage(String code) {
        Promotion promotion = promotionRepository.findByCode(code).orElse(null);
        if (promotion != null) {
            promotion.setCurrentUsage(promotion.getCurrentUsage() + 1);
            promotionRepository.save(promotion);
        }
    }

    private PromotionDTO convertToDTO(Promotion promotion) {
        return new PromotionDTO(
                promotion.getId(),
                promotion.getCode(),
                promotion.getDescription(),
                promotion.getDiscountType(),
                promotion.getDiscountValue(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getMinOrderAmount(),
                promotion.getMaxUsage(),
                promotion.getCurrentUsage()
        );
    }

    private void mapDtoToEntity(PromotionRequestDTO dto, Promotion entity) {
        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());
        entity.setDiscountType(dto.getDiscountType());
        entity.setDiscountValue(dto.getDiscountValue());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setMinOrderAmount(dto.getMinOrderAmount());
        entity.setMaxUsage(dto.getMaxUsage());
    }
}
