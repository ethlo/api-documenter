package com.ethlo.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.v1.JsonSchemaFactory;
import com.github.reinert.jjschema.v1.JsonSchemaV4Factory;

public class JsonSchemaCreator
{
    private final JsonSchemaFactory schemaFactory;
    
    public JsonSchemaCreator()
    {
        schemaFactory = new JsonSchemaV4Factory();
        schemaFactory.setAutoPutDollarSchema(true);
    }

    public JsonNode createSchema(Class<?> clazz)
    {
        return schemaFactory.createSchema(clazz);
    }
}
