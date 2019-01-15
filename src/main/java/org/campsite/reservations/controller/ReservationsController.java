package org.campsite.reservations.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.campsite.reservations.exceptions.ReservationException;
import org.campsite.reservations.persistence.Reservation;
import org.campsite.reservations.persistence.ReservationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle all the petitions to the service
 * 
 * @author nestor
 *
 */

@RestController
public class ReservationsController {

	@Autowired
	private ReservationDAO reservationDAO;

	@GetMapping("/availability")
	public List<Reservation> getReservations(@RequestParam(value = "startDate", required = false) Date startDate,
			@RequestParam(value = "startDate", required = false) Date endDate) {

		// Handle date ranges
		handleDates(startDate, endDate);

		return reservationDAO.getByRange(startDate, endDate);
	}

	@PutMapping("/createReservation")
	public String createReservation(@RequestBody ReservationRequest request) {
		String result;
		try {
			result = reservationDAO.createReservation(request);
		} catch (ReservationException exception) {
			result = "Can't create reservation";
		}

		return result;
	}

	@PostMapping("/cancelReservation")
	public boolean cancelReservation(@RequestParam(value = "cancelId", required = true) String cancelId) {
		try {
			return reservationDAO.cancelReservation(cancelId);
		} catch (ReservationException ex) {
			return false;
		}
	}

	/**
	 * Util method to handle the search range
	 * 
	 * @param start
	 * @param end
	 */
	private void handleDates(Date startDate, Date endDate) {

		Calendar calendar = Calendar.getInstance();
		if (startDate == null && endDate == null) {
			startDate = calendar.getTime();
			calendar.add(Calendar.MONTH, 1);
			endDate = calendar.getTime();
			return;
		}

		if (startDate == null) {
			calendar.setTime(endDate);
			calendar.add(Calendar.MONTH, -1);
			startDate = calendar.getTime();
			return;
		}

		if (endDate == null) {
			calendar.setTime(startDate);
			calendar.add(1, Calendar.MONTH);
			endDate = calendar.getTime();
			return;
		}

	}
}
