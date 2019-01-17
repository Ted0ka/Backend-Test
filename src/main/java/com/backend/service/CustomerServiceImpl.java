package com.backend.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import com.backend.command.AddCustomerCommand;
import com.backend.command.UpdateCustomerCommand;
import com.backend.dao.CustomerDao;
import com.backend.model.Account;
import com.backend.model.Customer;
import com.backend.validator.*;
import com.google.inject.Inject;

public class CustomerServiceImpl implements CustomerService {

	private CustomerDao customerDao;

	private AddCustomerValidator addCustomerValidator;
	private AddAccountValidator addAccountValidator;
	private UpdateCustomerValidator updateCustomerCommandValidator;

	@Inject
	public CustomerServiceImpl(AddCustomerValidator addCustomerValidator, AddAccountValidator addAccountValidator,
			UpdateCustomerValidator updateCustomerCommandValidator, CustomerDao customerDao) {
		super();
		this.customerDao = customerDao;

		this.addCustomerValidator = addCustomerValidator;
		this.addAccountValidator = addAccountValidator;
		this.updateCustomerCommandValidator = updateCustomerCommandValidator;
	}

	@Override
	public Optional<Customer> addCustomer(AddCustomerCommand addCustomerCommand) {
		if (addCustomerValidator.validate(addCustomerCommand)
				&& addAccountValidator.validate(addCustomerCommand.getAddAccountCommand())) {

			Customer customerToAdd = new Customer(addCustomerCommand);
			customerToAdd.addAccount(new Account(addCustomerCommand.getAddAccountCommand()));

			customerDao.persist(customerToAdd);

			return Optional.ofNullable(customerToAdd);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Customer> getCustomer(Long idCustomer) {
		return customerDao.find(idCustomer);
	}

	@Override
	public List<Customer> getAllCustomers() {
		return customerDao.findAll();
	}

	@Override
	public boolean deleteCustomer(Long idCustomer) {
		return customerDao.delete(idCustomer);
	}

	@Override
	public Optional<Customer> updateCustomer(Long idCustomer, UpdateCustomerCommand updateCustomerCommand) {
		if (updateCustomerCommandValidator.validate(updateCustomerCommand)) {
			Optional<Customer> customerOpt = customerDao.find(idCustomer);

			if (customerOpt.isPresent()) {
				Customer customer = customerOpt.get();

				if (!StringUtils.isEmpty(updateCustomerCommand.getStreet())) {
					customer.setStreet(updateCustomerCommand.getStreet());
				}
				if (!StringUtils.isEmpty(updateCustomerCommand.getCity())) {
					customer.setCity(updateCustomerCommand.getCity());
				}
				if (!StringUtils.isEmpty(updateCustomerCommand.getZip())) {
					customer.setZip(updateCustomerCommand.getZip());
				}
				if (!StringUtils.isEmpty(updateCustomerCommand.getPhone())) {
					customer.setPhone(updateCustomerCommand.getPhone());
				}

				customerDao.update(customer.getId(), customer);

				return Optional.ofNullable(customer);
			}
			return Optional.empty();
		}
		return Optional.empty();
	}
}
