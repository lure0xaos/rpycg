package gargoyle.rpycg.ex;

public final class CodeGenerationException extends AppException {
    private static final long serialVersionUID = 7805145984385110745L;

    public CodeGenerationException(final String message) {
        super(message);
    }

    public CodeGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
