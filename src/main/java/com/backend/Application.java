package com.backend;

import static spark.Spark.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.*;

import org.eclipse.jetty.http.HttpStatus;

import com.backend.command.AddAccountCommand;
import com.backend.command.AddCustomerCommand;
import com.backend.command.DepositCommand;
import com.backend.command.TransferCommand;
import com.backend.command.UpdateCustomerCommand;
import com.backend.datacreator.DataCreator;
import com.backend.model.Account;
import com.backend.model.Customer;
import com.backend.model.Operation;
import com.backend.module.DaoModule;
import com.backend.module.DataCreatorModule;
import com.backend.module.ServiceModule;
import com.backend.module.ValidatorModule;
import com.backend.response.StatusResponse;
import com.backend.service.AccountService;
import com.backend.service.AccountServiceImpl;
import com.backend.service.CustomerService;
import com.backend.service.CustomerServiceImpl;
import com.backend.service.OperationService;
import com.backend.service.OperationServiceImpl;
import com.backend.service.TransactionService;
import com.backend.service.TransactionServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class Application {

	private static final Logger logger = Logger.getLogger(Application.class.getName());

	public static void main(String[] args) {
		logger.info("Spark application started");
		
		Injector injector = Guice.createInjector(new ServiceModule(), new DaoModule(), new ValidatorModule(), new DataCreatorModule());
	
		DataCreator dataCreator = injector.getInstance(DataCreator.class);
		dataCreator.createDataAndPersist();
		
		TransactionService transactionService = injector.getInstance(TransactionServiceImpl.class);
		AccountService accountService = injector.getInstance(AccountServiceImpl.class);
		CustomerService customerService = injector.getInstance(CustomerServiceImpl.class);
		OperationService operationService = injector.getInstance(OperationServiceImpl.class);

		path("/api/v1", () -> {
			post("/deposit", (request, response) -> {
				response.type("application/json");

				if (transactionService.deposit(new Gson().fromJson(request.body(), DepositCommand.class))) {
					response.status(HttpStatus.CREATED_201);
					return new Gson().toJson(new StatusResponse(HttpStatus.CREATED_201, "Deposit done"));
				}
				
				response.status(HttpStatus.BAD_REQUEST_400);
				return new Gson().toJson(new StatusResponse(HttpStatus.BAD_REQUEST_400, "An error occurred!"));
			});
			
			post("/transfers", (request, response) -> {
				response.type("application/json");
				if (transactionService.transfer(new Gson().fromJson(request.body(), TransferCommand.class))) {
					response.status(HttpStatus.CREATED_201);

					return new Gson().toJson(new StatusResponse(HttpStatus.CREATED_201, "Transfer done"));
				}

				response.status(HttpStatus.BAD_REQUEST_400);
				return new Gson().toJson(new StatusResponse(HttpStatus.BAD_REQUEST_400, "An error occurred!"));
			});

			path("/customers", () -> {
				post("", (request, response) -> {
					response.type("application/json");
					Optional<Customer> customerOpt = customerService
							.addCustomer(new Gson().fromJson(request.body(), AddCustomerCommand.class));

					if (customerOpt.isPresent()) {
						response.status(HttpStatus.OK_200);
						return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
								.toJson(customerOpt.get());
					}

					response.status(HttpStatus.BAD_REQUEST_400);
					return new Gson().toJson(new StatusResponse(HttpStatus.BAD_REQUEST_400, "An error occurred!"));
				});

				get("", (request, response) -> {
					response.type("application/json");
					List<Customer> customerList = customerService.getAllCustomers();

					if (customerList.isEmpty()) {
						response.status(HttpStatus.NOT_FOUND_404);
						return new Gson().toJson(new StatusResponse(HttpStatus.NOT_FOUND_404, "An error occurred!"));
					}
					
					response.status(HttpStatus.OK_200);
					return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(customerList);
				});

				get("/:idCustomer", (request, response) -> {
					response.type("application/json");

					Optional<Customer> customerOpt = customerService
							.getCustomer(Long.valueOf(request.params(":idCustomer")));
					
					if (customerOpt.isPresent()) {
						response.status(HttpStatus.OK_200);

						return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
								.toJson(customerOpt.get());
					}

					response.status(HttpStatus.NOT_FOUND_404);
					return new Gson().toJson(new StatusResponse(HttpStatus.NOT_FOUND_404,
							"Customer with id = " + Long.valueOf(request.params(":idCustomer")) + " not exist"));
				});

				delete("/:idCustomer", (request, response) -> {
					response.type("application/json");

					if (customerService.deleteCustomer(Long.valueOf(request.params(":idCustomer")))) {
						response.status(HttpStatus.OK_200);
						return new Gson().toJson(new StatusResponse(HttpStatus.OK_200,
								"Customer with id = " + request.params(":idCustomer") + " deleted"));
					}

					response.status(HttpStatus.NOT_FOUND_404);
					return new Gson().toJson(new StatusResponse(HttpStatus.NOT_FOUND_404,
							"Customer with id = " + Long.valueOf(request.params(":idCustomer")) + " not exist"));
				});

				put("/:idCustomer", (request, response) -> {
					response.type("application/json");

					UpdateCustomerCommand updateCustomerCommand = new Gson().fromJson(request.body(),
							UpdateCustomerCommand.class);

					Optional<Customer> customerOpt = customerService
							.updateCustomer(Long.valueOf(request.params(":idCustomer")), updateCustomerCommand);

					if (customerOpt.isPresent()) {
						response.status(HttpStatus.OK_200);
						return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
								.toJson(customerOpt.get());
					}

					response.status(HttpStatus.NOT_FOUND_404);
					return new Gson().toJson(new StatusResponse(HttpStatus.NOT_FOUND_404, "Customer with id = "
							+ Long.valueOf(request.params(":idCustomer")) + " not exist or data is invalid"));
				});

				get("/:idCustomer/accounts", (request, response) -> {
					response.type("application/json");

					Optional<Customer> customerOpt = customerService
							.getCustomer(Long.valueOf(request.params(":idCustomer")));

					if (customerOpt.isPresent()) {
						response.status(HttpStatus.OK_200);
						return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
								.toJson(customerOpt.get().getAccountList());
					}

					response.status(HttpStatus.NOT_FOUND_404);
					return new Gson().toJson(new StatusResponse(HttpStatus.NOT_FOUND_404,
							"Customer with id = " + request.params(":idCustomer") + " not exist"));
				});

				post("/:idCustomer/accounts", (request, response) -> {
					response.type("application/json");
					Optional<Account> accountOpt = accountService.addAccount(
							Long.valueOf(request.params(":idCustomer")),
							new Gson().fromJson(request.body(), AddAccountCommand.class));

					if (accountOpt.isPresent()) {
						response.status(HttpStatus.CREATED_201);
						return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
								.toJson(accountOpt.get());
					}

					response.status(HttpStatus.BAD_REQUEST_400);
					return new Gson().toJson(new StatusResponse(HttpStatus.BAD_REQUEST_400,
							"Customer with id = " + request.params(":idCustomer") + " not exist or data is invalid"));
				});

				get("/:idCustomer/accounts/:idAccount", (request, response) -> {
					response.type("application/json");

					Optional<Account> accountOpt = accountService.getCustomerAccount(
							Long.valueOf(request.params(":idCustomer")), Long.valueOf(request.params(":idAccount")));

					if (accountOpt.isPresent()) {
						response.status(HttpStatus.OK_200);
						return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
								.toJson(accountOpt.get());
					}

					response.status(HttpStatus.NOT_FOUND_404);
					return new Gson().toJson(new StatusResponse(HttpStatus.NOT_FOUND_404, "Account with id = " + request.params(":idAccount")
							+ " and customer id = " + request.params(":idCustomer") + " not exist"));
				});

				delete("/:idCustomer/accounts/:idAccount", (request, response) -> {
					response.type("application/json");
					if (accountService.deleteAccount(Long.valueOf(request.params(":idCustomer")),
							Long.valueOf(request.params(":idAccount")))) {
						
						response.status(HttpStatus.OK_200);
						return new Gson().toJson(
								new StatusResponse(HttpStatus.OK_200, "Account with id = " + request.params(":idAccount")
										+ " and customer id = " + request.params(":idCustomer") + " deleted"));
					}
					response.status(HttpStatus.NOT_FOUND_404);
					return new Gson().toJson(new StatusResponse(HttpStatus.NOT_FOUND_404, "Account with id = " + request.params(":idAccount")
							+ " and customer id = " + request.params(":idCustomer") + " not exist"));
				});

				get("/:idCustomer/accounts/:idAccount/balances", (request, response) -> {
					response.type("application/json");

					Optional<BigDecimal> balanceOpt = accountService.getBalanceOfCustomerAccount(
							Long.valueOf(request.params(":idCustomer")), Long.valueOf(request.params(":idAccount")));

					if (balanceOpt.isPresent()) {
						response.status(HttpStatus.OK_200);
						return new Gson().toJson(balanceOpt.get());
					}

					response.status(HttpStatus.NOT_FOUND_404);
					return new Gson().toJson(new StatusResponse(HttpStatus.NOT_FOUND_404,
							"Account with id = " + request.params(":idAccount") + " and customer id = "
									+ request.params(":idCustomer") + " not exist"));
				});

				get("/:idCustomer/accounts/:idAccount/operations", (request, response) -> {
					response.type("application/json");

					List<Operation> operationList = operationService.getAllCustomerOperations(
							Long.valueOf(request.params(":idCustomer")), Long.valueOf(request.params(":idAccount")));

					if (operationList.isEmpty()) {
						response.status(HttpStatus.NOT_FOUND_404);
						return new Gson().toJson(new StatusResponse(HttpStatus.NOT_FOUND_404,
								"Account with id = " + request.params(":idAccount") + " and customer id = "
										+ request.params(":idCustomer") + " not exist"));
					}

					response.status(HttpStatus.OK_200);
					return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(operationList);
				});

				get("/:idCustomer/accounts/:idAccount/operations/:idOperation", (request, response) -> {
					response.type("application/json");

					Optional<Operation> operationOpt = operationService.getCustomerOperation(
							Long.valueOf(request.params(":idCustomer")), Long.valueOf(request.params(":idAccount")),
							Long.valueOf(request.params(":idOperation")));

					if (operationOpt.isPresent()) {
						
						response.status(HttpStatus.OK_200);
						return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
								.toJson(operationOpt.get());
					}

					response.status(HttpStatus.NOT_FOUND_404);
					return new Gson().toJson(new StatusResponse(HttpStatus.NOT_FOUND_404,
							"Operation with id = " + request.params(":idOperation") + ", account  id = "
									+ request.params(":idAccount") + " and customer id = "
									+ request.params(":idCustomer") + " not exist"));
				});
			});
		});
	}
}
