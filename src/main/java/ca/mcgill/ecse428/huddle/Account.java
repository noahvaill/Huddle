package ca.mcgill.ecse428.huddle;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A registered Huddle account (ID001): the user it belongs to, their email address, their password and their role.
 * The email is stored stripped and in lower case, so the same address typed in any case finds the same account.
 * The User stays a separate record so the game code (ID004-ID010), which identifies people by name, is unchanged.
 * The user's skill levels per sport (ID014) are kept here too, since the User record is replaced when the name changes.
 */
public final class Account {

    private User user;
    private final String email;
    private String password;
    private final Role role;
    private final Map<Sport, SkillLevel> skillLevels = new EnumMap<>(Sport.class);

    public Account(User user, String email, String password, Role role) {
        this.user = Objects.requireNonNull(user, "User is required");
        this.email = normalizeEmail(email);
        Objects.requireNonNull(password, "Password is required");
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        this.password = password;
        this.role = Objects.requireNonNull(role, "Role is required");
    }

    /** The form an email address is stored and looked up in: stripped and in lower case. */
    public static String normalizeEmail(String email) {
        Objects.requireNonNull(email, "Email is required");
        String normalized = email.strip().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        return normalized;
    }

    public User getUser() { return user; }
    public String getName() { return user.name(); }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

    /** Whether the given password is this account's password. Passwords are case-sensitive and never stripped. */
    public boolean passwordMatches(String candidate) {
        return password.equals(candidate);
    }

    /**
     * Changes the account's name (ID013): the account now belongs to the user with the new name, stripped.
     * A missing or blank name is rejected by User and the name is kept.
     */
    public void changeName(String newName) {
        this.user = new User(newName);
    }

    /** Replaces the password (ID011, ID013), with the same rules as a new account's password. */
    public void changePassword(String newPassword) {
        Objects.requireNonNull(newPassword, "Password is required");
        if (newPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        this.password = newPassword;
    }

    /** Sets the user's skill level for a sport (ID014, TID048), replacing the level that sport had, if any. */
    public void setSkillLevel(Sport sport, SkillLevel level) {
        Objects.requireNonNull(sport, "Sport is required");
        Objects.requireNonNull(level, "Skill level is required");
        skillLevels.put(sport, level);
    }

    /** The user's skill level for a sport; empty if none has been set. */
    public Optional<SkillLevel> getSkillLevel(Sport sport) {
        Objects.requireNonNull(sport, "Sport is required");
        return Optional.ofNullable(skillLevels.get(sport));
    }

    /** All the user's skill levels, in the order sports are offered. The map cannot be changed. */
    public Map<Sport, SkillLevel> getSkillLevels() {
        return Collections.unmodifiableMap(new EnumMap<>(skillLevels));
    }

    /** Never shows the password. */
    @Override
    public String toString() {
        return user + " <" + email + "> (" + role.getDisplayName() + ")";
    }
}
