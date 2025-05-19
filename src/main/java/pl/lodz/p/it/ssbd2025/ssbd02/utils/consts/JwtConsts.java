package pl.lodz.p.it.ssbd2025.ssbd02.utils.consts;

public class JwtConsts {
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_TYPE = "typ";
    public static final String CLAIM_NEW_EMAIL = "newEmail";
    public static final String CLAIM_OLD_EMAIL = "oldEmail";

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String TYPE_ACCESS_2FA = "access2fa";
    public static final String TYPE_EMAIL_CHANGE = "EMAIL_CHANGE";
    public static final String TYPE_EMAIL_REVERT = "EMAIL_REVERT";

    public static final String PRIVATE_KEY_PATH = "keys/private_key.pem";
    public static final String PUBLIC_KEY_PATH = "keys/public_key.pem";

    public static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
    public static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";
    public static final String BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----";
    public static final String END_PUBLIC_KEY = "-----END PUBLIC KEY-----";

    public static final String ALGORITHM = "RSA";

    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    public static final String ENVIRONMENT_PROD = "prod";
}
