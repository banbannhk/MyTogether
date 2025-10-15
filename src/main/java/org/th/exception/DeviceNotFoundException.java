package org.th.exception;

public class DeviceNotFoundException extends ResourceNotFoundException {
    public DeviceNotFoundException(String deviceId) {
        super("Device not found: " + deviceId);
    }
}
