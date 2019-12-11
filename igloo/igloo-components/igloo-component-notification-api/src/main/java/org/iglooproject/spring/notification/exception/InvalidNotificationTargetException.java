package org.iglooproject.spring.notification.exception;

public class InvalidNotificationTargetException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidNotificationTargetException(String msg) {
		super(msg);
	}

	public InvalidNotificationTargetException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
