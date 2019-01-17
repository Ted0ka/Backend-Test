package com.backend.command;

import java.math.BigDecimal;

public class TransferCommand {
	private Long sourceAccountNo;
	private Long destinationAccountNo;
	private BigDecimal amount;
	private String description;

	public TransferCommand(Long sourceAccountNo, Long destinationAccountNo, BigDecimal amount, String description) {
		super();
		this.sourceAccountNo = sourceAccountNo;
		this.destinationAccountNo = destinationAccountNo;
		this.amount = amount;
		this.description = description;
	}

	public Long getSourceAccountNo() {
		return sourceAccountNo;
	}

	public void setSourceAccountNo(Long from) {
		this.sourceAccountNo = from;
	}

	public long getDestinationAccountNo() {
		return destinationAccountNo;
	}

	public void setDestinationAccountNo(Long to) {
		this.destinationAccountNo = to;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((destinationAccountNo == null) ? 0 : destinationAccountNo.hashCode());
		result = prime * result + ((sourceAccountNo == null) ? 0 : sourceAccountNo.hashCode());
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
		TransferCommand other = (TransferCommand) obj;
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
		if (sourceAccountNo == null) {
			if (other.sourceAccountNo != null) {
				return false;
			}
		} else if (!sourceAccountNo.equals(other.sourceAccountNo)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "TransferCommand [sourceAccountNo=" + sourceAccountNo + ", destinationAccountNo=" + destinationAccountNo
				+ ", amount=" + amount + ", description=" + description + "]";
	}

}
