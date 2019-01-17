package com.backend.module;

import com.backend.validator.*;
import com.google.inject.AbstractModule;

public class ValidatorModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(AddAccountValidator.class).to(AddAccountValidatorImpl.class);
		bind(AddCustomerValidator.class).to(AddCustomerValidatorImpl.class);
		bind(UpdateCustomerValidator.class).to(UpdateCustomerValidatorImpl.class);
		bind(DepositCommandValidator.class).to(DepositCommandValidatorImpl.class);
		bind(TransferCommandValidator.class).to(TransferCommandValidatorImpl.class);
	}
}
