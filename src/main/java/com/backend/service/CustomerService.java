package com.backend.service;


import java.util.List;
import java.util.Optional;

import com.backend.command.AddCustomerCommand;
import com.backend.command.UpdateCustomerCommand;
import com.backend.model.Customer;

public interface CustomerService {
	Optional<Customer> addCustomer(AddCustomerCommand addCustomerRequest);
	Optional<Customer> getCustomer(Long idCustomer);
	List<Customer> getAllCustomers();
	boolean deleteCustomer(Long idCustomer);
	Optional<Customer> updateCustomer(Long idCustomer, UpdateCustomerCommand updateCustomerCommand);
}
