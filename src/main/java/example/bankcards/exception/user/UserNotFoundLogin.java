package example.bankcards.exception.user;

public class UserNotFoundLogin extends RuntimeException {
    public UserNotFoundLogin(String username) {
        super("User with login '" + username + "' not found");
    }
}

