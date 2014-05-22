package com.ethlo.api.apt;

/**
 * 
 * @author Morten Haraldsen
 */
public class TypeDescriptor
{
    private String type;

    public TypeDescriptor(String type)
    {
        this.type = type;
    }
    
    public String getType()
    {
        return this.type;
    }
    
    @Override
    public String toString()
    {
        return type;
    }
}
