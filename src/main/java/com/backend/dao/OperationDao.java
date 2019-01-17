package com.backend.dao;

import java.util.List;
import java.util.Optional;
import com.backend.model.Operation;

public interface OperationDao extends Dao<Operation, Long> {
	List<Operation> findAllByCustomerIdAndAccountId(Long idCustomer, Long idAccount);
	Optional<Operation> findByCustomerIdAndAccountIdAndOperationid(Long idCustomer, Long idAccount, Long idOperation);
}
