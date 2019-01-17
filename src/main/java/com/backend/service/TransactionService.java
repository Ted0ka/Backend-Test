package com.backend.service;

import com.backend.command.DepositCommand;
import com.backend.command.TransferCommand;

public interface TransactionService {
	boolean transfer(TransferCommand dransferRequest);
	boolean deposit(DepositCommand incomRequest);
}
