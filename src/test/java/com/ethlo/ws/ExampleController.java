package com.ethlo.ws;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        final MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        final MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
        model.addAttribute("heap", heap);
        model.addAttribute("nonHeap", nonHeap);
        final List<MemoryPoolMXBean> poolBeans = ManagementFactory.getMemoryPoolMXBeans();
        model.addAttribute("poolBeans", poolBeans);
        return "system/memstatus";
    }
}