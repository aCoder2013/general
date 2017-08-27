package com.song.middleware.server.storage.exception;

/**
 * wrap all the exception raised by the storage layer.
 *
 * Created by song on 2017/8/5.
 */
public class StorageException extends RuntimeException {

    public StorageException() {
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

}
