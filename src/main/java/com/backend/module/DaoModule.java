package com.backend.module;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.backend.dao.*;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DaoModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(AccountDao.class).to(AccountDaoImpl.class);
		bind(CustomerDao.class).to(CustomerDaoImpl.class);
		bind(OperationDao.class).to(OperationDaoImpl.class);
		bind(TransactionDao.class).to(TransactionDaoImpl.class);
	}
	
	@Provides @Singleton
	EntityManagerFactory getEntityManagerFactory() {
		return Persistence.createEntityManagerFactory("database");
	}
}
