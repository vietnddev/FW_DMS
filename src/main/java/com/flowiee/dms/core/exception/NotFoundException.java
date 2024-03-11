package com.flowiee.dms.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	public NotFoundException(String message) {
		super(message);
	}
}