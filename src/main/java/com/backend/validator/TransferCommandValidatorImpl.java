package com.backend.validator;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import com.backend.command.TransferCommand;

public class TransferCommandValidatorImpl implements TransferCommandValidator {

	@Override
	public boolean validate(TransferCommand transferCommand) {
		return ((transferCommand.getAmount().compareTo(new BigDecimal(0)) > 0)
				&& !StringUtils.isEmpty(transferCommand.getDescription())
				&& (transferCommand.getSourceAccountNo() != transferCommand.getDestinationAccountNo()));
	}

}
