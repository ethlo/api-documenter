package com.ethlo.ws;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.github.reinert.jjschema.Attributes;

@JsonSubTypes({@Type(name="customer", value=Customer.class), @Type(name="employee", value=Employee.class)})
public abstract class Person
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
	@Attributes(required=true, minimum=4, maximum=32)
	private String username;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}
