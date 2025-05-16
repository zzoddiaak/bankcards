package example.bankcards.exception.user;

public class UserNotFound extends RuntimeException {
    public UserNotFound(Long id) {
        super("User '" + id + "' not found");
    }
}

