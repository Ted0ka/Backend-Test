package com.backend.validator;

import org.apache.commons.lang.StringUtils;

import com.backend.command.AddCustomerCommand;

public class AddCustomerValidatorImpl implements AddCustomerValidator {

	@Override
	public boolean validate(AddCustomerCommand addCustomerCommand) {
		return (!StringUtils.isEmpty(addCustomerCommand.getFirstName())
				&& !StringUtils.isEmpty(addCustomerCommand.getLastName())
				&& !StringUtils.isEmpty(addCustomerCommand.getStreet())
				&& !StringUtils.isEmpty(addCustomerCommand.getCity())
				&& !StringUtils.isEmpty(addCustomerCommand.getZip())
				&& !StringUtils.isEmpty(addCustomerCommand.getPhone())
				&& addCustomerCommand.getAddAccountCommand() != null);
	}
}
