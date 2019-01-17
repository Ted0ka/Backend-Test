package com.backend.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.backend.model.Account;

public interface AccountDao extends Dao<Account, Long> {
	Optional<Account> findByCustomerIdAndAccountId(Long idCustomer, Long idAccount);
	List<Account> findAllByCustomerId(Long idCustomer);
	Optional<BigDecimal> findBalanceByCustomerIdAndAccountId(Long idCustomer, Long idAccount);
}
