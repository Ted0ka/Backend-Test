package com.backend;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assume;
import org.junit.Test;

import com.backend.command.AddAccountCommand;
import com.backend.command.AddCustomerCommand;
import com.backend.command.UpdateCustomerCommand;
import com.backend.datacreator.DataCreator;
import com.backend.model.Customer;
import com.backend.response.StatusResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class CustomerApiTest extends AbstractTest {

	@Test
	public void getCustomer_ValidCustomerId_ReturnCustomer() {
		startServer();

		int idCustomer = 1;

		Customer customerExpected = DataCreator.createData().get(0);

		Optional<HttpResponse> responseGetOpt = executeGetCustomer(idCustomer);
		Assume.assumeTrue(responseGetOpt.isPresent());
		HttpResponse responseGet = responseGetOpt.get();

		Optional<Customer> customerActualOpt = getCustomer(responseGet);
		Assume.assumeTrue(customerActualOpt.isPresent());
		Customer customerActual = customerActualOpt.get();

		assertEquals(HttpStatus.OK_200, responseGet.getStatusLine().getStatusCode());

		assertCustomerParamsEquals(customerExpected, customerActual);

		stopServer();
	}

	@Test
	public void getCustomer_InvalidCustomerId_Fail() {
		startServer();

		int idCustomer = 99;

		Optional<HttpResponse> responseGetOpt = executeGetCustomer(idCustomer);
		Assume.assumeTrue(responseGetOpt.isPresent());
		HttpResponse responseGet = responseGetOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responseGet);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Customer with id = " + idCustomer + " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, responseGet.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void createCustomer_AllFieldsValid_ReturnCreatedCustomer() {
		startServer();

		AddAccountCommand addAccountCommand = new AddAccountCommand("New account", new BigDecimal(1000));
		AddCustomerCommand addCustomerCommand = new AddCustomerCommand("John", "Doe", "Karmelicka", "Cracow", "12-123",
				"123-456-789", addAccountCommand);

		Optional<HttpResponse> responsePostOpt = executeCreateCustomer(addCustomerCommand);
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Customer> customerReturnFromPostOpt = getCustomer(responsePost);
		Assume.assumeTrue(customerReturnFromPostOpt.isPresent());
		Customer customerReturnFromPost = customerReturnFromPostOpt.get();

		Optional<HttpResponse> responseGetOpt = executeGetCustomer(
				Math.toIntExact(customerReturnFromPost.getId().longValue()));
		Assume.assumeTrue(responseGetOpt.isPresent());
		HttpResponse responseGet = responseGetOpt.get();

		Optional<Customer> customerGetFromDatabaseOpt = getCustomer(responseGet);
		Assume.assumeTrue(customerGetFromDatabaseOpt.isPresent());
		Customer customerGetFromDatabase = customerGetFromDatabaseOpt.get();

		assertEquals(HttpStatus.OK_200, responsePost.getStatusLine().getStatusCode());
		assertEquals(HttpStatus.OK_200, responseGet.getStatusLine().getStatusCode());

		assertCustomerParamsEquals(customerGetFromDatabase, customerReturnFromPost);

		stopServer();
	}

	@Test
	public void createCustomer_MissingPhoneField_Fail() {
		startServer();

		AddAccountCommand addAccountCommand = new AddAccountCommand("New account", new BigDecimal(1000));
		AddCustomerCommand addCustomerCommand = new AddCustomerCommand("John", "Doe", "Karmelicka", "Cracow", "12-123",
				"", addAccountCommand);

		Optional<HttpResponse> responsePostOpt = executeCreateCustomer(addCustomerCommand);
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void createCustomer_MissingAmountField_Fail() {
		startServer();

		AddAccountCommand addAccountCommand = new AddAccountCommand("New account", null);
		AddCustomerCommand addCustomerCommand = new AddCustomerCommand("John", "Doe", "Karmelicka", "Cracow", "12-123",
				"123-456-789", addAccountCommand);

		Optional<HttpResponse> responsePostOpt = executeCreateCustomer(addCustomerCommand);
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void createCustomer_MissingAddCustomerCommand_Fail() {
		startServer();

		AddCustomerCommand addCustomerCommand = new AddCustomerCommand("John", "Doe", "Karmelicka", "Cracow", "12-123",
				"123-456-789", null);

		Optional<HttpResponse> responsePostOpt = executeCreateCustomer(addCustomerCommand);
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void deleteCustomer_ValidCustomerId_ReturnOk() {
		startServer();

		int idCustomer = 1;

		Optional<HttpResponse> responseGetBeforeOpt = executeGetCustomer(idCustomer);
		Assume.assumeTrue(responseGetBeforeOpt.isPresent());
		HttpResponse responseGetBefore = responseGetBeforeOpt.get();

		Optional<HttpResponse> responseDeleteOpt = executeDeleteCustomer(idCustomer);
		Assume.assumeTrue(responseDeleteOpt.isPresent());
		HttpResponse responseDelete = responseDeleteOpt.get();

		Optional<HttpResponse> responseGetAfterOpt = executeGetCustomer(idCustomer);
		Assume.assumeTrue(responseGetAfterOpt.isPresent());
		HttpResponse responseGetAfter = responseGetAfterOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responseDelete);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.OK_200,
				"Customer with id = " + idCustomer + " deleted");

		assertEquals(HttpStatus.OK_200, responseGetBefore.getStatusLine().getStatusCode());
		assertEquals(HttpStatus.OK_200, responseDelete.getStatusLine().getStatusCode());
		assertEquals(HttpStatus.NOT_FOUND_404, responseGetAfter.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void deleteCustomer_InvalidCustomerId_Fail() {
		startServer();

		int idCustomer = 45;

		Optional<HttpResponse> responseDeleteOpt = executeDeleteCustomer(idCustomer);
		Assume.assumeTrue(responseDeleteOpt.isPresent());
		HttpResponse responseDelete = responseDeleteOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responseDelete);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Customer with id = " + idCustomer + " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, responseDelete.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void editCustomer_AllFieldsValid_ReturnCreatedCustomer() {
		startServer();

		int idCustomer = 1;

		UpdateCustomerCommand updateCustomerCommand = new UpdateCustomerCommand("New street", "New city", "New zip",
				"New phone");

		Optional<HttpResponse> responsePutOpt = executeUpdateCustomer(idCustomer, updateCustomerCommand);
		Assume.assumeTrue(responsePutOpt.isPresent());
		HttpResponse responsePut = responsePutOpt.get();

		Optional<Customer> customerOpt = getCustomer(responsePut);
		Assume.assumeTrue(customerOpt.isPresent());
		Customer customer = customerOpt.get();

		assertEquals(HttpStatus.OK_200, responsePut.getStatusLine().getStatusCode());
		assertEquals(updateCustomerCommand.getStreet(), customer.getStreet());
		assertEquals(updateCustomerCommand.getCity(), customer.getCity());
		assertEquals(updateCustomerCommand.getZip(), customer.getZip());

		Optional<HttpResponse> responseGetAfterOpt = executeGetCustomer(idCustomer);
		Assume.assumeTrue(responseGetAfterOpt.isPresent());
		HttpResponse responseGetAfter = responseGetAfterOpt.get();

		customerOpt = getCustomer(responseGetAfter);
		Assume.assumeTrue(customerOpt.isPresent());
		customer = customerOpt.get();

		assertEquals(HttpStatus.OK_200, responseGetAfter.getStatusLine().getStatusCode());
		assertEquals(updateCustomerCommand.getStreet(), customer.getStreet());
		assertEquals(updateCustomerCommand.getCity(), customer.getCity());
		assertEquals(updateCustomerCommand.getZip(), customer.getZip());

		stopServer();
	}

	@Test
	public void editCustomer_InvalidCustomerId_Fail() {
		startServer();

		int idCustomer = 99;

		UpdateCustomerCommand updateCustomerCommand = new UpdateCustomerCommand("New street", "New city", "New zip",
				"New phone");
		Optional<HttpResponse> responsePutOpt = executeUpdateCustomer(idCustomer, updateCustomerCommand);
		Assume.assumeTrue(responsePutOpt.isPresent());
		HttpResponse responsePut = responsePutOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePut);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Customer with id = " + idCustomer + " not exist or data is invalid");

		assertEquals(HttpStatus.NOT_FOUND_404, responsePut.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void editCustomer_MissingAllUpdateCustomerCommandFields_Fail() {
		startServer();

		int idCustomer = 1;

		UpdateCustomerCommand updateCustomerCommand = new UpdateCustomerCommand(null, null, null, null);

		Optional<HttpResponse> responsePutOpt = executeUpdateCustomer(idCustomer, updateCustomerCommand);
		Assume.assumeTrue(responsePutOpt.isPresent());
		HttpResponse responsePut = responsePutOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePut);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Customer with id = " + idCustomer + " not exist or data is invalid");

		assertEquals(HttpStatus.NOT_FOUND_404, responsePut.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	private Optional<HttpResponse> executeGetCustomer(int idCustomer) {
		try {
			HttpClient client = HttpClients.createDefault();

			HttpGet httpGet = new HttpGet("http://localhost:4567/api/v1/customers/" + idCustomer);

			return Optional.ofNullable(client.execute(httpGet));

		} catch (IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	private Optional<HttpResponse> executeCreateCustomer(AddCustomerCommand addCustomerCommand) {
		try {
			CloseableHttpClient client = HttpClients.createDefault();

			HttpPost httpPost = new HttpPost("http://localhost:4567/api/v1/customers");

			StringEntity addCustomerCmd = new StringEntity(new Gson().toJson(addCustomerCommand));

			httpPost.setEntity(addCustomerCmd);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			return Optional.ofNullable(client.execute(httpPost));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	private Optional<HttpResponse> executeDeleteCustomer(int idCustomer) {
		try {
			HttpClient client = HttpClients.createDefault();

			HttpDelete httpDelete = new HttpDelete("http://localhost:4567/api/v1/customers/" + idCustomer);

			return Optional.ofNullable(client.execute(httpDelete));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	private Optional<HttpResponse> executeUpdateCustomer(int idCustomer, UpdateCustomerCommand updateCustomerCommand) {
		try {
			HttpClient client = HttpClients.createDefault();

			HttpPut httpPut = new HttpPut("http://localhost:4567/api/v1/customers/" + String.valueOf(idCustomer));
			StringEntity updateCustomerCmd = new StringEntity(new Gson().toJson(updateCustomerCommand));

			httpPut.setEntity(updateCustomerCmd);
			httpPut.setHeader("Accept", "application/json");
			httpPut.setHeader("Content-type", "application/json");

			return Optional.ofNullable(client.execute(httpPut));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	private Optional<Customer> getCustomer(HttpResponse httpResponse) {
		try {
			return Optional
					.ofNullable(new Gson().fromJson(EntityUtils.toString(httpResponse.getEntity()), Customer.class));

		} catch (JsonSyntaxException | ParseException | IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	private void assertCustomerParamsEquals(Customer cus1, Customer cus2) {
		assertEquals(cus1.getFirstName(), cus2.getFirstName());
		assertEquals(cus1.getLastName(), cus2.getLastName());
		assertEquals(cus1.getStreet(), cus2.getStreet());
		assertEquals(cus1.getCity(), cus2.getCity());
		assertEquals(cus1.getZip(), cus2.getZip());
		assertEquals(cus1.getPhone(), cus2.getPhone());
		assertEquals(cus1.getAccountList().get(0).getBalance().intValue(),
				cus1.getAccountList().get(0).getBalance().intValue());
		assertEquals(cus2.getAccountList().get(0).getDescription(), cus1.getAccountList().get(0).getDescription());
	}
}
