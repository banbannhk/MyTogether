package org.th.exception;

public class InactiveDeviceException extends BusinessException {
    public InactiveDeviceException(String deviceId) {
        super("Device is inactive: " + deviceId);
    }
}
