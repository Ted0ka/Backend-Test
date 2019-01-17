package com.backend.dao;

import java.util.Optional;

import com.backend.model.Customer;

public interface CustomerDao extends Dao<Customer, Long> {
	Optional<Customer> findByAccountId(Long idAccount);
}
