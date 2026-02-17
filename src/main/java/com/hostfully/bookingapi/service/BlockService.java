package com.hostfully.bookingapi.service;

import com.hostfully.bookingapi.dto.BlockRequest;
import com.hostfully.bookingapi.dto.BlockResponse;
import com.hostfully.bookingapi.entity.Block;
import com.hostfully.bookingapi.enums.BookingStatus;
import com.hostfully.bookingapi.exception.OverlapException;
import com.hostfully.bookingapi.exception.ResourceNotFoundException;
import com.hostfully.bookingapi.repository.BlockRepository;
import com.hostfully.bookingapi.repository.BookingRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BlockService {

    private final BlockRepository blockRepository;
    private final BookingRepository bookingRepository;

    public BlockService(BlockRepository blockRepository, BookingRepository bookingRepository) {
        this.blockRepository = blockRepository;
        this.bookingRepository = bookingRepository;
    }

    public BlockResponse create(BlockRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        checkForOverlaps(request.getPropertyId(), request.getStartDate(), request.getEndDate(), null);

        Block block = new Block();
        block.setPropertyId(request.getPropertyId());
        block.setReason(request.getReason());
        block.setStartDate(request.getStartDate());
        block.setEndDate(request.getEndDate());

        return toResponse(blockRepository.save(block));
    }

    public BlockResponse update(Long id, BlockRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        Block block = findBlockOrThrow(id);

        checkForOverlaps(request.getPropertyId(), request.getStartDate(), request.getEndDate(), id);

        block.setPropertyId(request.getPropertyId());
        block.setReason(request.getReason());
        block.setStartDate(request.getStartDate());
        block.setEndDate(request.getEndDate());

        return toResponse(blockRepository.save(block));
    }

    public void delete(Long id) {
        Block block = findBlockOrThrow(id);
        blockRepository.delete(block);
    }

    private void checkForOverlaps(String propertyId, java.time.LocalDate startDate,
                                  java.time.LocalDate endDate, Long excludeBlockId) {
        if (!bookingRepository.findOverlapping(propertyId, startDate, endDate, BookingStatus.ACTIVE).isEmpty()) {
            throw new OverlapException("Block overlaps with an existing active booking");
        }

        List<Block> overlappingBlocks = blockRepository.findOverlapping(propertyId, startDate, endDate);

        if (excludeBlockId != null) {
            overlappingBlocks = overlappingBlocks.stream()
                    .filter(b -> !b.getId().equals(excludeBlockId))
                    .toList();
        }

        if (!overlappingBlocks.isEmpty()) {
            throw new OverlapException("Block overlaps with an existing block");
        }
    }

    private void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
    }

    private Block findBlockOrThrow(Long id) {
        return blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block not found with id: " + id));
    }

    private BlockResponse toResponse(Block block) {
        BlockResponse response = new BlockResponse();
        response.setId(block.getId());
        response.setPropertyId(block.getPropertyId());
        response.setReason(block.getReason());
        response.setStartDate(block.getStartDate());
        response.setEndDate(block.getEndDate());
        return response;
    }
}
