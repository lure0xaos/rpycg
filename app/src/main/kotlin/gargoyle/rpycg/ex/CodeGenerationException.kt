package gargoyle.rpycg.ex

@Suppress("unused")
class CodeGenerationException : AppException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
