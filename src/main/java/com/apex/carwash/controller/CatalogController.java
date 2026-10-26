package com.apex.carwash.controller;

import com.apex.carwash.dto.BranchResponse;
import com.apex.carwash.dto.ServiceResponse;
import com.apex.carwash.entity.Branch;
import com.apex.carwash.entity.WashingService;
import com.apex.carwash.repository.BranchRepository;
import com.apex.carwash.repository.WashingServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogController {

    private final BranchRepository branches;
    private final WashingServiceRepository services;

    @GetMapping("/branches")
    public List<BranchResponse> branches() {
        List<BranchResponse> responses = new ArrayList<>();
        for (Branch branch : branches.findAll(Sort.by("id"))) {
            responses.add(BranchResponse.from(branch));
        }
        return responses;
    }

    @GetMapping("/services")
    public List<ServiceResponse> services() {
        List<ServiceResponse> responses = new ArrayList<>();
        for (WashingService service : services.findAll(Sort.by("id"))) {
            responses.add(ServiceResponse.from(service));
        }
        return responses;
    }
}
