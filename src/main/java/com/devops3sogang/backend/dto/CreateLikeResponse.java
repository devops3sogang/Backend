package com.devops3sogang.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateLikeResponse {
    private LikeDto createdLike;
    private LikeDto deletedLike;
    private int likeCount;

    // 좋아요 생성 시 사용
    public CreateLikeResponse(LikeDto createdLike, int likeCount) {
        this.createdLike = createdLike;
        this.likeCount = likeCount;
    }

    // 좋아요 취소 시 사용
    public static CreateLikeResponse forDeletion(LikeDto deletedLike, int likeCount) {
        CreateLikeResponse response = new CreateLikeResponse(null, likeCount);
        response.deletedLike = deletedLike;
        response.createdLike = null;
        return response;
    }

    @Data
    public static class LikeDto {
        private String _id;
        private String userId;
        private String reviewId;
        private String createdAt;

        public LikeDto(String id, String userId, String reviewId, String createdAt) {
            this._id = id;
            this.userId = userId;
            this.reviewId = reviewId;
            this.createdAt = createdAt;
        }
    }
}
