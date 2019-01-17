package com.backend.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.backend.model.Account;
import com.google.inject.Inject;

public class AccountDaoImpl extends CrudDao<Account, Long>implements AccountDao {

	@Inject
	public AccountDaoImpl(EntityManagerFactory entityManagerFactory) {
		super(entityManagerFactory, Account.class);
	}

	@Override
	public Optional<Account> findByCustomerIdAndAccountId(Long idCustomer, Long idAccount) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		TypedQuery<Account> query = entityManager.createQuery(
				"SELECT a FROM Account a WHERE a.id = :idAccount AND a.customer.id = :idCustomer", Account.class);

		query.setParameter("idAccount", idAccount);
		query.setParameter("idCustomer", idCustomer);

		Optional<Account> accountOpt = Optional.ofNullable(query.getSingleResult());

		entityManager.close();

		return accountOpt;
	}

	@Override
	public List<Account> findAllByCustomerId(Long idCustomer) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		TypedQuery<Account> query = entityManager
				.createQuery("SELECT a FROM Account a WHERE a.customer.id = :idCustomer", Account.class);

		query.setParameter("idCustomer", idCustomer);

		List<Account> accountList = query.getResultList();

		entityManager.close();

		return accountList;
	}

	@Override
	public Optional<BigDecimal> findBalanceByCustomerIdAndAccountId(Long idCustomer, Long idAccount) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		TypedQuery<BigDecimal> query = entityManager.createQuery(
				"SELECT a.balance FROM Account a WHERE a.id = :idAccount AND a.customer.id = :idCustomer",
				BigDecimal.class);

		query.setParameter("idAccount", idAccount);
		query.setParameter("idCustomer", idCustomer);

		Optional<BigDecimal> balanceOpt = Optional.ofNullable(query.getSingleResult());

		entityManager.close();

		return balanceOpt;
	}
}
