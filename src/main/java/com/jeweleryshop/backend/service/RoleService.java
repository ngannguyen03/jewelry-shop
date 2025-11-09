package com.jeweleryshop.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.dto.RoleDTO;
import com.jeweleryshop.backend.entity.Role;
import com.jeweleryshop.backend.exception.DuplicateResourceException;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
    }

    public Optional<Role> findByNameOptional(String name) {
        return roleRepository.findByName(name);
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return convertToDTO(role);
    }

    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        roleRepository.findByName(roleDTO.getName().toUpperCase()).ifPresent(r -> {
            throw new DuplicateResourceException("Role already exists with name: " + roleDTO.getName());
        });
        Role role = new Role();
        role.setName(roleDTO.getName().toUpperCase());
        return convertToDTO(roleRepository.save(role));
    }

    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        roleRepository.findByName(roleDTO.getName().toUpperCase()).ifPresent(r -> {
            if (!r.getId().equals(id)) {
                throw new DuplicateResourceException("Role already exists with name: " + roleDTO.getName());
            }
        });
        role.setName(roleDTO.getName().toUpperCase());
        return convertToDTO(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    private RoleDTO convertToDTO(Role role) {
        return new RoleDTO(role.getId(), role.getName());
    }
}
