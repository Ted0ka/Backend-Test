package com.backend.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<T,ID> {
	void persist(T entity);
	Optional<T> find(ID id);
    List<T>	findAll();
    boolean update(ID id, T entity);
    boolean delete(ID id);
}
