package com.jeweleryshop.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.dto.AddressDTO;
import com.jeweleryshop.backend.dto.AddressRequestDTO;
import com.jeweleryshop.backend.entity.Address;
import com.jeweleryshop.backend.entity.User;
import com.jeweleryshop.backend.exception.AppException;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.AddressRepository;
import com.jeweleryshop.backend.repository.UserRepository;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressDTO> getMyAddresses() {
        User currentUser = getCurrentUser();
        return currentUser.getAddresses().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO addAddress(AddressRequestDTO requestDTO) {
        User currentUser = getCurrentUser();

        Address address = new Address();
        address.setUser(currentUser);
        address.setFullName(requestDTO.getFullName());
        address.setPhoneNumber(requestDTO.getPhoneNumber());
        address.setStreetAddress(requestDTO.getStreetAddress());
        address.setCity(requestDTO.getCity());
        address.setDistrict(requestDTO.getDistrict());
        address.setWard(requestDTO.getWard());

        Address savedAddress = addressRepository.save(address);
        return convertToDTO(savedAddress);
    }

    @Transactional
    public AddressDTO updateAddress(Long addressId, AddressRequestDTO requestDTO) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new AppException("You are not authorized to update this address.");
        }

        address.setFullName(requestDTO.getFullName());
        address.setPhoneNumber(requestDTO.getPhoneNumber());
        address.setStreetAddress(requestDTO.getStreetAddress());
        address.setCity(requestDTO.getCity());
        address.setDistrict(requestDTO.getDistrict());
        address.setWard(requestDTO.getWard());

        Address updatedAddress = addressRepository.save(address);
        return convertToDTO(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new AppException("You are not authorized to delete this address.");
        }

        addressRepository.delete(address);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private AddressDTO convertToDTO(Address address) {
        return new AddressDTO(
                address.getId(),
                address.getFullName(),
                address.getPhoneNumber(),
                address.getStreetAddress(),
                address.getCity(),
                address.getDistrict(),
                address.getWard());
    }
}
