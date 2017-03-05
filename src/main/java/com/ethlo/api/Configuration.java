package com.ethlo.api;

import java.util.Set;
import java.util.TreeSet;

public class Configuration
{
    private Set<String> basePackages;
    private Set<String> excludeModels;;
    private String targetModelFile;
    private String templateFile;
    private String targetRenderedFile;

    public Set<String> getBasePackages()
    {
        return basePackages;
    }

    public Set<String> getExcludeModels()
    {
        return excludeModels;
    }

    public String getTargetModelFile()
    {
        return targetModelFile;
    }

    public String getTemplateFile()
    {
        return templateFile;
    }

    public String getTargetRenderedFile()
    {
        return targetRenderedFile;
    }

    public static class Builder
    {
        private Set<String> basePackages = null;
        private Set<String> excludeModels = new TreeSet<>();
        private String targetModelFile = "api.json";
        private String templateFile = "default.tpl.html";
        private String targetRenderedFile = "api.html";


        public Builder basePackages(Set<String> basePackages)
        {
            this.basePackages = basePackages;
            return this;
        }

        public Builder excludeModels(Set<String> excludeModels)
        {
            this.excludeModels = excludeModels;
            return this;
        }

        public Builder targetModelFile(String targetModelFile)
        {
            this.targetModelFile = targetModelFile;
            return this;
        }

        public Builder templateFile(String templateFile)
        {
            this.templateFile = templateFile;
            return this;
        }

        public Builder targetRenderedFile(String targetRenderedFile)
        {
            this.targetRenderedFile = targetRenderedFile;
            return this;
        }

        public Configuration build()
        {
            return new Configuration(this);
        }
    }

    private Configuration(Builder builder)
    {
        this.basePackages = builder.basePackages;
        this.excludeModels = builder.excludeModels;
        this.targetModelFile = builder.targetModelFile;
        this.templateFile = builder.templateFile;
        this.targetRenderedFile = builder.targetRenderedFile;
    }
}