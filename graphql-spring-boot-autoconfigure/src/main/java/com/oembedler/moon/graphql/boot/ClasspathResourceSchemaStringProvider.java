package com.oembedler.moon.graphql.boot;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClasspathResourceSchemaStringProvider implements SchemaStringProvider {

    private ApplicationContext applicationContext;
    // @Value("${graphql.tools.schemaLocationPattern:**/*.graphqls}")
    private String schemaLocationPattern;

    public ClasspathResourceSchemaStringProvider(ApplicationContext applicationContext, String schemaLocationPattern) {
        this.applicationContext = applicationContext;
        if (schemaLocationPattern != null) {
            this.schemaLocationPattern = schemaLocationPattern;
        } else {
            this.schemaLocationPattern = "**/*.graphqls";
        }

    }

    @Override
    public List<String> schemaStrings() throws IOException {
        List<Resource> resources = new ArrayList<>();
        Arrays.stream(this.schemaLocationPattern.split(",")).forEach(pattern -> {
            try {
                resources.addAll(Arrays.asList(applicationContext.getResources("classpath*:" + pattern)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //  Resource[] resources = applicationContext.getResources("classpath*:" + schemaLocationPattern);
        if (resources.size() <= 0) {
            throw new IllegalStateException(
                    "No graphql schema files found on classpath with location pattern '"
                            + schemaLocationPattern
                            + "'.  Please add a graphql schema to the classpath or add a SchemaParser bean to your application context.");
        }

        return resources
                .stream()
                .map(this::readSchema)
                .collect(Collectors.toList());
    }

    private String readSchema(Resource resource) {
        StringWriter writer = new StringWriter();
        try (InputStream inputStream = resource.getInputStream()) {
            IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read graphql schema from resource " + resource, e);
        }
        return writer.toString();
    }

}
