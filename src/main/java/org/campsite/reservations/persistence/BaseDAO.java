package org.campsite.reservations.persistence;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseDAO {

	@Autowired
	protected EntityManager entityManager;
	
	protected CriteriaBuilder getCriteriaBuilder() {
		return entityManager.getCriteriaBuilder();
	}
}

