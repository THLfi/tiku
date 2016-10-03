package fi.thl.pivot.exception;

import fi.thl.pivot.datasource.HydraSource;

public class UserNotAuthenticatedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final HydraSource source;
    private final boolean authenticationFailed;

    public UserNotAuthenticatedException(HydraSource source) {
        this(source, false);
    }

    public UserNotAuthenticatedException(HydraSource source,
            boolean authenticationFailed) {
        this.source = source;
        this.authenticationFailed = authenticationFailed;
    }

    public HydraSource getSource() {
        return source;
    }

    public boolean isAuthenticationFailed() {
        return authenticationFailed;
    }

}
