package gargoyle.rpycg.ex

class MalformedScriptException : AppException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
