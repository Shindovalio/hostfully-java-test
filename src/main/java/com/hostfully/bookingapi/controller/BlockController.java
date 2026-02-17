package com.hostfully.bookingapi.controller;

import com.hostfully.bookingapi.dto.BlockRequest;
import com.hostfully.bookingapi.dto.BlockResponse;
import com.hostfully.bookingapi.service.BlockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping
    public ResponseEntity<BlockResponse> create(@Valid @RequestBody BlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(blockService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlockResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody BlockRequest request) {
        return ResponseEntity.ok(blockService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        blockService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
