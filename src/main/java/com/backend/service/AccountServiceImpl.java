package com.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.persistence.NoResultException;

import com.backend.command.AddAccountCommand;
import com.backend.dao.AccountDao;
import com.backend.dao.CustomerDao;
import com.backend.model.Account;
import com.backend.model.Customer;
import com.backend.validator.AddAccountValidator;
import com.google.inject.Inject;

public class AccountServiceImpl implements AccountService {

	private CustomerDao customerDao;
	private AccountDao accountDao;
	private AddAccountValidator addAccountValidator;

	@Inject
	public AccountServiceImpl(AddAccountValidator addAccountValidator, CustomerDao customerDao, AccountDao accountDao) {
		super();
		this.customerDao = customerDao;
		this.accountDao = accountDao;
		this.addAccountValidator = addAccountValidator;
	}

	@Override
	public Optional<Account> addAccount(Long idCustomer, AddAccountCommand addAccountCommand) {

		Optional<Customer> customerOpt = customerDao.find(idCustomer);

		if (customerOpt.isPresent()) {

			Customer customer = customerOpt.get();

			if (addAccountValidator.validate(addAccountCommand)) {
				Account accountToAdd = new Account(addAccountCommand);

				customer.addAccount(accountToAdd);

				customerDao.update(customer.getId(), customer);

				Optional<Customer> updatedCustomerOpt = customerDao.find(customer.getId());

				return Optional.ofNullable(updatedCustomerOpt.get().getAccountList()
						.get(updatedCustomerOpt.get().getAccountList().size() - 1));
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean deleteAccount(Long idCustomer, Long idAccount) {

		Optional<Customer> customerOpt = customerDao.find(idCustomer);

		if (customerOpt.isPresent()) {

			Customer customer = customerOpt.get();
			Optional<Account> accountOpt = customer.getAccountList().stream()
					.filter(account -> account.getId() == idAccount).findAny();

			if (accountOpt.isPresent()) {
				customer.removeAccount(accountOpt.get());
				customerDao.update(customer.getId(), customer);

				return true;
			}
			return false;
		}
		return false;
	}

	public Optional<Account> getCustomerAccount(Long idCustomer, Long idAccount) {
		try {
			return accountDao.findByCustomerIdAndAccountId(idCustomer, idAccount);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<Account> getAllCustomerAccounts(Long idCustomer) {
		return accountDao.findAllByCustomerId(idCustomer);
	}

	@Override
	public Optional<BigDecimal> getBalanceOfCustomerAccount(Long idCustomer, Long idAccount) {
		try {
			return accountDao.findBalanceByCustomerIdAndAccountId(idCustomer, idAccount);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}
}
