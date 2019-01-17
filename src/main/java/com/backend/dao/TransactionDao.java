package com.backend.dao;

import com.backend.command.DepositCommand;
import com.backend.command.TransferCommand;

public interface TransactionDao {
	boolean deposit(DepositCommand depositCommand);
	boolean transfer(TransferCommand transferCommand);
}
