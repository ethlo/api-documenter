package com.ethlo.ws;

/**
 * 
 * @author mha
 * 
 */
public class ResponseObject
{
    private String name;
    private int age;
    private Person helper;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }
    
    public Person getHelper()
    {
        return this.helper;
    }
}
