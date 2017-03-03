package com.ethlo.api.apt;

import java.util.Map;
import java.util.TreeMap;

import com.mitchellbosecke.pebble.utils.StringUtils;

/**
 * 
 * @author Morten Haraldsen
 */
public class TypeDescriptor
{
    private final String type;
    private Map<String, String> subTypes = new TreeMap<>();

    public TypeDescriptor(String type)
    {
        this.type = type;
    }
    
    public String getType()
    {
        return this.type;
    }
    
    public Map<String, String> getSubTypes()
    {
        return this.subTypes;
    }
    
    @Override
    public String toString()
    {
        return type + (!subTypes.isEmpty() ? " ->" + StringUtils.toString(subTypes) : "");
    }
}
