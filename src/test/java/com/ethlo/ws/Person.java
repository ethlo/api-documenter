package com.ethlo.ws;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Person
{
	/**
	 * The primary ID of the user
	 */
	@NotNull
	private Integer id;
	
	/**
	 * The username of this user
	 */
	@NotNull
	@Size(min=4, max=32)
	private String username;
}
