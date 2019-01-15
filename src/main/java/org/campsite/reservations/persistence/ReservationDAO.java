package org.campsite.reservations.persistence;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.campsite.reservations.controller.ReservationRequest;
import org.campsite.reservations.exceptions.ReservationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class implements all the methods for retrieving and persisting
 * reservations on the service
 * 
 * @author nestor
 *
 */
@Repository
public class ReservationDAO extends BaseDAO {

	public List<Reservation> getByRange(Date startDate, Date endDate) {
		CriteriaBuilder builder = getCriteriaBuilder();

		CriteriaQuery<Reservation> query = builder.createQuery(Reservation.class);

		Root<Reservation> root = query.from(Reservation.class);

		Predicate start = builder.greaterThanOrEqualTo(root.get("startDate"), startDate);
		Predicate end = builder.lessThanOrEqualTo(root.get("endDate"), endDate);
		Predicate canceled = builder.equal(root.get("canceled"), Boolean.FALSE);
		Predicate and = builder.and(start, end, canceled);

		query.select(root).where(and);

		return entityManager.createQuery(query).getResultList();

	}

	@Transactional
	public String createReservation(ReservationRequest request) throws ReservationException {

		validateRequest(request);

		Reservation reservation = new Reservation();
		reservation.setCanceled(Boolean.FALSE);
		reservation.setStartDate(request.getStartDate());
		reservation.setEndDate(request.getEndDate());
		reservation.setUserEmail(request.getUserEmail());
		reservation.setUserFullName(request.getUserFullName());
		reservation.setUniqueIdentifier(UUID.randomUUID().toString());

		entityManager.persist(reservation);

		return reservation.getUniqueIdentifier();
	}

	private void validateRequest(ReservationRequest request) throws ReservationException {
		// First we must validate if there is no reservation in that time period
		List<Reservation> existingReservations = getByRange(request.getStartDate(), request.getEndDate());
		if (existingReservations.size() > 0) {
			throw new ReservationException("Already reserved in that time period");
		}

		// We can't reserve more than 3 days
		long diff = request.getEndDate().getTime() - request.getStartDate().getTime();
		long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		if (days > 3) {
			throw new ReservationException("Cannot reserve for more than 3 days");
		}

	}

	@Transactional
	public boolean cancelReservation(String cancelId) throws ReservationException {
		// First we have to find the reservation
		CriteriaBuilder builder = getCriteriaBuilder();

		CriteriaQuery<Reservation> query = builder.createQuery(Reservation.class);

		Root<Reservation> root = query.from(Reservation.class);

		query.select(root).where(builder.equal(root.get("uniqueIdentifier"), cancelId));

		try {
			Reservation result = entityManager.createQuery(query).getSingleResult();

			if (result.isCanceled()) {
				throw new ReservationException("Reservation already canceled");
			}

		} catch (NoResultException ex) {
			throw new ReservationException("Reservation not exist");

		}

		// Update the reservation
		CriteriaUpdate<Reservation> update = builder.createCriteriaUpdate(Reservation.class);

		root = update.from(Reservation.class);

		update.set(root.get("canceled"), Boolean.TRUE).where(builder.equal(root.get("uniqueIdentifier"), cancelId));

		return entityManager.createQuery(update).executeUpdate() > 0;

	}

}
