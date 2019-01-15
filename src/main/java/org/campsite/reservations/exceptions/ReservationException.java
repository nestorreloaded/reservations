package org.campsite.reservations.exceptions;

public class ReservationException extends Exception {

	/**
	 * Serial UID for the object
	 */
	private static final long serialVersionUID = -6014351064383507756L;
	private String message;

	public ReservationException(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
