package com.backend.validator;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import com.backend.command.DepositCommand;

public class DepositCommandValidatorImpl implements DepositCommandValidator {

	@Override
	public boolean validate(DepositCommand depositCommand) {
		return ((depositCommand.getAmount().compareTo(new BigDecimal(0)) > 0) && !StringUtils.isEmpty(depositCommand.getDescription()));
	}

}
