package com.example.department_service.controller;

import com.example.department_service.model.Department;
import com.example.department_service.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    List<Department> getAll(){
        return departmentRepository.findAll();
    }
}
