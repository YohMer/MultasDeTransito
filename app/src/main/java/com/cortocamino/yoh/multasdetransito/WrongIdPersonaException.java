package com.cortocamino.yoh.multasdetransito;

/**
 * Created by yoh on 7/14/16.
 */
public class WrongIdPersonaException extends Exception {
    public WrongIdPersonaException() {
        super();
    }

    public WrongIdPersonaException(String message) {
        super(message);
    }

    public WrongIdPersonaException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongIdPersonaException(Throwable cause) {
        super(cause);
    }
}
