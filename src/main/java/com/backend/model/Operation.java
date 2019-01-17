package com.backend.model;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.backend.command.DepositCommand;
import com.backend.command.TransferCommand;
import com.backend.service.TransactionType;
import com.google.gson.annotations.Expose;

@Entity
public class Operation {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Expose
	private Long id;
	@Expose
	private BigDecimal amount;
	@Expose
	private Long sourceAccountNo;
	@Expose
	private Long destinationAccountNo;
	@Expose
	private String description;
	@Expose
	@Enumerated(EnumType.STRING)
	TransactionType transactionType;
	@JoinColumn(name = "account_id"/* , nullable=false */)
	@ManyToOne
	private Account account;

	Operation() {

	}

	public Operation(DepositCommand depositCommand) {
		super();
		this.amount = depositCommand.getAmount();
		this.destinationAccountNo = depositCommand.getDestinationAccountNo();
		this.description = depositCommand.getDescription();
		this.transactionType = TransactionType.DEPOSIT;
	}

	public Operation(TransferCommand transferCommand, TransactionType transactionType) {
		super();
		this.amount = transferCommand.getAmount();
		this.sourceAccountNo = transferCommand.getSourceAccountNo();
		this.destinationAccountNo = transferCommand.getDestinationAccountNo();
		this.description = transferCommand.getDescription();
		this.transactionType = transactionType;
	}

	public Long getId() {
		return id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Long getSourceAccountNo() {
		return sourceAccountNo;
	}

	public void setSourceAccountNo(Long sourceAccountNo) {
		this.sourceAccountNo = sourceAccountNo;
	}

	public Long getDestinationAccountNo() {
		return destinationAccountNo;
	}

	public void setDestinationAccountNo(Long destinationAccountNo) {
		this.destinationAccountNo = destinationAccountNo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((destinationAccountNo == null) ? 0 : destinationAccountNo.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((sourceAccountNo == null) ? 0 : sourceAccountNo.hashCode());
		result = prime * result + ((transactionType == null) ? 0 : transactionType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Operation other = (Operation) obj;
		if (amount == null) {
			if (other.amount != null) {
				return false;
			}
		} else if (!amount.equals(other.amount)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (destinationAccountNo == null) {
			if (other.destinationAccountNo != null) {
				return false;
			}
		} else if (!destinationAccountNo.equals(other.destinationAccountNo)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (sourceAccountNo == null) {
			if (other.sourceAccountNo != null) {
				return false;
			}
		} else if (!sourceAccountNo.equals(other.sourceAccountNo)) {
			return false;
		}
		if (transactionType != other.transactionType) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Operation [id=" + id + ", amount=" + amount + ", sourceAccountNo=" + sourceAccountNo
				+ ", destinationAccountNo=" + destinationAccountNo + ", description=" + description
				+ ", transactionType=" + transactionType + "]";
	}
}
