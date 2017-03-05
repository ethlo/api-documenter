package com.ethlo.api.apt;

import java.util.Set;
import java.util.TreeSet;

import com.mitchellbosecke.pebble.utils.StringUtils;

/**
 * 
 * @author Morten Haraldsen
 */
public class TypeDescriptor
{
    private final String type;
    private final Set<String> subTypes;

    public TypeDescriptor(String type, Set<String> subTypes)
    {
        this.type = type;
        this.subTypes = subTypes;
    }
    
    public TypeDescriptor(String type)
    {
        this(type, new TreeSet<>());
    }

    public String getType()
    {
        return this.type;
    }
    
    public Set<String> getSubTypes()
    {
        return this.subTypes;
    }
    
    @Override
    public String toString()
    {
        return type + (subTypes != null && !subTypes.isEmpty() ? " -> " + StringUtils.toString(subTypes) : "");
    }
}
