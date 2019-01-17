package com.backend.dao;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public abstract class CrudDao<T, ID> implements Dao<T, ID> {
	protected Class<T> entityClass;
	protected EntityManagerFactory entityManagerFactory;

	public CrudDao(EntityManagerFactory entityManagerFactory, Class<T> entityClass) {
		super();
		this.entityManagerFactory = entityManagerFactory;
		this.entityClass = entityClass;
	}

	@Override
	public void persist(T t) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.persist(t);
		entityManager.getTransaction().commit();
		entityManager.close();
	}

	@Override
	public Optional<T> find(ID id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Optional<T> entity = Optional.ofNullable(entityManager.find(entityClass, id));
		entityManager.close();
		return entity;
	}

	@Override
	public List<T> findAll() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		List<T> entityList = entityManager.createQuery("SELECT a FROM " + entityClass.getName() + " a").getResultList();
		entityManager.close();
		return entityList;
	}

	@Override
	public boolean update(ID id, T t) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		Optional<T> entity = Optional.ofNullable(entityManager.find(entityClass, id));

		if (entity.isPresent()) {
			entityManager.getTransaction().begin();
			entityManager.merge(t);
			entityManager.flush();
			entityManager.getTransaction().commit();
			entityManager.close();

			return true;
		}

		entityManager.close();

		return false;
	}

	@Override
	public boolean delete(ID id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		Optional<T> entity = Optional.ofNullable(entityManager.find(entityClass, id));
		if (entity.isPresent()) {
			entityManager.getTransaction().begin();
			entityManager.remove(entity.get());
			entityManager.getTransaction().commit();
			entityManager.close();

			return true;
		}
		entityManager.close();
		return false;
	}
}
