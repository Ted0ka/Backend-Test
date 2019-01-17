package com.backend;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assume;
import org.junit.Test;

import com.backend.command.DepositCommand;
import com.backend.model.Account;
import com.backend.model.Operation;
import com.backend.response.StatusResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class DespoitApiTest extends AbstractTest {

	@Test
	public void deposit_AllFieldsValid_AddMoneyToAccount() {
		startServer();

		Long destCustomerId = 1L;
		Long destAccountNo = 1L;
		Long destAccountOpertionId = 4L;
		String description = "description";
		
		BigDecimal amountToDeposit = new BigDecimal(100);
		

		Optional<Account> accountBeforeDepositOpt = getAccount(Math.toIntExact(destAccountNo));
		Assume.assumeTrue(accountBeforeDepositOpt.isPresent());
		Account accountBeforeDeposit = accountBeforeDepositOpt.get();

		Optional<HttpResponse> responsePostOpt = executeDeposit(new Long(destAccountNo), amountToDeposit, description);
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> accountAfterDepositOpt = getAccount(Math.toIntExact(destAccountNo));
		Assume.assumeTrue(accountAfterDepositOpt.isPresent());
		Account accountAfterDeposit = accountAfterDepositOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.CREATED_201, "Deposit done");

		assertEquals(accountBeforeDeposit.getBalance().intValue() + amountToDeposit.intValue(),
				accountAfterDeposit.getBalance().intValue());
		assertEquals(HttpStatus.CREATED_201, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);
		
		
		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo), Math.toIntExact(destAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		Optional<Operation> srcCustOperationOpt = getOperation(srcCustOperationGetResp);
		Assume.assumeTrue(srcCustOperationOpt.isPresent());
		Operation destCustOperation = srcCustOperationOpt.get();
		
		assertEquals(HttpStatus.OK_200, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(amountToDeposit.intValue(), destCustOperation.getAmount().intValue());
		assertEquals(description, destCustOperation.getDescription());
		assertEquals(destCustomerId, destCustOperation.getDestinationAccountNo());

		
		stopServer();
	}

	@Test
	public void deposit_DepositZero_Fail() {
		startServer();

		Long destCustomerId = 1L;
		Long destAccountNo = 1L;
		Long destAccountOpertionId = 5L;

		BigDecimal amountToDeposit = new BigDecimal(0);
		
		Optional<Account> accountBeforeDepositOpt = getAccount(Math.toIntExact(destAccountNo));
		Assume.assumeTrue(accountBeforeDepositOpt.isPresent());
		Account accountBeforeDeposit = accountBeforeDepositOpt.get();

		Optional<HttpResponse> responsePostOpt = executeDeposit(destAccountNo, amountToDeposit, "description");
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> accountAfterDepositOpt = getAccount(Math.toIntExact(destAccountNo));
		Assume.assumeTrue(accountAfterDepositOpt.isPresent());
		Account accountAfterDeposit = accountAfterDepositOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(accountBeforeDeposit.getBalance().intValue(), accountAfterDeposit.getBalance().intValue());
		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		
		
		
		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo), Math.toIntExact(destAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		StatusResponse statusCustOperationResp = getStatusResponse(srcCustOperationGetResp);
		StatusResponse statusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(destAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(destAccountNo) + " and customer id = " + Math.toIntExact(destCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(statusCustOperationRespExpected, statusCustOperationResp);
		
		
		
		
		stopServer();
	}

	@Test
	public void deposit_DepositNegative_Fail() {
		startServer();

		Long destCustomerId = 1L;
		Long destAccountNo = 1L;
		Long destAccountOpertionId = 5L;

		BigDecimal amountToDeposit = new BigDecimal(-1);
		
		Optional<Account> accountBeforeDepositOpt = getAccount(Math.toIntExact(destAccountNo));
		Assume.assumeTrue(accountBeforeDepositOpt.isPresent());
		Account accountBeforeDeposit = accountBeforeDepositOpt.get();

		Optional<HttpResponse> responsePostOpt = executeDeposit(destAccountNo, amountToDeposit, "description");
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> accountAfterDepositOpt = getAccount(Math.toIntExact(destAccountNo));
		Assume.assumeTrue(accountAfterDepositOpt.isPresent());
		Account accountAfterDeposit = accountAfterDepositOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(accountBeforeDeposit.getBalance().intValue(), accountAfterDeposit.getBalance().intValue());
		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		
		
		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo), Math.toIntExact(destAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		StatusResponse statusCustOperationResp = getStatusResponse(srcCustOperationGetResp);
		StatusResponse statusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(destAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(destAccountNo) + " and customer id = " + Math.toIntExact(destCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(statusCustOperationRespExpected, statusCustOperationResp);
		
		
		
		stopServer();
	}

	@Test
	public void deposit_InvalidAccuntId_Fail() {
		startServer();

		Long incorrectDestAccNo = 99L;

		BigDecimal amountToDeposit = new BigDecimal(1000);
		
		Optional<HttpResponse> responsePostOpt = executeDeposit(incorrectDestAccNo, amountToDeposit, "description");
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);
		
		stopServer();
	}

	@Test
	public void deposit_MissingDescriptionField_Fail() {
		startServer();
		
		Long destCustomerId = 1L;
		Long destAccountNo = 1L;
		Long destAccountOpertionId = 5L;

		BigDecimal amountToDeposit = new BigDecimal(1000);
		
		Optional<Account> accountBeforeDepositOpt = getAccount(Math.toIntExact(destAccountNo));
		Assume.assumeTrue(accountBeforeDepositOpt.isPresent());
		Account accountBeforeDeposit = accountBeforeDepositOpt.get();

		Optional<HttpResponse> responsePostOpt = executeDeposit(destAccountNo, amountToDeposit, "");
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> accountAfterDepositOpt = getAccount(Math.toIntExact(destAccountNo));
		Assume.assumeTrue(accountAfterDepositOpt.isPresent());
		Account accountAfterDeposit = accountAfterDepositOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(accountBeforeDeposit.getBalance().intValue(), accountAfterDeposit.getBalance().intValue());
		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);
		
		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo), Math.toIntExact(destAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		StatusResponse statusCustOperationResp = getStatusResponse(srcCustOperationGetResp);
		StatusResponse statusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(destAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(destAccountNo) + " and customer id = " + Math.toIntExact(destCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(statusCustOperationRespExpected, statusCustOperationResp);

		stopServer();
	}

	private Optional<HttpResponse> executeDeposit(Long accountNo, BigDecimal amount, String description) {
		try {
			HttpClient client = HttpClients.createDefault();

			HttpPost httpPost = new HttpPost("http://localhost:4567/api/v1/deposit");

			StringEntity depositCommand = new StringEntity(
					new Gson().toJson(new DepositCommand(accountNo, amount, description)));

			httpPost.setEntity(depositCommand);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			return Optional.ofNullable(client.execute(httpPost));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
	
	private Optional<HttpResponse> executeGetOperation(int idCustomer, int idAccount, int idOperation) {
		try {
			HttpClient client = HttpClients.createDefault();

			HttpGet httpGet = new HttpGet("http://localhost:4567/api/v1/customers/" + idCustomer + "/accounts/" + idAccount
					+ "/operations/" + idOperation);

			return Optional.ofNullable(client.execute(httpGet));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	private Optional<Account> getAccount(int id) {
		try {
			HttpClient client = HttpClients.createDefault();

			HttpGet httpGet = new HttpGet("http://localhost:4567/api/v1/customers/1/accounts/" + id);
			HttpResponse responseGet = client.execute(httpGet);

			return Optional
					.ofNullable(new Gson().fromJson(EntityUtils.toString(responseGet.getEntity()), Account.class));
		} catch (JsonSyntaxException | ParseException | IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}
	
	private Optional<Operation> getOperation(HttpResponse httpResponse) {
		try {
			return Optional
					.ofNullable(new Gson().fromJson(EntityUtils.toString(httpResponse.getEntity()), Operation.class));

		} catch (JsonSyntaxException | ParseException | IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
}
