package com.backend.validator;

import org.apache.commons.lang.StringUtils;

import com.backend.command.UpdateCustomerCommand;

public class UpdateCustomerValidatorImpl implements UpdateCustomerValidator {

	@Override
	public boolean validate(UpdateCustomerCommand updateCustomerCommand) {
		return (!StringUtils.isEmpty(updateCustomerCommand.getStreet())
				|| !StringUtils.isEmpty(updateCustomerCommand.getCity())
				|| !StringUtils.isEmpty(updateCustomerCommand.getZip())
				|| !StringUtils.isEmpty(updateCustomerCommand.getPhone()));
	}
}
