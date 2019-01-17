package com.backend.validator;

public interface Validator<T> {
	boolean validate(T t);
}
