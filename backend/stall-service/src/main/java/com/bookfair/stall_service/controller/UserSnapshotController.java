package com.bookfair.stall_service.controller;

import com.bookfair.stall_service.dto.UserSnapshotDTO;
import com.bookfair.stall_service.service.UserSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-snapshots")
@CrossOrigin(origins = "*")
public class UserSnapshotController {

    @Autowired
    private UserSnapshotService userSnapshotService;

    @PostMapping
    public ResponseEntity<UserSnapshotDTO> createOrUpdateUserSnapshot(@RequestBody UserSnapshotDTO userSnapshotDTO) {
        try {
            UserSnapshotDTO createdSnapshot = userSnapshotService.createOrUpdateUserSnapshot(userSnapshotDTO);
            return new ResponseEntity<>(createdSnapshot, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserSnapshotDTO> getUserSnapshotById(@PathVariable UUID id) {
        try {
            UserSnapshotDTO userSnapshot = userSnapshotService.getUserSnapshotById(id);
            return new ResponseEntity<>(userSnapshot, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserSnapshotDTO> getUserSnapshotByUserId(@PathVariable UUID userId) {
        try {
            UserSnapshotDTO userSnapshot = userSnapshotService.getUserSnapshotByUserId(userId);
            return new ResponseEntity<>(userSnapshot, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<UserSnapshotDTO>> getAllUserSnapshots() {
        try {
            List<UserSnapshotDTO> userSnapshots = userSnapshotService.getAllUserSnapshots();
            return new ResponseEntity<>(userSnapshots, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUserSnapshot(@PathVariable UUID id) {
        try {
            userSnapshotService.deleteUserSnapshot(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
