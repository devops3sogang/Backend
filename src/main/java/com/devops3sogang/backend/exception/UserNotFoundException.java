public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("사용자 정보를 찾을 수 없습니다. Email: " + email);
    }
}