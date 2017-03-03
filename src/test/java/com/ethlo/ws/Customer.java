package com.ethlo.ws;

import javax.validation.constraints.NotNull;

public class Customer extends Person
{
	/**
	 * The companyName of the person
	 */
	@NotNull
	private String companyName;

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }
}
