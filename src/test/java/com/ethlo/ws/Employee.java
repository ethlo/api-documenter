package com.ethlo.ws;

import javax.validation.constraints.NotNull;

import com.github.reinert.jjschema.Attributes;

public class Employee extends Person
{
	/**
	 * The salary of of the user
	 */
	@NotNull
	@Attributes(required=true)
	private int salary;

    public int getSalary()
    {
        return salary;
    }

    public void setSalary(int salary)
    {
        this.salary = salary;
    }
}
