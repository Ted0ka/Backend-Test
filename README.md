# Backend Test
---
RESTful API for money transfers between accounts.


### Tech Stack
---
*  Java 8
*  Spark
*  Guice
*  H2 Database
*  Hibernate JPA2
*  JUnit

### Features
___
*  money transfer between accounts
*  money deposit on account
*  creating/deleting customers
*  creating/accounts accounts
*  transaction history

### API
---

```sh
http://localhost:4567/api/v1
```


| Method  | Endpoint |
| ------ | ------ |
| **POST** |  /deposit |
| **POST** |  /transfers |
| **POST** |  /customers |
| **GET** |  /customers |
| **GET** |  /customers/_**{idCustomer}**_ |
| **DELETE** |  /customers/_**{idCustomer}**_ |
| **PUT** |  /customers/_**{idCustomer}**_ |
| **GET** |  /customers/_**{idCustomer}**_/accounts |
| **POST** |  /customers/_**{idCustomer}**_/accounts |
| **GET** |  /customers/_**{idCustomer}**_/accounts/_**{idAccount}**_ |
| **DELETE** |  /customers/_**{idCustomer}**_/accounts/_**{idAccount}**_ |
| **GET** |  /customers/_**{idCustomer}**_/accounts/_**{idAccount}**_/balance |
| **GET** |  /customers/_**{idCustomer}**_/accounts/_**{idAccount}**_/operations |
| **GET** |  /customers/_**{idCustomer}**_/accounts/_**{idAccount}**_/operations/_**{idOperation}**_ |

### EXAMPLES
---

### Make transfer: 
```sh
[POST] http://localhost:4567/api/v1/customers/1
```
Body:
```json
{
    "sourceAccountNo": 1,
	"destinationAccountNo": 2,
	"amount": 1000,
	"description" : "Transfer no 1"
}
```
Response:
STATUS: 201 Created
```json
{
    "code": 201,
    "message": "Transfer done"
}
```


### Make deposit: 
```sh
[POST] http://localhost:4567/api/v1/deposit
```
Body:
```json
{
	"destinationAccountNo": 1,
	"amount": 10000,
	"description" : "Deposit description"
}
```

Response:
STATUS: 201 Created

```json
{
    "code": 201,
    "message": "Deposit done"
}
```

### Create customer: 
```sh
[POST] http://localhost:4567/api/v1/customers
```
Body:
```json
{
        "firstName": "Rupert",
        "lastName": "Bean",
        "street": "Victoria St",
        "city": "Bristol",
        "zip": "45-54",
        "phone": "687-593-447",
        "addAccountCommand": {
            "description": "Personal account",
             "amount": 3000
        }
    }
```

Response:
STATUS: 201 Created

```json
{
        "id": 2,
        "firstName": "Rupert",
        "lastName": "Bean",
        "street": "Victoria St",
        "city": "Bristol",
        "zip": "45-54",
        "phone": "687-593-447",
        "accountList": [
            {
                "id": 2,
                "description": "Personal account",
                "balance": 3000
            }
        ]
    }
```

### Create account: 
```sh
[POST] http://localhost:4567/api/v1/customers/1/accounts
```
Body:
```json
{
   "description": "New added account",
   "amount": 3000
}
```

Response:
STATUS: 201 Created

```json
{
    "id": 4,
    "description": "New added account",
    "balance": 3000
}
```