package fi.thl.pivot.exception;

public class CubeAccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 755098219455250227L;

    public CubeAccessDeniedException(String env, String cube) {
        super(String.format("Access to cube %s in %s is denied", env, cube));
    }

}
