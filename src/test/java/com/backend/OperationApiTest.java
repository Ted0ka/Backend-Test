package com.backend;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assume;
import org.junit.Test;

import com.backend.datacreator.DataCreator;
import com.backend.model.Operation;
import com.backend.response.StatusResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public class OperationApiTest extends AbstractTest {
	@Test
	public void getOpration_AllParametersValid_Returnoperation() {
		startServer();

		int idCustomer = 1;
		int idAccount = 1;
		int idOperation = 1;

		Operation operationExpected = DataCreator.createData().get(0).getAccountList().get(0).getOperationList().get(0);

		Optional<HttpResponse> responseGetOpt = executeGetOperation(idCustomer, idAccount, idOperation);
		Assume.assumeTrue(responseGetOpt.isPresent());
		HttpResponse responseGet = responseGetOpt.get();

		Optional<Operation> operationActualOpt = getOperation(responseGet);
		Assume.assumeTrue(operationActualOpt.isPresent());
		Operation operationActual = operationActualOpt.get();

		assertEquals(HttpStatus.OK_200, responseGet.getStatusLine().getStatusCode());
		assertEquals(operationExpected.getDestinationAccountNo(), operationActual.getDestinationAccountNo());
		assertEquals(operationExpected.getAmount().intValue(), operationActual.getAmount().intValue());
		assertEquals(operationExpected.getDescription(), operationActual.getDescription());

		stopServer();
	}

	@Test
	public void getOpration_InvalidCustomerId_Fail() {
		startServer();

		int idCustomer = 99;
		int idAccount = 1;
		int idOperation = 1;

		Optional<HttpResponse> responseGetOpt = executeGetOperation(idCustomer, idAccount, idOperation);
		Assume.assumeTrue(responseGetOpt.isPresent());
		HttpResponse responseGet = responseGetOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responseGet);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404, "Operation with id = " + idOperation
				+ ", account  id = " + idAccount + " and customer id = " + idCustomer + " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, responseGet.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void getOpration_InvalidAccountId_Fail() {
		startServer();

		int idCustomer = 1;
		int idAccount = 99;
		int idOperation = 1;

		Optional<HttpResponse> responseGetOpt = executeGetOperation(idCustomer, idAccount, idOperation);
		Assume.assumeTrue(responseGetOpt.isPresent());
		HttpResponse responseGet = responseGetOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responseGet);
		
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404, "Operation with id = " + idCustomer
				+ ", account  id = " + idAccount + " and customer id = " + idCustomer + " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, responseGet.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
	}

	@Test
	public void getOpration_InvalidOperationId_Fail() {
		startServer();

		int idCustomer = 1;
		int idAccount = 1;
		int idOperation = 99;

		Optional<HttpResponse> responseGetOpt = executeGetOperation(idCustomer, idAccount, idOperation);
		Assume.assumeTrue(responseGetOpt.isPresent());
		HttpResponse responseGet = responseGetOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responseGet);
		StatusResponse StatusResponseExpected = new StatusResponse(HttpStatus.NOT_FOUND_404, "Operation with id = " + idOperation
				+ ", account  id = " + idAccount + " and customer id = " + idCustomer + " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, responseGet.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		stopServer();
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
