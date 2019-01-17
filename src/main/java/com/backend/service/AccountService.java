package com.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.backend.command.AddAccountCommand;
import com.backend.model.Account;

public interface AccountService {
	Optional<Account> addAccount(Long idCustomer, AddAccountCommand addAccountCommand);
	boolean deleteAccount(Long idCustomer, Long idAccount);
	Optional<Account> getCustomerAccount(Long idCustomer, Long idAccount);
	List<Account> getAllCustomerAccounts(Long idCustomer);
	Optional<BigDecimal> getBalanceOfCustomerAccount(Long idCustomer, Long idAccount);
}
