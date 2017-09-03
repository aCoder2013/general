package com.song.middleware.server.storage.exception

/**
 * wrap all the exception raised by the storage layer.

 * Created by song on 2017/8/5.
 */
class StorageException : RuntimeException {

    constructor()

    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super(cause)

    constructor(message: String, cause: Throwable) : super(message, cause)

}
