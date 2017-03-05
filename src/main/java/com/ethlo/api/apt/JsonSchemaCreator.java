package com.ethlo.api.apt;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.reinert.jjschema.v1.JsonSchemaFactory;
import com.github.reinert.jjschema.v1.JsonSchemaV4Factory;

public class JsonSchemaCreator
{
    private final JsonSchemaFactory schemaFactory;
    private final ObjectMapper mapper;
    
    public JsonSchemaCreator()
    {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        mapper.setVisibility(mapper.getSerializationConfig()
            .getDefaultVisibilityChecker()
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
            .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
            .withGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE));
        
        schemaFactory = new JsonSchemaV4Factory();
        schemaFactory.setAutoPutDollarSchema(true);
    }

    public JsonNode createSchema(Class<?> clazz)
    {
        return schemaFactory.createSchema(clazz);
    }
}
