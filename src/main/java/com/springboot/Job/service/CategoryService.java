package com.springboot.Job.service;

import com.springboot.Job.model.CategoryBean;
import com.springboot.Job.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;


//Add in 15.10.2025
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryRepository.CategoryWithCount> getAllCategoriesWithJobCounts() {
        return categoryRepository.getCategoriesWithJobCounts();
    }

    public List<CategoryBean> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Map<Integer, Integer> getJobCountsMap() {
        return categoryRepository.getAllCategoriesWithJobCounts();
    }

    public Map<String, Object> getCategoryById(Integer categoryId) {
        Optional<CategoryBean> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isPresent()) {
            CategoryBean category = categoryOpt.get();
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", category.getId());
            categoryMap.put("catName", category.getCatName());
            categoryMap.put("adminId", category.getAdminId());
            return categoryMap;
        }
        return null;
    }

    public List<CategoryBean> getCategoriesWithJobs() {
        return categoryRepository.findCategoriesWithJobs();
    }

    public List<Map<String, Object>> getCategoriesWithJobCountsForView() {
        try {
            List<CategoryRepository.CategoryWithCount> categoriesWithCounts = 
                categoryRepository.getCategoriesWithJobCounts();
            
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (CategoryRepository.CategoryWithCount category : categoriesWithCounts) {
                Map<String, Object> categoryMap = new HashMap<>();
                categoryMap.put("id", category.getId());
                categoryMap.put("catName", category.getCatName());
                categoryMap.put("jobCount", category.getJobCount());
                result.add(categoryMap);
            }
            
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}