package com.backend.module;

import com.backend.service.*;
import com.google.inject.AbstractModule;

public class ServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(AccountService.class).to(AccountServiceImpl.class);
		bind(CustomerService.class).to(CustomerServiceImpl.class);
		bind(OperationService.class).to(OperationServiceImpl.class);
		bind(TransactionService.class).to(TransactionServiceImpl.class);
	}
}
