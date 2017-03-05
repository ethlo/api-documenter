package com.ethlo.api;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class Renderer
{
    private static final PebbleEngine engine = new PebbleEngine.Builder().build();
    private static final ObjectMapper mapper;
    
    static
    {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
    }
    
    public static void render(Map<String, Object> model, String tpl, Writer out) throws IOException
    {
        try
        {
            final PebbleTemplate compiledTemplate = engine.getTemplate(tpl);
            compiledTemplate.evaluate(out, model);
        }
        catch (PebbleException exc)
        {
            throw new IOException(exc.getMessage(), exc);
        }
    }

    public static void render(Map<String, Object> model, Writer out) throws IOException
    {
        mapper.writerWithDefaultPrettyPrinter().writeValue(out, model);
    }
}
