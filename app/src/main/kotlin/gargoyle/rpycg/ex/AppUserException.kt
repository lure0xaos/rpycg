package gargoyle.rpycg.ex

import gargoyle.fx.FxUtil.format

class AppUserException : AppException {
    constructor(code: String, args: Map<String, Any> = mapOf()) : super(format(code, args))
    constructor(cause: Throwable, code: String, args: Map<String, Any> = mapOf()) : super(format(code, args), cause)
}
