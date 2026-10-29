package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.entity.Category;
import com.campus.market.mapper.CategoryMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @GetMapping
    public Result<List<Category>> list() {
        return Result.ok(categoryMapper.findAll());
    }
}
