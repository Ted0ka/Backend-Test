package com.backend.datacreator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.backend.Application;
import com.backend.command.AddAccountCommand;
import com.backend.command.AddCustomerCommand;
import com.backend.command.DepositCommand;
import com.backend.dao.CustomerDao;
import com.backend.model.Account;
import com.backend.model.Customer;
import com.backend.model.Operation;
import com.google.inject.Inject;

public class DataCreator {
	
	private static final Logger logger = Logger.getLogger(Application.class.getName());

	private CustomerDao customerDao;
	
	@Inject
	public DataCreator(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}
	
	public void createDataAndPersist() {
		logger.info("Persist in database");

		for (Customer customer : createData()) {
			customerDao.persist(customer);
		}
	}

	public static List<Customer> createData() {

		logger.info("Create data");
		AddCustomerCommand addCustomerCommand1 = new AddCustomerCommand("John", "Smith", "Baker St", "London", "31-67",
				"587-456-340", new AddAccountCommand("Personal account", new BigDecimal(2000)));
		Customer customer1 = new Customer(addCustomerCommand1);
		Account account1 = new Account(addCustomerCommand1.getAddAccountCommand());
		account1.addOperation(new Operation(new DepositCommand(1L, new BigDecimal(2000), "First deposit")));
		customer1.addAccount(account1);

		AddCustomerCommand addCustomerCommand2 = new AddCustomerCommand("Rupert", "Bean", "Victoria St", "Bristol",
				"45-54", "687-593-447", new AddAccountCommand("Personal account", new BigDecimal(1000)));
		Customer customer2 = new Customer(addCustomerCommand2);
		Account account2 = new Account(addCustomerCommand2.getAddAccountCommand());
		account2.addOperation(new Operation(new DepositCommand(2L, new BigDecimal(1000), "First deposit")));
		customer2.addAccount(account2);
		Account account3 = new Account(new AddAccountCommand("Savings account", new BigDecimal(5000)));
		account3.addOperation(new Operation(new DepositCommand(3L, new BigDecimal(5000), "My savings")));
		customer2.addAccount(account3);

		return Arrays.asList(customer1, customer2);
	}
}
