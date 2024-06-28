package com.flowiee.dms.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Data
public class ResourceNotFoundException extends RuntimeException {
	private boolean redirectErrorUI;

	@Serial
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message, boolean redirectErrorUI) {
		super(message);
		this.redirectErrorUI = redirectErrorUI;
	}

	public ResourceNotFoundException(String message, Throwable cause, boolean redirectErrorUI) {
		super(message, cause);
		this.redirectErrorUI = redirectErrorUI;
	}
}