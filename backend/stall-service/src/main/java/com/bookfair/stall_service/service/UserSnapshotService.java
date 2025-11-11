package com.bookfair.stall_service.service;

import com.bookfair.stall_service.dto.UserSnapshotDTO;
import com.bookfair.stall_service.entity.UserSnapshot;
import com.bookfair.stall_service.repository.UserSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserSnapshotService {

    @Autowired
    private UserSnapshotRepository userSnapshotRepository;

    public UserSnapshotDTO createOrUpdateUserSnapshot(UserSnapshotDTO userSnapshotDTO) {
        UserSnapshot userSnapshot = userSnapshotRepository.findByUserId(userSnapshotDTO.getUserId())
                .orElse(new UserSnapshot());

        // Generate new ID if it's a new snapshot
        if (userSnapshot.getId() == null) {
            userSnapshot.setId(UUID.randomUUID());
        }
        
        userSnapshot.setUserId(userSnapshotDTO.getUserId());
        userSnapshot.setFirstName(userSnapshotDTO.getFirstName());
        userSnapshot.setLastName(userSnapshotDTO.getLastName());
        userSnapshot.setEmail(userSnapshotDTO.getEmail());
        userSnapshot.setRole(userSnapshotDTO.getRole());
        userSnapshot.setStatus(userSnapshotDTO.getStatus());
        userSnapshot.setUpdatedAt(LocalDateTime.now());

        UserSnapshot savedSnapshot = userSnapshotRepository.save(userSnapshot);
        return convertToDTO(savedSnapshot);
    }

    public UserSnapshotDTO getUserSnapshotById(UUID id) {
        UserSnapshot userSnapshot = userSnapshotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User snapshot not found with id: " + id));
        return convertToDTO(userSnapshot);
    }

    public UserSnapshotDTO getUserSnapshotByUserId(UUID userId) {
        UserSnapshot userSnapshot = userSnapshotRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User snapshot not found for user id: " + userId));
        return convertToDTO(userSnapshot);
    }

    public List<UserSnapshotDTO> getAllUserSnapshots() {
        return userSnapshotRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteUserSnapshot(UUID id) {
        if (!userSnapshotRepository.existsById(id)) {
            throw new RuntimeException("User snapshot not found with id: " + id);
        }
        userSnapshotRepository.deleteById(id);
    }

    private UserSnapshotDTO convertToDTO(UserSnapshot userSnapshot) {
        UserSnapshotDTO dto = new UserSnapshotDTO();
        dto.setId(userSnapshot.getId());
        dto.setUserId(userSnapshot.getUserId());
        dto.setFirstName(userSnapshot.getFirstName());
        dto.setLastName(userSnapshot.getLastName());
        dto.setEmail(userSnapshot.getEmail());
        dto.setRole(userSnapshot.getRole());
        dto.setStatus(userSnapshot.getStatus());
        dto.setUpdatedAt(userSnapshot.getUpdatedAt());
        return dto;
    }
}
