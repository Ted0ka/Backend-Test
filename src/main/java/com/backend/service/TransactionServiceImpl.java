package com.backend.service;

import com.backend.command.DepositCommand;
import com.backend.command.TransferCommand;
import com.backend.dao.TransactionDao;
import com.backend.validator.DepositCommandValidator;
import com.backend.validator.TransferCommandValidator;
import com.google.inject.Inject;

public class TransactionServiceImpl implements TransactionService {

	private TransactionDao transactionDao;

	private DepositCommandValidator depositCommandValidator;
	private TransferCommandValidator transferCommandValidator;

	@Inject
	public TransactionServiceImpl(DepositCommandValidator depositCommandValidator,
			TransferCommandValidator transferCommandValidator,
			TransactionDao transactionDao/* EntityManagerFactory entityManagerFactory */) {
		super();
		this.transactionDao = transactionDao;
		this.depositCommandValidator = depositCommandValidator;
		this.transferCommandValidator = transferCommandValidator;
	}

	@Override
	public boolean transfer(TransferCommand transferCommand) {
		if (transferCommandValidator.validate(transferCommand)) {
			return transactionDao.transfer(transferCommand);
		}
		return false;
	}

	@Override
	public boolean deposit(DepositCommand depositCommand) {
		if (depositCommandValidator.validate(depositCommand)) {
			return transactionDao.deposit(depositCommand);
		}
		return false;
	}
}
