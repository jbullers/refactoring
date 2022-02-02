package refactor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ArgsException extends Exception {

    public ArgsException(String message) {
        super(message);
    }

    public ArgsException(String message, Throwable cause) {
        super(message, cause);
    }
}
