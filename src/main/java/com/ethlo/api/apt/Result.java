package com.ethlo.api.apt;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Result
{
    private Map<String, ClassDescriptor> api = new TreeMap<>();
    private Set<Class<?>> models = new LinkedHashSet<>();

    public Result(Map<String, ClassDescriptor> api, Set<Class<?>> models)
    {
        this.api = api;
        models.remove(null);
        this.models = models;
    }
    
    public Set<Class<?>> getModels()
    {
        return models;
    }

    public Map<String, ClassDescriptor> getClasses()
    {
        return this.api;
    }

    public ClassDescriptor addClass(ClassDescriptor candidate)
    {
        return this.api.put(candidate.getPackageName() + "." + candidate.getName(), candidate);
    }
}