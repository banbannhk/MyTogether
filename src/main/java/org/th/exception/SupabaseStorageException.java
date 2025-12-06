package org.th.exception;

/**
 * Custom exception for Supabase Storage operations
 */
public class SupabaseStorageException extends RuntimeException {

    public SupabaseStorageException(String message) {
        super(message);
    }

    public SupabaseStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
