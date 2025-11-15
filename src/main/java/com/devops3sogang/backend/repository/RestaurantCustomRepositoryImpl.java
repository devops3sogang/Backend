package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.dto.RestaurantSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.geo.Metrics;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RestaurantCustomRepositoryImpl implements RestaurantCustomRepository {
    
    private final MongoTemplate mongoTemplate;
    
    @Override
    public List<Restaurant> search(RestaurantSearchRequest req) {

        String category = req.getCategory();
        Integer radius = req.getRadius();
        SortBy sortBy = req.getSortBy() != null ? req.getSortBy() : SortBy.NONE;

        boolean hasCategory = category != null && !category.isBlank();
        boolean hasRadius = radius != null && radius > 0;

        // 좌표 유효성 체크
        if ((sortBy == SortBy.DISTANCE || hasRadius)
            && (req.getLatitude() == null || req.getLongitude() == null)) {
            throw new IllegalArgumentException("위도/경도는 거리 기반 탐색에 필요합니다.");
        }

        if (hasRadius) {

            List<Restaurant> result = restaurantRepository.findByDistance(
                req.getLatitude(),
                req.getLongitude(),
                radius,
                category
            );

            if (sortBy == SortBy.RATING) {
                result.sort(
                    Comparator.comparingDouble(
                        r -> r.getStats() != null && r.getStats().getRating() != null
                            ? r.getStats().getRating()
                            : 0.0
                    ).reversed()
                );
            }

            return result;
        }

        if (sortBy == SortBy.RATING) {
            if (hasCategory) {
                return restaurantRepository
                    .findByCategoryAndIsActiveTrueOrderByStats_RatingDesc(category);
            } else {
                return restaurantRepository
                    .findByIsActiveTrueOrderByStats_RatingDesc();
            }
        }

        if (sortBy == SortBy.DISTANCE) {
            return restaurantRepository.findByDistance(
                req.getLatitude(),
                req.getLongitude(),
                null,  // unlimited
                category
            );
        }

        List<Restaurant> result;
        if (hasCategory) {
            result = restaurantRepository.findByCategoryAndIsActiveTrue(category);
        } else {
            result = restaurantRepository.findByIsActiveTrue();
        }

        return result;
    }
}