package com.apex.carwash.dto;

import com.apex.carwash.entity.Branch;

public record BranchResponse(
        Long id,
        String name,
        String location
) {
    public static BranchResponse from(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getLocation());
    }
}
