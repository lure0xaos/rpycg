package gargoyle.fx

class FxUserException : FxException {
    constructor(code: String, args: Map<String, Any> = mapOf<String, String>()) :
            super(FxUtil.format(code, args))

    constructor(cause: Throwable?, code: String, args: Map<String, Any> = mapOf<String, String>()) :
            super(FxUtil.format(code, args), cause)

}
