package gargoyle.rpycg.ex

class CodeGenerationException : AppException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
