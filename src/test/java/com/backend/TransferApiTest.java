package com.backend;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assume;
import org.junit.Test;

import com.backend.command.TransferCommand;
import com.backend.model.Account;
import com.backend.model.Operation;
import com.backend.response.StatusResponse;
import com.backend.service.TransactionType;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class TransferApiTest extends AbstractTest {

	@Test
	public void transfer_AllFieldsvalid_ReturnOk() {
		startServer();

		Long srcCustomerId = 1L;
		Long srcAccountNo = 1L;
		Long srcAccountOpertionId = 4L;
		Long destCustomerId = 2L;
		Long destAccountNo = 2L;
		Long destAccountOpertionId = 5L;

		String description = "Transfer done";
		BigDecimal amountToTransfer = new BigDecimal(1000);

		Optional<Account> srcAccBeforeTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(srcAccBeforeTransactionOpt.isPresent());
		Account srcAccBeforeTransaction = srcAccBeforeTransactionOpt.get();

		Optional<Account> destAccBeforeTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo));
		Assume.assumeTrue(destAccBeforeTransactionOpt.isPresent());
		Account destAccBeforeTransaction = destAccBeforeTransactionOpt.get();

		Optional<HttpResponse> responsePostOpt = executeTransfer(srcAccountNo, destAccountNo, amountToTransfer,
				description);
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> srcAccAfterTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(srcAccAfterTransactionOpt.isPresent());
		Account srcAccAfterTransaction = srcAccAfterTransactionOpt.get();

		Optional<Account> destAccAfterTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo));
		Assume.assumeTrue(destAccAfterTransactionOpt.isPresent());
		Account destAccAfterTransaction = destAccAfterTransactionOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse statusResponseExpected = new StatusResponse(HttpStatus.CREATED_201, "Transfer done");

		assertEquals(srcAccBeforeTransaction.getBalance().intValue(),
				srcAccAfterTransaction.getBalance().intValue() + amountToTransfer.intValue());
		assertEquals(destAccBeforeTransaction.getBalance().intValue(),
				destAccAfterTransaction.getBalance().intValue() - amountToTransfer.intValue());
		assertEquals(HttpStatus.CREATED_201, responsePost.getStatusLine().getStatusCode());
		assertEquals(statusResponseExpected, statusResponseActual);

		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo), Math.toIntExact(srcAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		Optional<Operation> srcCustomerOperationOpt = getOperation(srcCustOperationGetResp);
		Assume.assumeTrue(srcCustomerOperationOpt.isPresent());
		Operation srcCustOperation = srcCustomerOperationOpt.get();

		assertEquals(HttpStatus.OK_200, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(amountToTransfer.intValue(), srcCustOperation.getAmount().intValue());
		assertEquals(srcAccountNo, srcCustOperation.getSourceAccountNo());
		assertEquals(destAccountNo, srcCustOperation.getDestinationAccountNo());
		assertEquals(description, srcCustOperation.getDescription());
		assertEquals(TransactionType.WITHDRAW, srcCustOperation.getTransactionType());

		Optional<HttpResponse> destCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo), Math.toIntExact(destAccountOpertionId));
		Assume.assumeTrue(destCustOperationGetRespOpt.isPresent());
		HttpResponse destCustOperationGetResp = destCustOperationGetRespOpt.get();

		Optional<Operation> destCustOperationOpt = getOperation(destCustOperationGetResp);
		Assume.assumeTrue(destCustOperationOpt.isPresent());
		Operation destCustOperation = destCustOperationOpt.get();

		assertEquals(HttpStatus.OK_200, destCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(amountToTransfer.intValue(), destCustOperation.getAmount().intValue());
		assertEquals(srcAccountNo, destCustOperation.getSourceAccountNo());
		assertEquals(destAccountNo, destCustOperation.getDestinationAccountNo());
		assertEquals(description, destCustOperation.getDescription());
		assertEquals(TransactionType.DEPOSIT, destCustOperation.getTransactionType());

		stopServer();
	}

	@Test
	public void transfer_transferToTheSameAccount_Fail() {
		startServer();

		Long srcCustomerId = 1L;
		Long srcAccountNo = 1L;
		Long srcAccountOpertionId = 4L;

		BigDecimal amountToTransfer = new BigDecimal(1000);

		Optional<Account> accBeforeTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(accBeforeTransactionOpt.isPresent());
		Account accBeforeTransaction = accBeforeTransactionOpt.get();

		Optional<HttpResponse> responsePostOpt = executeTransfer(srcCustomerId, srcCustomerId, amountToTransfer,
				"transfer description");
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> accAfterTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(accAfterTransactionOpt.isPresent());
		Account accAfterTransaction = accAfterTransactionOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(accBeforeTransaction.getBalance().intValue(), accAfterTransaction.getBalance().intValue());
		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo), Math.toIntExact(srcAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		StatusResponse statusCustOperationResp = getStatusResponse(srcCustOperationGetResp);
		StatusResponse statusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(srcAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(srcAccountNo) + " and customer id = " + Math.toIntExact(srcCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(statusCustOperationRespExpected, statusCustOperationResp);

		stopServer();
	}

	@Test
	public void transfer_transferMoreThanAccountBalance_Fail() {
		startServer();

		Long srcCustomerId = 1L;
		Long srcAccountNo = 1L;
		Long srcAccountOpertionId = 4L;
		Long destCustomerId = 2L;
		Long destAccountNo = 2L;
		Long destAccountOpertionId = 5L;

		BigDecimal amountToTransfer = new BigDecimal(9999);

		Optional<Account> sourceAccBeforeTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(sourceAccBeforeTransactionOpt.isPresent());
		Account sourceAccBeforeTransaction = sourceAccBeforeTransactionOpt.get();

		Optional<Account> destinationAccBeforeTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo));
		Assume.assumeTrue(destinationAccBeforeTransactionOpt.isPresent());
		Account destinationAccBeforeTransaction = destinationAccBeforeTransactionOpt.get();

		Optional<HttpResponse> responsePostOpt = executeTransfer(srcAccountNo, destAccountNo, amountToTransfer,
				"transfer description");
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> sourceAccAfterTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(sourceAccAfterTransactionOpt.isPresent());
		Account sourceAccAfterTransaction = sourceAccAfterTransactionOpt.get();

		Optional<Account> destinationAccAfterTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo));
		Assume.assumeTrue(destinationAccAfterTransactionOpt.isPresent());
		Account destinationAccAfterTransaction = destinationAccAfterTransactionOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(sourceAccBeforeTransaction.getBalance().intValue(),
				sourceAccAfterTransaction.getBalance().intValue());
		assertEquals(destinationAccBeforeTransaction.getBalance().intValue(),
				destinationAccAfterTransaction.getBalance().intValue());
		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo), Math.toIntExact(srcAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		StatusResponse srcStatusCustOperationResp = getStatusResponse(srcCustOperationGetResp);
		StatusResponse srcStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(srcAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(srcAccountNo) + " and customer id = " + Math.toIntExact(srcCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(srcStatusCustOperationRespExpected, srcStatusCustOperationResp);

		Optional<HttpResponse> destCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo), Math.toIntExact(destAccountOpertionId));
		Assume.assumeTrue(destCustOperationGetRespOpt.isPresent());
		HttpResponse destCustOperationGetResp = destCustOperationGetRespOpt.get();

		StatusResponse destStatusCustOperationResp = getStatusResponse(destCustOperationGetResp);
		StatusResponse destStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(destAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(destAccountNo) + " and customer id = " + Math.toIntExact(destCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(destStatusCustOperationRespExpected, destStatusCustOperationResp);

		stopServer();
	}

	@Test
	public void transfer_transferNegative_Fail() {
		startServer();

		Long srcCustomerId = 1L;
		Long srcAccountNo = 1L;
		Long srcAccountOpertionId = 4L;
		Long destCustomerId = 2L;
		Long destionAccountNo = 2L;
		Long destAccountOpertionId = 5L;

		BigDecimal amountToTransfer = new BigDecimal(-1);

		Optional<Account> srcAccBeforeTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(srcAccBeforeTransactionOpt.isPresent());
		Account srcAccBeforeTransaction = srcAccBeforeTransactionOpt.get();

		Optional<Account> destAccBeforeTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destionAccountNo));
		Assume.assumeTrue(destAccBeforeTransactionOpt.isPresent());
		Account destAccBeforeTransaction = destAccBeforeTransactionOpt.get();

		Optional<HttpResponse> responsePostOpt = executeTransfer(srcAccountNo, destionAccountNo, amountToTransfer,
				"transfer description");
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> srcAccAfterTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(srcAccAfterTransactionOpt.isPresent());
		Account srcAccAfterTransaction = srcAccAfterTransactionOpt.get();

		Optional<Account> destAccAfterTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destionAccountNo));
		Assume.assumeTrue(destAccAfterTransactionOpt.isPresent());
		Account destAccAfterTransaction = destAccAfterTransactionOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(srcAccBeforeTransaction.getBalance().intValue(),
				srcAccAfterTransaction.getBalance().intValue());
		assertEquals(destAccBeforeTransaction.getBalance().intValue(),
				destAccAfterTransaction.getBalance().intValue());
		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo), Math.toIntExact(srcAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		StatusResponse srcStatusCustOperationResp = getStatusResponse(srcCustOperationGetResp);
		StatusResponse srcStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(srcAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(srcAccountNo) + " and customer id = " + Math.toIntExact(srcCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(srcStatusCustOperationRespExpected, srcStatusCustOperationResp);

		Optional<HttpResponse> destCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountOpertionId), Math.toIntExact(destAccountOpertionId));
		Assume.assumeTrue(destCustOperationGetRespOpt.isPresent());
		HttpResponse destCustOperationGetResp = destCustOperationGetRespOpt.get();

		StatusResponse destStatusCustOperationResp = getStatusResponse(destCustOperationGetResp);
		StatusResponse destStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(destAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(destAccountOpertionId) + " and customer id = "
						+ Math.toIntExact(destCustomerId) + " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(destStatusCustOperationRespExpected, destStatusCustOperationResp);

		stopServer();
	}

	@Test
	public void transfer_transferZero_Fail() {
		startServer();

		Long srcCustomerId = 1L;
		Long srcAccountNo = 1L;
		Long srcAccountOpertionId = 4L;
		Long destCustomerId = 2L;
		Long destAccountNo = 2L;
		Long destAccountOpertionId = 5L;
		BigDecimal amountToTransfer = new BigDecimal(0);

		Optional<Account> srcAccBeforeTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(srcAccBeforeTransactionOpt.isPresent());
		Account srcAccBeforeTransaction = srcAccBeforeTransactionOpt.get();

		Optional<Account> destAccBeforeTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo));
		Assume.assumeTrue(destAccBeforeTransactionOpt.isPresent());
		Account destAccBeforeTransaction = destAccBeforeTransactionOpt.get();

		Optional<HttpResponse> responsePostOpt = executeTransfer(srcAccountNo, destAccountNo, amountToTransfer,
				"transfer description");
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> srcAccAfterTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(srcAccAfterTransactionOpt.isPresent());
		Account srcAccAfterTransaction = srcAccAfterTransactionOpt.get();

		Optional<Account> destAccAfterTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo));
		Assume.assumeTrue(destAccAfterTransactionOpt.isPresent());
		Account destAccAfterTransaction = destAccAfterTransactionOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(srcAccBeforeTransaction.getBalance().intValue(),
				srcAccAfterTransaction.getBalance().intValue());
		assertEquals(destAccBeforeTransaction.getBalance().intValue(),
				destAccAfterTransaction.getBalance().intValue());
		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo), Math.toIntExact(srcAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		StatusResponse srcStatusCustOperationResp = getStatusResponse(srcCustOperationGetResp);
		StatusResponse srcStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(srcAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(srcAccountNo) + " and customer id = " + Math.toIntExact(srcCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(srcStatusCustOperationRespExpected, srcStatusCustOperationResp);

		Optional<HttpResponse> destCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo), Math.toIntExact(destAccountOpertionId));
		Assume.assumeTrue(destCustOperationGetRespOpt.isPresent());
		HttpResponse destCustOperationGetResp = destCustOperationGetRespOpt.get();

		StatusResponse destStatusCustOperationResp = getStatusResponse(destCustOperationGetResp);
		StatusResponse destStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(destAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(destAccountNo) + " and customer id = " + Math.toIntExact(destCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(destStatusCustOperationRespExpected, destStatusCustOperationResp);

		stopServer();
	}

	@Test
	public void transfer_transferWithNoDescription_Fail() {
		startServer();

		Long srcCustomerId = 1L;
		Long srcAccountNo = 1L;
		Long srcAccountOpertionId = 4L;
		Long destCustomerId = 2L;
		Long destAccountNo = 2L;
		Long destAccountOpertionId = 5L;

		BigDecimal amountToTransfer = new BigDecimal(9999);

		Optional<Account> srcAccBeforeTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(srcAccBeforeTransactionOpt.isPresent());
		Account srcAccBeforeTransaction = srcAccBeforeTransactionOpt.get();

		Optional<Account> destAccBeforeTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo));
		Assume.assumeTrue(destAccBeforeTransactionOpt.isPresent());
		Account destAccBeforeTransaction = destAccBeforeTransactionOpt.get();

		Optional<HttpResponse> responsePostOpt = executeTransfer(srcAccountNo, destAccountNo, amountToTransfer, "");
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> srcAccAfterTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(srcAccAfterTransactionOpt.isPresent());
		Account srcAccAfterTransaction = srcAccAfterTransactionOpt.get();

		Optional<Account> destAccAfterTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo));
		Assume.assumeTrue(destAccAfterTransactionOpt.isPresent());
		Account destAccAfterTransaction = destAccAfterTransactionOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(srcAccBeforeTransaction.getBalance().intValue(),
				srcAccAfterTransaction.getBalance().intValue());
		assertEquals(destAccBeforeTransaction.getBalance().intValue(),
				destAccAfterTransaction.getBalance().intValue());
		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo), Math.toIntExact(srcAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		StatusResponse srcStatusCustOperationResp = getStatusResponse(srcCustOperationGetResp);
		StatusResponse srcStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(srcAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(srcAccountNo) + " and customer id = " + Math.toIntExact(srcCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(srcStatusCustOperationRespExpected, srcStatusCustOperationResp);

		Optional<HttpResponse> destCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo), Math.toIntExact(destAccountOpertionId));
		Assume.assumeTrue(destCustOperationGetRespOpt.isPresent());
		HttpResponse destCustOperationGetResp = destCustOperationGetRespOpt.get();

		StatusResponse destStatusCustOperationResp = getStatusResponse(destCustOperationGetResp);
		StatusResponse destStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(destAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(destAccountNo) + " and customer id = " + Math.toIntExact(destCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(destStatusCustOperationRespExpected, destStatusCustOperationResp);

		stopServer();
	}

	@Test
	public void transfer_transferToInvalidDestinationAccountNo_Fail() {
		startServer();

		Long srcCustomerId = 1L;
		Long srcAccountNo = 1L;
		Long srcAccountOpertionId = 4L;
		Long destCustomerId = 2L;
		Long incorrectAccountNo = 99L;
		Long destAccountOpertionId = 5L;

		BigDecimal amountToTransfer = new BigDecimal(1000);

		Optional<Account> srcAccBeforeTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(srcAccBeforeTransactionOpt.isPresent());
		Account srcAccBeforeTransaction = srcAccBeforeTransactionOpt.get();

		Optional<HttpResponse> responsePostOpt = executeTransfer(srcAccountNo, incorrectAccountNo, amountToTransfer,
				"transfer description");
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> srcAccAfterTransactionOpt = getAccount(Math.toIntExact(srcCustomerId),
				Math.toIntExact(srcAccountNo));
		Assume.assumeTrue(srcAccAfterTransactionOpt.isPresent());
		Account srcAccAfterTransaction = srcAccAfterTransactionOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(srcAccBeforeTransaction.getBalance().intValue(),
				srcAccAfterTransaction.getBalance().intValue());
		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(srcCustomerId),
				Math.toIntExact(incorrectAccountNo), Math.toIntExact(srcAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		StatusResponse srcStatusCustOperationResp = getStatusResponse(srcCustOperationGetResp);
		StatusResponse srcStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(srcAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(incorrectAccountNo) + " and customer id = " + Math.toIntExact(srcCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(srcStatusCustOperationRespExpected, srcStatusCustOperationResp);

		Optional<HttpResponse> destCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(destCustomerId),
				Math.toIntExact(incorrectAccountNo), Math.toIntExact(destAccountOpertionId));
		Assume.assumeTrue(destCustOperationGetRespOpt.isPresent());
		HttpResponse destCustOperationGetResp = destCustOperationGetRespOpt.get();

		StatusResponse destStatusCustOperationResp = getStatusResponse(destCustOperationGetResp);
		StatusResponse destStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(destAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(incorrectAccountNo) + " and customer id = " + Math.toIntExact(destCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(destStatusCustOperationRespExpected, destStatusCustOperationResp);

		stopServer();
	}

	@Test
	public void transfer_transferFromInvalidSourceAccountNo_Fail() {
		startServer();

		Long srcCustomerId = 1L;
		Long incorrectAccountNo = 99L;
		Long srcAccountOpertionId = 4L;
		Long destCustomerId = 2L;
		Long destAccountNo = 2L;

		BigDecimal amountToTransfer = new BigDecimal(1000);

		Optional<Account> destAccBeforeTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo));
		Assume.assumeTrue(destAccBeforeTransactionOpt.isPresent());
		Account destAccBeforeTransaction = destAccBeforeTransactionOpt.get();

		Optional<HttpResponse> responsePostOpt = executeTransfer(incorrectAccountNo, destAccountNo, amountToTransfer,
				"transfer description");
		Assume.assumeTrue(responsePostOpt.isPresent());
		HttpResponse responsePost = responsePostOpt.get();

		Optional<Account> destAccAfterTransactionOpt = getAccount(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo));
		Assume.assumeTrue(destAccAfterTransactionOpt.isPresent());
		Account destAccAfterTransaction = destAccAfterTransactionOpt.get();

		StatusResponse statusResponseActual = getStatusResponse(responsePost);
		StatusResponse StatusResponseExpected = createErrorStatusResponse();

		assertEquals(destAccBeforeTransaction.getBalance().intValue(), destAccAfterTransaction.getBalance().intValue());
		assertEquals(HttpStatus.BAD_REQUEST_400, responsePost.getStatusLine().getStatusCode());
		assertEquals(StatusResponseExpected, statusResponseActual);

		Optional<HttpResponse> srcCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(srcCustomerId),
				Math.toIntExact(incorrectAccountNo), Math.toIntExact(srcAccountOpertionId));
		Assume.assumeTrue(srcCustOperationGetRespOpt.isPresent());
		HttpResponse srcCustOperationGetResp = srcCustOperationGetRespOpt.get();

		StatusResponse srcStatusCustOperationResp = getStatusResponse(srcCustOperationGetResp);
		StatusResponse srcStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(srcAccountOpertionId) + ", account  id = "
						+ Math.toIntExact(incorrectAccountNo) + " and customer id = " + Math.toIntExact(srcCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(srcStatusCustOperationRespExpected, srcStatusCustOperationResp);

		Optional<HttpResponse> destCustOperationGetRespOpt = executeGetOperation(Math.toIntExact(destCustomerId),
				Math.toIntExact(destAccountNo), Math.toIntExact(incorrectAccountNo));
		Assume.assumeTrue(destCustOperationGetRespOpt.isPresent());
		HttpResponse destCustOperationGetResp = destCustOperationGetRespOpt.get();

		StatusResponse destStatusCustOperationResp = getStatusResponse(destCustOperationGetResp);
		StatusResponse destStatusCustOperationRespExpected = new StatusResponse(HttpStatus.NOT_FOUND_404,
				"Operation with id = " + Math.toIntExact(incorrectAccountNo) + ", account  id = "
						+ Math.toIntExact(destAccountNo) + " and customer id = " + Math.toIntExact(destCustomerId)
						+ " not exist");

		assertEquals(HttpStatus.NOT_FOUND_404, srcCustOperationGetResp.getStatusLine().getStatusCode());
		assertEquals(destStatusCustOperationRespExpected, destStatusCustOperationResp);

		stopServer();
	}

	private Optional<HttpResponse> executeTransfer(Long sourceAccountNo, Long destinationAccountNo, BigDecimal amount,
			String description) {
		try {
			CloseableHttpClient client = HttpClients.createDefault();

			HttpPost httpPost = new HttpPost("http://localhost:4567/api/v1/transfers");

			StringEntity transferCommand = new StringEntity(
					new Gson().toJson(new TransferCommand(sourceAccountNo, destinationAccountNo, amount, description)));

			httpPost.setEntity(transferCommand);
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

	private Optional<Account> getAccount(int idCustomer, int idAccount) {
		try {
			HttpClient client = HttpClients.createDefault();

			HttpGet httpGet = new HttpGet(
					"http://localhost:4567/api/v1/customers/" + idCustomer + "/accounts/" + idAccount);
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
