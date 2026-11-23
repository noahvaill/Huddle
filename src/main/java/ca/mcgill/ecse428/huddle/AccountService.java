package ca.mcgill.ecse428.huddle;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The registry of Huddle accounts, one per email address (ID001).
 * Log in (ID002), publishing time slots (ID003), resetting a password (ID011) and logging out (ID012)
 * look accounts up here.
 */
public class AccountService {

    public static final String ACCOUNT_CREATED_MESSAGE = "Account created";
    public static final String PROFILE_UPDATED_MESSAGE = "Profile updated";
    public static final String SKILL_LEVEL_SAVED_MESSAGE = "Skill level saved";
    public static final String LOGGED_IN_MESSAGE_FORMAT = "Welcome back, %s";   // user name
    public static final String LOGGED_OUT_MESSAGE = "You have been logged out";
    /** The device used when a log in does not name one (ID002). */
    public static final String DEFAULT_DEVICE = "Default device";
    public static final String PASSWORD_RESET_MESSAGE = "Your password has been reset";
    /** Issued for every reset request, so the message does not reveal which addresses have an account (TID024). */
    public static final String RESET_LINK_SENT_MESSAGE =
            "If an account exists for this email address, a password reset link has been sent";
    /** How long a requested reset link stays valid (ID011). */
    public static final Duration RESET_LINK_VALIDITY = Duration.ofHours(1);
    private static final String STUDENT_EMAIL_DOMAIN = "@mail.mcgill.ca";
    private static final String STAFF_EMAIL_DOMAIN = "@mcgill.ca";

    private final Map<String, Account> accountsByEmail = new LinkedHashMap<>();
    private final Map<String, PasswordResetLink> resetLinksByToken = new HashMap<>();
    private Clock clock = Clock.systemDefaultZone();
    /** The active session on each device (ID012), in the order they were started. */
    private final Map<String, Session> sessionsByDevice = new LinkedHashMap<>();

    /**
     * Creates an account for a McGill student or staff member (ID001) and returns it.
     * Only @mail.mcgill.ca (student) and @mcgill.ca (staff) addresses are accepted, in any case and with
     * surrounding spaces, and an email address can have only one account. A student address gets the Student role
     * and a staff address the Staff role. Nothing is added when a rule is broken.
     */
    public Account createAccount(String name, String email, String password) {
        Role role = roleForMcGillEmail(email);
        if (findByEmail(email).isPresent()) {
            throw new InvalidAccountException(InvalidAccountException.EMAIL_ALREADY_IN_USE_MESSAGE);
        }
        Account account = new Account(new User(name), email, password, role);
        addAccount(account);
        return account;
    }

    /** Student or Staff for a McGill address with exactly one @ and something before it; fails for any other address. */
    private static Role roleForMcGillEmail(String email) {
        String normalized = email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
        int at = normalized.indexOf('@');
        boolean oneAtWithNameBeforeIt = at > 0 && at == normalized.lastIndexOf('@');
        if (oneAtWithNameBeforeIt && normalized.endsWith(STUDENT_EMAIL_DOMAIN)) {
            return Role.STUDENT;
        }
        if (oneAtWithNameBeforeIt && normalized.endsWith(STAFF_EMAIL_DOMAIN)) {
            return Role.STAFF;
        }
        throw new InvalidAccountException(InvalidAccountException.MCGILL_EMAIL_REQUIRED_MESSAGE);
    }

    /**
     * Adds an existing account as it is, without createAccount's McGill rules, e.g. the accounts in a feature file's
     * Background. Fails if an account already exists for its email address, in any case.
     */
    public void addAccount(Account account) {
        Objects.requireNonNull(account, "Account is required");
        if (accountsByEmail.containsKey(account.getEmail())) {
            throw new IllegalArgumentException("An account already exists for " + account.getEmail());
        }
        accountsByEmail.put(account.getEmail(), account);
    }

