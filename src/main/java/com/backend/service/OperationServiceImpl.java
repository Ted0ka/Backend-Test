package com.backend.service;

import java.util.List;
import java.util.Optional;

import javax.persistence.NoResultException;

import com.backend.dao.OperationDao;
import com.backend.model.Operation;
import com.google.inject.Inject;

public class OperationServiceImpl implements OperationService {

	private OperationDao operationDao;

	@Inject
	public OperationServiceImpl(OperationDao operationDao/* EntityManagerFactory entityManagerFactory */) {
		super();
		this.operationDao = operationDao;

	}

	@Override
	public List<Operation> getAllCustomerOperations(Long idCustomer, Long idAccount) {
		return operationDao.findAllByCustomerIdAndAccountId(idCustomer, idAccount);
	}

	@Override
	public Optional<Operation> getCustomerOperation(Long idCustomer, Long idAccount, Long idOperation) {
		try {
			return operationDao.findByCustomerIdAndAccountIdAndOperationid(idCustomer, idAccount, idOperation);
		} catch (NoResultException e) {
			return Optional.empty();
		}

	}
}
