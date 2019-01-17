package com.backend.dao;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.backend.command.DepositCommand;
import com.backend.command.TransferCommand;
import com.backend.model.Account;
import com.backend.model.Operation;
import com.backend.service.TransactionType;
import com.google.inject.Inject;

public class TransactionDaoImpl implements TransactionDao {

	private EntityManagerFactory entityManagerFactory;

	@Inject
	public TransactionDaoImpl(EntityManagerFactory entityManagerFactory) {
		super();
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public boolean deposit(final DepositCommand depositCommand) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		Optional<Account> destAccountOpt = Optional
				.ofNullable(entityManager.find(Account.class, depositCommand.getDestinationAccountNo()));

		if (destAccountOpt.isPresent()) {

			Account destAccount = destAccountOpt.get();

			destAccount.setBalance(destAccount.getBalance().add(depositCommand.getAmount()));
			destAccount.addOperation(new Operation(depositCommand));

			entityManager.getTransaction().commit();
			return true;
		}
		entityManager.getTransaction().commit();
		return false;
	}

	@Override
	public boolean transfer(final TransferCommand transferCommand) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		Optional<Account> srcAccountOpt = Optional
				.ofNullable(entityManager.find(Account.class, transferCommand.getSourceAccountNo()));
		Optional<Account> destnAccountOpt = Optional
				.ofNullable(entityManager.find(Account.class, transferCommand.getDestinationAccountNo()));

		if (srcAccountOpt.isPresent() && destnAccountOpt.isPresent()) {
			Account srcAccount = srcAccountOpt.get();
			Account destAccount = destnAccountOpt.get();

			if (srcAccount.getBalance().compareTo(transferCommand.getAmount()) >= 0) {

				destAccount.setBalance(destAccount.getBalance().add(transferCommand.getAmount()));
				destAccount.addOperation(new Operation(transferCommand, TransactionType.DEPOSIT));

				srcAccount.setBalance(srcAccount.getBalance().subtract(transferCommand.getAmount()));
				srcAccount.addOperation(new Operation(transferCommand, TransactionType.WITHDRAW));

				entityManager.getTransaction().commit();
				return true;
			}
			entityManager.getTransaction().commit();
			return false;
		}
		entityManager.getTransaction().commit();
		return false;
	}
}