    /** The account for this email address, in any case and with surrounding spaces; empty if there is none. */
    public Optional<Account> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountsByEmail.get(email.strip().toLowerCase(Locale.ROOT)));
    }

    /** The first account that belongs to this user, for steps that name a user (e.g. "Frank Ho" is logged in). */
    public Optional<Account> findByUser(User user) {
        return accountsByEmail.values().stream().filter(a -> a.getUser().equals(user)).findFirst();
    }

    /** Logs in on DEFAULT_DEVICE (ID002); see {@link #logIn(String, String, String)}. */
    public Session logIn(String email, String password) {
        return logIn(email, password, DEFAULT_DEVICE);
    }

    /**
     * Logs in on a device with an email address and password (ID002, TID009) and returns the new active session,
     * which carries the account's role. The email is matched in any case and with surrounding spaces; the password
     * must match exactly. A device has one session, so logging in replaces any session that was active on it; the
     * sessions on other devices are kept (ID012, TID033).
     * A wrong password and an unknown email give the same error (TID010), so the error does not reveal which
     * addresses have an account, and the active sessions are left unchanged.
     */
    public Session logIn(String email, String password, String device) {
        requireDevice(device);
        Account account = findByEmail(email)
                .filter(a -> a.passwordMatches(password))
                .orElseThrow(() -> new InvalidAccountException(InvalidAccountException.INVALID_CREDENTIALS_MESSAGE));
        Session session = new Session(account);
        sessionsByDevice.remove(device); // so the new session goes last, see getActiveSession
        sessionsByDevice.put(device, session);
        return session;
    }

    /**
     * Logs out on one device (ID012, TID033): ends the session on this device and keeps the sessions on other
     * devices, including the same account's. A device with no active session gives NOT_LOGGED_IN_MESSAGE (TID034),
     * and nothing changes.
     */
    public void logOut(String device) {
        requireDevice(device);
        if (sessionsByDevice.remove(device) == null) {
            throw new InvalidAccountException(InvalidAccountException.NOT_LOGGED_IN_MESSAGE);
        }
    }

    private static void requireDevice(String device) {
        Objects.requireNonNull(device, "Device is required");
        if (device.isBlank()) {
            throw new IllegalArgumentException("Device must not be blank");
        }
    }

    /** The most recently started session that is still active, on any device; empty if nobody is logged in. */
    public Optional<Session> getActiveSession() {
        return sessionsByDevice.values().stream().reduce((earlier, later) -> later);
    }

    /** The active session on this device; empty if nobody is logged in on it. */
    public Optional<Session> getActiveSession(String device) {
        return Optional.ofNullable(sessionsByDevice.get(device));
    }

    /** Whether this user has an active session on any device, for steps like "Alice Chen" is logged in to Huddle. */
    public boolean isLoggedIn(User user) {
        return sessionsByDevice.values().stream().anyMatch(s -> s.user().equals(user));
    }

    /** Whether this user has the active session on this device. */
    public boolean isLoggedIn(User user, String device) {
        return getActiveSession(device).filter(s -> s.user().equals(user)).isPresent();
    }

    /** The message issued when an account logs in, e.g. "Welcome back, Alice Chen". */
    public static String loggedInMessage(Account account) {
        return String.format(LOGGED_IN_MESSAGE_FORMAT, account.getName());
    }

    /**
     * Changes the name of this user's account (ID013, TID041) and returns the account; the email, role, password and
     * any active session are kept, and the session shows the new name. Fails if the user has no account.
     * A blank name gives NAME_EMPTY_MESSAGE and the name is kept.
     */
    public Account changeName(User user, String newName) {
        Account account = findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("No account for " + user));
        Objects.requireNonNull(newName, "Name is required");
        if (newName.isBlank()) {
            throw new InvalidAccountException(InvalidAccountException.NAME_EMPTY_MESSAGE);
        }
        account.changeName(newName);
        return account;
    }

    /**
     * Changes the password of the account with this email address after checking the current one (ID013, TID042)
     * and returns the account; the name, email, role and any active session are kept. The account is found by
     * email, not by name, since two accounts can have the same name. Fails if there is no account for the email.
     * A wrong current password gives CURRENT_PASSWORD_INCORRECT_MESSAGE and the password is kept.
     * The new password follows the same rules as a new account's password.
     */
    public Account changePassword(String email, String currentPassword, String newPassword) {
        Account account = findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account for " + email));
        if (!account.passwordMatches(currentPassword)) {
            throw new InvalidAccountException(InvalidAccountException.CURRENT_PASSWORD_INCORRECT_MESSAGE);
        }
        account.changePassword(newPassword);
        return account;
    }

    /**
     * Sets this user's skill level for a sport (ID014, TID049), or changes it if the sport already has one, and
     * returns the account; the levels of the other sports are kept. The sport and level names are matched in any case
     * and with surrounding spaces. Fails if the user has no account.
     * A sport that is not offered gives SportNotOfferedException ("Sport not offered") and a level that is not offered
     * gives SKILL_LEVEL_NOT_OFFERED_MESSAGE (TID050); either way the skill levels are unchanged.
     */
    public Account setSkillLevel(User user, String sportName, String levelName) {
        Account account = findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("No account for " + user));
        Sport sport = Sport.fromName(sportName);
        SkillLevel level;
        try {
            level = SkillLevel.fromName(levelName);
        } catch (IllegalArgumentException e) {
            throw new InvalidAccountException(InvalidAccountException.SKILL_LEVEL_NOT_OFFERED_MESSAGE);
        }
        account.setSkillLevel(sport, level);
        return account;
    }

    /** All accounts, in the order they were added. */
    public List<Account> getAccounts() {
        return List.copyOf(accountsByEmail.values());
    }

    /** Adds a password reset link (ID011), e.g. the links in a feature file's Background. Tokens must be unique. */
    public void addResetLink(PasswordResetLink link) {
        Objects.requireNonNull(link, "Reset link is required");
        if (resetLinksByToken.putIfAbsent(link.getToken(), link) != null) {
            throw new IllegalArgumentException("A reset link already exists with this token");
        }
    }

    /**
     * Requests a password reset link (ID011, TID024). For an email with an account, in any case and with surrounding
     * spaces, a new link that expires RESET_LINK_VALIDITY from now is added and returned (it would be emailed).
     * An unknown email gets no link but the caller issues the same RESET_LINK_SENT_MESSAGE, so the response does not
     * reveal which addresses have an account.
     */
    public Optional<PasswordResetLink> requestResetLink(String email) {
        return findByEmail(email).map(account -> {
            PasswordResetLink link = new PasswordResetLink(
                    UUID.randomUUID().toString(), account, currentDateTime().plus(RESET_LINK_VALIDITY));
            addResetLink(link);
            return link;
        });
    }

    /** The reset link with this token; empty if there is none. */
    public Optional<PasswordResetLink> findResetLink(String token) {
        return Optional.ofNullable(token == null ? null : resetLinksByToken.get(token));
    }

    /**
     * Sets a new password with a password reset link (ID011) and marks the link Used. Returns the account.
     * An unknown, expired or already used link gives INVALID_RESET_LINK_MESSAGE, and nothing changes.
     */
    public Account resetPassword(String token, String newPassword) {
        Objects.requireNonNull(newPassword, "Password is required");
        if (newPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        PasswordResetLink link = findResetLink(token)
                .orElseThrow(() -> new InvalidAccountException(InvalidAccountException.INVALID_RESET_LINK_MESSAGE));
        if (link.isUsed() || link.isExpiredAt(currentDateTime())) {
            throw new InvalidAccountException(InvalidAccountException.INVALID_RESET_LINK_MESSAGE);
        }
        Account account = link.getAccount();
        account.changePassword(newPassword);
        link.markUsed();
        return account;
    }

    /** The current date and time, used to check whether a reset link has expired (ID011). */
    public LocalDateTime currentDateTime() {
        return LocalDateTime.now(clock);
    }

    /** Test hook: fixes the current date and time, so scenarios about expired links do not depend on the real clock. */
    public void setCurrentDateTime(LocalDateTime dateTime) {
        clock = Clock.fixed(dateTime.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    }
}
