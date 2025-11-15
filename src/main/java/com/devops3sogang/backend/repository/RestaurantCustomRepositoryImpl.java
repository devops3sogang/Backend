package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.SortBy;
import com.devops3sogang.backend.dto.RestaurantSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Metrics;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RestaurantCustomRepositoryImpl implements RestaurantCustomRepository {
    
    private final MongoTemplate mongoTemplate;
    //private final RestaurantRepository restaurantRepository;
    
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
            List<Restaurant> result = findByDistance(
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
            Query query = new Query(Criteria.where("isActive").is(true));

            if (hasCategory) {
                query.addCriteria(Criteria.where("category").is(category));
            }

            query.with(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "stats.rating"
            ));

            return mongoTemplate.find(query, Restaurant.class);
        }

        if (sortBy == SortBy.DISTANCE) {
            return findByDistance(
                req.getLatitude(),
                req.getLongitude(),
                null,  // unlimited
                category
            );
        }

        Query query = new Query(Criteria.where("isActive").is(true));

        if (hasCategory) {
            query.addCriteria(Criteria.where("category").is(category));
        }

        return mongoTemplate.find(query, Restaurant.class);
    }

    private List<Restaurant> findByDistance(Double latitude, Double longitude, Integer radiusInMeters, String category) {
        Point location = new Point(longitude, latitude);  // GeoJSON: 경도, 위도 순서
        
        NearQuery nearQuery = NearQuery.near(location);
        
        // 거리 제한
        if (radiusInMeters != null && radiusInMeters > 0) {
            nearQuery.maxDistance(new Distance(radiusInMeters / 1000.0, Metrics.KILOMETERS));
        }
        
        // 기본 조건: isActive = true
        Criteria criteria = Criteria.where("isActive").is(true);
        
        // 카테고리 필터
        if (category != null && !category.isBlank()) {
            criteria.and("category").is(category);
        }
        
        nearQuery.query(Query.query(criteria));
        
        // 거리순으로 정렬되어 반환됨
        GeoResults<Restaurant> results = mongoTemplate.geoNear(nearQuery, Restaurant.class);
        
        return results.getContent()
                     .stream()
                     .map(result -> result.getContent())
                     .collect(Collectors.toList());
    }
}