package com.ethlo.api;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestMapping;

import com.ethlo.api.model.Result;
import com.fasterxml.jackson.databind.JsonNode;

public class ApiBuilder
{
    private final JsonSchemaCreator jsonSchemaCreator = new JsonSchemaCreator();
    private Configuration cfg;
    
    public ApiBuilder(Configuration cfg)
    {
        this.cfg = cfg;
    }

    public void build()
    {
        final Result result = new ControllerScanner(RequestMapping.class).findMethods();
        final Map<String, Object> model = new LinkedHashMap<>();
        model.put("api", result.getClasses());
        final Map<String, JsonNode> modelSchemas = result
            .getModels()
            .stream()
            .collect(Collectors.toMap(c->c.getName(), c->jsonSchemaCreator.createSchema(c)));
        model.put("models", modelSchemas);
        
        try (final Writer out = new FileWriter(cfg.getTargetRenderedFile()))
        {
            Renderer.render(model, cfg.getTemplateFile(), out);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        try (final Writer out = new FileWriter(cfg.getTargetModelFile()))
        {
            Renderer.render(model, out);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
