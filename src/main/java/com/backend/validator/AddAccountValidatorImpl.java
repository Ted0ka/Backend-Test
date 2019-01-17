package com.backend.validator;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import com.backend.command.AddAccountCommand;

public class AddAccountValidatorImpl implements AddAccountValidator {

	@Override
	public boolean validate(AddAccountCommand addAccountCommand) {
		return (!StringUtils.isEmpty(addAccountCommand.getDescription()) && (addAccountCommand.getAmount() != null
				&& addAccountCommand.getAmount().compareTo(new BigDecimal(0)) >= 0));
	}
}
