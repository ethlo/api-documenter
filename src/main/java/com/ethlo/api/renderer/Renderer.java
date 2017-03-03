package com.ethlo.api.renderer;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class Renderer
{
    private final static PebbleEngine engine = new PebbleEngine.Builder().build();
    
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
}
