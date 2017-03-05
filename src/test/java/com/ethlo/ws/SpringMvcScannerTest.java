package com.ethlo.ws;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ethlo.api.apt.ControllerScanner;
import com.ethlo.api.apt.JsonSchemaCreator;
import com.ethlo.api.apt.Result;
import com.ethlo.api.renderer.Renderer;
import com.fasterxml.jackson.databind.JsonNode;

public class SpringMvcScannerTest
{
    private final JsonSchemaCreator jsonSchemaCreator = new JsonSchemaCreator();
    
    @Test
    public void findMethods() throws IOException
    {
        final Result result = new ControllerScanner(RequestMapping.class).findMethods();
        final Map<String, Object> model = new LinkedHashMap<>();
        model.put("api", result.getClasses());
        final Map<String, JsonNode> modelSchemas = result
            .getModels()
            .stream()
            .collect(Collectors.toMap(c->c.getName(), c->jsonSchemaCreator.createSchema(c)));
        model.put("models", modelSchemas);
        
        try (final Writer out = new FileWriter("/tmp/test.html"))
        {
            Renderer.render(model, "oai.tpl.html", out);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        //ApiProcessor.mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, model);
    }
}