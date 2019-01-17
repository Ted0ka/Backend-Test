package com.backend.command;

public class AddCustomerCommand {
	private String firstName;
	private String lastName;
	private String street;
	private String city;
	private String zip;
	private String phone;
	private AddAccountCommand addAccountCommand;

	public AddCustomerCommand(String firstName, String lastName, String street, String city, String zip, String phone,
			AddAccountCommand addAccountCommand) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.street = street;
		this.city = city;
		this.zip = zip;
		this.phone = phone;
		this.addAccountCommand = addAccountCommand;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public String getZip() {
		return zip;
	}

	public String getPhone() {
		return phone;
	}

	public AddAccountCommand getAddAccountCommand() {
		return addAccountCommand;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addAccountCommand == null) ? 0 : addAccountCommand.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((phone == null) ? 0 : phone.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((zip == null) ? 0 : zip.hashCode());
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
		AddCustomerCommand other = (AddCustomerCommand) obj;
		if (addAccountCommand == null) {
			if (other.addAccountCommand != null) {
				return false;
			}
		} else if (!addAccountCommand.equals(other.addAccountCommand)) {
			return false;
		}
		if (city == null) {
			if (other.city != null) {
				return false;
			}
		} else if (!city.equals(other.city)) {
			return false;
		}
		if (firstName == null) {
			if (other.firstName != null) {
				return false;
			}
		} else if (!firstName.equals(other.firstName)) {
			return false;
		}
		if (lastName == null) {
			if (other.lastName != null) {
				return false;
			}
		} else if (!lastName.equals(other.lastName)) {
			return false;
		}
		if (phone == null) {
			if (other.phone != null) {
				return false;
			}
		} else if (!phone.equals(other.phone)) {
			return false;
		}
		if (street == null) {
			if (other.street != null) {
				return false;
			}
		} else if (!street.equals(other.street)) {
			return false;
		}
		if (zip == null) {
			if (other.zip != null) {
				return false;
			}
		} else if (!zip.equals(other.zip)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AddCustomerCommand [firstName=" + firstName + ", lastName=" + lastName + ", street=" + street
				+ ", city=" + city + ", zip=" + zip + ", phone=" + phone + ", addAccountCommand=" + addAccountCommand
				+ "]";
	}
}
