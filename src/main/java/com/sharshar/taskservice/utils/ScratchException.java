package com.sharshar.taskservice.utils;

/**
 * General Exception for this application
 * Created by lsharshar on 3/19/2018.
 */
public class ScratchException extends Exception {
	public ScratchException(String val) {
		super(val);
	}

	public ScratchException(String message, Throwable cause) {
		super(message, cause);
	}
}
