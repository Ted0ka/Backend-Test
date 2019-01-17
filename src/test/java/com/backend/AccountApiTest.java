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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assume;
import org.junit.Test;

import com.backend.command.AddAccountCommand;
import com.backend.datacreator.DataCreator;
import com.backend.model.Account;
import com.backend.response.StatusResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class AccountApiTest extends AbstractTest {

	@Test
	public void getAccount_ValidCustomerIdAndAccountId_ReturnAccount() {
		startServer();

		int idCustomer = 1;
		int idAccount = 1;

		Account accountExpected = DataCreator.createData().get(0).getAccountList().get(0);

		Optional<HttpResponse> responseGetOpt = executeGetAccount(idCustomer, idAccount);
		Assume.assumeTrue(responseGetOpt.isPresent());
		HttpResponse responseGet = responseGetOpt.get();

		Optional<Account> accountActualOpt = getAccount(responseGet);
		Assume.assumeTrue(accountActualOpt.isPresent());
		Account accountActual = accountActualOpt.get();

		assertEquals(HttpStatus.OK_200, responseGet.getStatusLine().getStatusCode());
		assertEquals(accountExpected.getDescription(), accountActual.getDescription());
		assertEquals(accountExpected.getBalance().intValue(), accountActual.getBalance().intValue());

		stopServer();
	}

	@Test
	public void getAccounts_InvalidCustomerId_Fail() {
		startServer();

		int idCustomer = 99;
		
		Optional<HttpResponse> responseGetOpt = executeGetUserAccounts(idCustomer);
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
	public void getAccount_InvalidAccountId_Fail() {
		startServer();

		int idCustomer = 1;
		int idAccount = 99;

		Optional<HttpResponse> responseGetOpt = executeGetAccount(idCustomer, idAccount);
		Assume.assumeTrue(responseGetOpt.isPresent());
		HttpResponse responseGet = responseGetOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responseGet);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Account with id = " + idAccount + " and customer id = " + idCustomer + " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, responseGet.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void getAccount_InvalidCustomerIdAndValidAccountId_Fail() {
		startServer();

		int idCustomer = 99;
		int idAccount = 2;

		Optional<HttpResponse> responseGetOpt = executeGetAccount(idCustomer, idAccount);
		Assume.assumeTrue(responseGetOpt.isPresent());
		HttpResponse responseGet = responseGetOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responseGet);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Account with id = " + idAccount + " and customer id = " + idCustomer + " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, responseGet.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void createAccount_ValidCustomerId_ReturnAccount() {
		startServer();

		int idCustomer = 1;
		int idOfNewAccount = 4;

		Optional<HttpResponse> responseGetBeforeOpt = executeGetAccount(idCustomer, idOfNewAccount);
		Assume.assumeTrue(responseGetBeforeOpt.isPresent());
		HttpResponse responseGetBefore = responseGetBeforeOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responseGetBefore);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Account with id = " + idOfNewAccount + " and customer id = " + idCustomer + " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, responseGetBefore.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		AddAccountCommand addAccountCommand = new AddAccountCommand("description", new BigDecimal(1000));
		Optional<HttpResponse> responsePostOpt = executeCreateAccount(idCustomer, addAccountCommand);
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> accountFromPostOpt = getAccount(responsePost);
		Assume.assumeTrue(accountFromPostOpt.isPresent());
		Account accountFromPost = accountFromPostOpt.get();

		assertEquals(HttpStatus.CREATED_201, responsePost.getStatusLine().getStatusCode());

		assertEquals(addAccountCommand.getDescription(), accountFromPost.getDescription());
		assertEquals(addAccountCommand.getAmount().intValue(), accountFromPost.getBalance().intValue());

		Optional<HttpResponse> responseGetAfterOpt = executeGetAccount(idCustomer,
				Math.toIntExact(accountFromPost.getId()));
		Assume.assumeTrue(responseGetAfterOpt.isPresent());
		HttpResponse responseGetAfter = responseGetAfterOpt.get();

		Optional<Account> accountActualOpt = getAccount(responseGetAfter);
		Assume.assumeTrue(accountActualOpt.isPresent());
		Account accountActual = accountActualOpt.get();

		assertEquals(HttpStatus.OK_200, responseGetAfter.getStatusLine().getStatusCode());
		assertEquals(addAccountCommand.getDescription(), accountActual.getDescription());
		assertEquals(addAccountCommand.getAmount().intValue(), accountActual.getBalance().intValue());

		assertEquals(idOfNewAccount, Math.toIntExact(accountFromPost.getId()));

		stopServer();
	}

	@Test
	public void createAccount_MissingAmountField_Fail() {
		startServer();

		int idCustomer = 1;

		AddAccountCommand addAccountCommand = new AddAccountCommand("description", null);
		Optional<HttpResponse> responsePostopt = executeCreateAccount(idCustomer, addAccountCommand);
		Assume.assumeTrue(responsePostopt.isPresent());
		HttpResponse responsePost = responsePostopt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.BAD_REQUEST_400,
				"Customer with id = " + idCustomer + " not exist or data is invalid");

		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void createAccount_IncorrectAmountField_Fail() {
		startServer();

		int idCustomer = 1;

		AddAccountCommand addAccountCommand = new AddAccountCommand("description", new BigDecimal(-1));

		Optional<HttpResponse> responsePostopt = executeCreateAccount(idCustomer, addAccountCommand);
		Assume.assumeTrue(responsePostopt.isPresent());
		HttpResponse responsePost = responsePostopt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.BAD_REQUEST_400,
				"Customer with id = " + idCustomer + " not exist or data is invalid");

		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void deleteAccount_ValidCustomerIdAndAccountId_ReturnOk() {
		startServer();

		int idCustomer = 1;
		int idAccount = 1;

		Optional<HttpResponse> responseGetBeforeOpt = executeGetAccount(idCustomer, idAccount);
		Assume.assumeTrue(responseGetBeforeOpt.isPresent());
		HttpResponse responseGetBefore = responseGetBeforeOpt.get();
		assertEquals(HttpStatus.OK_200, responseGetBefore.getStatusLine().getStatusCode());

		Optional<HttpResponse> responseDeleteOpt = executeDeleteAccount(idCustomer, idAccount);
		Assume.assumeTrue(responseDeleteOpt.isPresent());
		HttpResponse responseDelete = responseDeleteOpt.get();

		Optional<HttpResponse> responseGetCustomerAfterOpt = executeGetUserAccounts(idCustomer);
		Assume.assumeTrue(responseGetCustomerAfterOpt.isPresent());
		HttpResponse responseGetCustomerAfter = responseGetCustomerAfterOpt.get();
		assertEquals(HttpStatus.OK_200, responseGetCustomerAfter.getStatusLine().getStatusCode());

		Optional<HttpResponse> responseGetAfterOpt = executeGetAccount(idCustomer, idAccount);
		Assume.assumeTrue(responseGetAfterOpt.isPresent());
		HttpResponse responseGetAfter = responseGetAfterOpt.get();
		assertEquals(HttpStatus.NOT_FOUND_404, responseGetAfter.getStatusLine().getStatusCode());

		StatusResponse statusResponseActual = getStatusResponse(responseDelete);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.OK_200,
				"Account with id = " + idAccount + " and customer id = " + idCustomer + " deleted");

		assertEquals(HttpStatus.OK_200, responseDelete.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void deleteAccount_ValidCustomerIdAndInvalidAccountId_Fail() {
		startServer();

		int idCustomer = 1;
		int idAccount = 99;

		Optional<HttpResponse> responseDeleteOpt = executeDeleteAccount(idCustomer, idAccount);
		Assume.assumeTrue(responseDeleteOpt.isPresent());

		HttpResponse responseDelete = responseDeleteOpt.get();

		Optional<HttpResponse> responseGetCustomerAfterOpt = executeGetUserAccounts(idCustomer);
		Assume.assumeTrue(responseGetCustomerAfterOpt.isPresent());

		HttpResponse responseGetCustomerAfter = responseGetCustomerAfterOpt.get();
		assertEquals(HttpStatus.OK_200, responseGetCustomerAfter.getStatusLine().getStatusCode());

		StatusResponse statusResponseActual = getStatusResponse(responseDelete);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Account with id = " + idAccount + " and customer id = " + idCustomer + " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, responseDelete.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void deleteAccount_InvalidCustomerIdAndValidAccountId_Fail() {
		startServer();

		int idCustomer = 99;
		int idAccount = 1;

		Optional<HttpResponse> responseDeleteOpt = executeDeleteAccount(idCustomer, idAccount);
		Assume.assumeTrue(responseDeleteOpt.isPresent());

		HttpResponse responseDelete = responseDeleteOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responseDelete);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Account with id = " + idAccount + " and customer id = " + idCustomer + " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, responseDelete.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	private Optional<HttpResponse> executeGetAccount(int idCustomer, int idAccount) {
		try {
			HttpClient client = HttpClients.createDefault();

			HttpGet httpGet = new HttpGet("http://localhost:4567/api/v1/customers/" + String.valueOf(idCustomer)
					+ "/accounts/" + String.valueOf(idAccount));

			return Optional.ofNullable(client.execute(httpGet));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	private Optional<HttpResponse> executeGetUserAccounts(int idCustomer) {
		try {
			HttpClient client = HttpClients.createDefault();

			HttpGet httpGet = new HttpGet(
					"http://localhost:4567/api/v1/customers/" + String.valueOf(idCustomer) + "/accounts");

			return Optional.ofNullable(client.execute(httpGet));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	private Optional<HttpResponse> executeCreateAccount(int idCustomer, AddAccountCommand addAccountCommand) {
		try {
			CloseableHttpClient client = HttpClients.createDefault();

			HttpPost httpPost = new HttpPost("http://localhost:4567/api/v1/customers/" + idCustomer + "/accounts");

			StringEntity addAccountCmd = new StringEntity(new Gson().toJson(addAccountCommand));

			httpPost.setEntity(addAccountCmd);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			return Optional.ofNullable(client.execute(httpPost));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	private Optional<HttpResponse> executeDeleteAccount(int idCustomer, int idAccount) {
		try {
			HttpClient client = HttpClients.createDefault();

			HttpDelete httpDelete = new HttpDelete(
					"http://localhost:4567/api/v1/customers/" + idCustomer + "/accounts/" + idAccount);

			return Optional.ofNullable(client.execute(httpDelete));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	private Optional<Account> getAccount(HttpResponse httpResponse) {
		try {
			return Optional
					.ofNullable(new Gson().fromJson(EntityUtils.toString(httpResponse.getEntity()), Account.class));

		} catch (JsonSyntaxException | ParseException | IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
}
