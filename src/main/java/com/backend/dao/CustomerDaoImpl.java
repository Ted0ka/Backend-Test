package com.backend.dao;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.backend.model.Customer;
import com.google.inject.Inject;

public class CustomerDaoImpl extends CrudDao<Customer, Long>implements CustomerDao {

	@Inject
	public CustomerDaoImpl(EntityManagerFactory entityManagerFactory) {
		super(entityManagerFactory, Customer.class);
	}

	@Override
	public Optional<Customer> findByAccountId(Long idAccount) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		TypedQuery<Customer> query = entityManager.createQuery(
				"SELECT c FROM Customer c WHERE c.accountList.id = :idAccount", Customer.class);

		query.setParameter("idAccount", idAccount);

		Optional<Customer> customeropt = Optional.ofNullable(query.getSingleResult());

		entityManager.close();

		return customeropt;
	}
}
