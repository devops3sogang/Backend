package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Restaurant;
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
    public List<Restaurant> findByDistance(
            double latitude, double longitude, double maxDistanceInMeters,
            String type, String category) {
        
        List<AggregationOperation> operations = new ArrayList<>();
        
        // 1. 거리 기반 필터링 및 정렬
        operations.add(Aggregation.geoNear(
            NearQuery.near(longitude, latitude)
                    .maxDistance(maxDistanceInMeters / 1000.0, Metrics.KILOMETERS)
                    .spherical(true),
            "distance"
        ));
        
        // 2. 활성 상태 필터
        operations.add(Aggregation.match(Criteria.where("isActive").is(true)));
        
        // 3. 타입 필터 (옵션)
        if (type != null && !type.isEmpty()) {
            operations.add(Aggregation.match(Criteria.where("type").is(type)));
        }
        
        // 4. 카테고리 필터 (옵션)
        if (category != null && !category.isEmpty()) {
            operations.add(Aggregation.match(Criteria.where("category").is(category)));
        }
        
        Aggregation aggregation = Aggregation.newAggregation(operations);
        
        return mongoTemplate.aggregate(aggregation, "restaurants", Restaurant.class)
                .getMappedResults();
    }
}