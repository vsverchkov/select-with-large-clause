package com.vsverchkov.selectwithlargeclause;

import org.springframework.data.relational.core.mapping.Table;

@Table("product_recommendation")
public record ProductRecommendation(Integer id, Integer productId1, Integer productId2) {}
