package com.backend.dao;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import com.backend.model.Operation;
import com.google.inject.Inject;

public class OperationDaoImpl extends CrudDao<Operation, Long>implements OperationDao {

	@Inject
	public OperationDaoImpl(EntityManagerFactory entityManagerFactory) {
		super(entityManagerFactory, Operation.class);
	}

	@Override
	public List<Operation> findAllByCustomerIdAndAccountId(Long idCustomer, Long idAccount) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		TypedQuery<Operation> query = entityManager.createQuery(
				"SELECT o FROM Operation o  WHERE o.account.id = :idAccount AND o.account.customer.id = :idCustomer",
				Operation.class);

		query.setParameter("idCustomer", idCustomer);
		query.setParameter("idAccount", idAccount);

		List<Operation> operationList = query.getResultList();
		
		entityManager.close();

		return operationList;
	}

	@Override
	public Optional<Operation> findByCustomerIdAndAccountIdAndOperationid(Long idCustomer, Long idAccount, Long idOperation) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		TypedQuery<Operation> query = entityManager.createQuery(
				"SELECT o FROM Operation o WHERE o.account.id = :idAccount AND o.account.customer.id = :idCustomer AND id = :idOperation",
				Operation.class);

		query.setParameter("idCustomer", idCustomer);
		query.setParameter("idAccount", idAccount);
		query.setParameter("idOperation", idOperation);
//NoResultException
		Optional<Operation> operationOpt = Optional.ofNullable(query.getSingleResult());
		
		entityManager.close();

		return operationOpt;
	}
}
