package gargoyle.rpycg.ex;

public final class CodeGenerationException extends AppException {
    public CodeGenerationException(String message) {
        super(message);
    }

    public CodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
