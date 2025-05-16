package example.bankcards.exception.user;

public class UsernameExists extends RuntimeException {
    public UsernameExists(String request) {
        super("Username already exists: " + request);
    }
}

