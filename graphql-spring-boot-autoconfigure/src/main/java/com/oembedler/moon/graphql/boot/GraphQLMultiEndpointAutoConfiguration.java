package com.oembedler.moon.graphql.boot;

import com.coxautodev.graphql.tools.*;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.servlet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.coxautodev.graphql.tools.SchemaParserOptions.newOptions;

@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties({GraphQLMultiEndpointProperties.class, GraphQLServletProperties.class})
@AutoConfigureAfter(GraphQLWebAutoConfiguration.class)
public class GraphQLMultiEndpointAutoConfiguration {
    @Autowired
    GraphQLMultiEndpointProperties multiEndpointProperties;
    @Autowired(required = false)
    SchemaParserDictionary dictionary;
    @Autowired
    DefaultListableBeanFactory registry;
    @Autowired
    List<GraphQLResolver<?>> resolvers;
    @Autowired
    PerFieldObjectMapperProvider perFieldObjectMapperProvider;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    GraphQLServletProperties graphQLServletProperties;

    @Autowired(required = false)
    private GraphQLScalarType[] scalars;

    @Autowired(required = false)
    private SchemaParserOptions options;

    @Autowired(required = false)
    private GraphQLRootObjectBuilder graphQLRootObjectBuilder;

    @Autowired(required = false)
    private GraphQLContextBuilder contextBuilder;

    @Autowired(required = false)
    private List<GraphQLServletListener> listeners;

    @Autowired
    GraphQLQueryInvoker queryInvoker;
    @Autowired
    GraphQLObjectMapper graphQLObjectMapper;

    @Value("${graphql.tools.introspectionEnabled:true}")
    private boolean introspectionEnabled;

    @Bean
    ServletRegistrationBean<AbstractGraphQLHttpServlet> servletRegistrationBean() {
        return servletRegistrationBeans().get(0);
    }

    List<ServletRegistrationBean<AbstractGraphQLHttpServlet>> servletRegistrationBeans() {
        List<ServletRegistrationBean<AbstractGraphQLHttpServlet>> registrationBeans = new ArrayList<>();
        if (multiEndpointProperties != null && multiEndpointProperties.getMultiEndpoints() != null) {
            multiEndpointProperties.getMultiEndpoints().forEach((name, endpoint) -> {
                try {
                    SchemaParser schemaParser = schemaParser(new ClasspathResourceSchemaStringProvider(applicationContext, endpoint.getSchemaLocationPattern()));
                    GraphQLSchema graphQLSchema = graphQLSchema(schemaParser);
                    GraphQLSchemaProvider graphQLSchemaProvider = graphQLSchemaProvider(graphQLSchema);
                    GraphQLInvocationInputFactory invocationInputFactory = invocationInputFactory(graphQLSchemaProvider);
                    SimpleGraphQLHttpServlet servlet = graphQLHttpServlet(invocationInputFactory);
                    String mapping = endpoint.getEndpoint().endsWith("/") ? endpoint.getEndpoint() + "*" : endpoint.getEndpoint() + "/*";
                    ServletRegistrationBean<AbstractGraphQLHttpServlet> registrationBean = new ServletRegistrationBean<>(servlet, mapping);
                    registrationBean.setMultipartConfig(new MultipartConfigElement(""));
                    registrationBean.setName(name);
                    registrationBeans.add(registrationBean);
                    registry.registerBeanDefinition(name, new RootBeanDefinition(ServletRegistrationBean.class, () -> registrationBean));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return registrationBeans;
    }

    private SchemaParser schemaParser(SchemaStringProvider schemaStringProvider) throws IOException {
        SchemaParserBuilder builder = this.dictionary != null ? new SchemaParserBuilder(this.dictionary) : new SchemaParserBuilder();
        List<String> schemaStrings = schemaStringProvider.schemaStrings();
        schemaStrings.forEach(builder::schemaString);
        if (this.scalars != null) {
            builder.scalars(scalars);
        }

        if (this.options != null) {
            builder.options(options);
        } else if (perFieldObjectMapperProvider != null) {
            final SchemaParserOptions.Builder optionsBuilder =
                    newOptions().objectMapperProvider(this.perFieldObjectMapperProvider);
            optionsBuilder.introspectionEnabled(this.introspectionEnabled);
            builder.options(optionsBuilder.build());
        }

        return builder
                .resolvers(resolvers)
                .build();
    }

    private GraphQLSchema graphQLSchema(SchemaParser schemaParser) {
        return schemaParser.makeExecutableSchema();
    }

    private GraphQLSchemaProvider graphQLSchemaProvider(GraphQLSchema schema) {
        return new DefaultGraphQLSchemaProvider(schema);
    }

    public GraphQLInvocationInputFactory invocationInputFactory(GraphQLSchemaProvider schemaProvider) {
        GraphQLInvocationInputFactory.Builder builder = GraphQLInvocationInputFactory.newBuilder(schemaProvider);
        if (graphQLRootObjectBuilder != null) {
            builder.withGraphQLRootObjectBuilder(graphQLRootObjectBuilder);
        }
        if (contextBuilder != null) {
            builder.withGraphQLContextBuilder(contextBuilder);
        }
        return builder.build();
    }

    public SimpleGraphQLHttpServlet graphQLHttpServlet(GraphQLInvocationInputFactory invocationInputFactory) {
        return SimpleGraphQLHttpServlet.newBuilder(invocationInputFactory)
                .withQueryInvoker(queryInvoker)
                .withObjectMapper(graphQLObjectMapper)
                .withListeners(listeners)
                .withAsyncServletMode(graphQLServletProperties.isAsyncModeEnabled())
                .build();
    }

    public void afterPropertiesSet() throws Exception {
        //   servletRegistrationBeans();
    }


}
