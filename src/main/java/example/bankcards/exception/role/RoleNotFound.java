package example.bankcards.exception.role;

public class RoleNotFound extends RuntimeException {
    public RoleNotFound(Long roleId) {
        super("Role '" + roleId + "' not found");
    }
}

