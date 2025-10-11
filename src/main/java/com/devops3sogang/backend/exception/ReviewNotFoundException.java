public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String id) {
        super("리뷰 정보를 찾을 수 없습니다. ID: " + id);
    }
}