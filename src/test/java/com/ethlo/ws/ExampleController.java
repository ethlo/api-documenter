package com.ethlo.ws;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author mha
 */
@Controller
@RequestMapping("/system")
public class ExampleController
{
    @RequestMapping("/status")
    @Secured("ROLE_ADMIN")
    public String memoryStatus(ModelMap model)
    {
        return null;
    }
    
    @RequestMapping("/status/{section}")
    @Secured("ROLE_ADMIN")
    public String memoryStatus(@PathVariable("section-name") String sectionName)
    {
        return null;
    }
}