package com.backend.service;

import java.util.List;
import java.util.Optional;

import com.backend.model.Operation;

public interface OperationService {
	List<Operation> getAllCustomerOperations(Long idCustomer, Long idAccount);
	Optional<Operation> getCustomerOperation(Long idCustomer, Long idAccount, Long idOperation);
}
