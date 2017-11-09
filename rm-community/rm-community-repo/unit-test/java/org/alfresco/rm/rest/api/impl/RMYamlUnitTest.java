/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rm.rest.api.impl;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

/**
 * Unit Test class for RM Yaml file validation.
 *
 * @author Sara Aspery
 * @since 2.6
 *
 */
public class RMYamlUnitTest extends BaseUnitTest
{
    private static String SWAGGER_2_SCHEMA = "C:\\Users\\saspery\\Documents\\iWeek\\schema.json";
    private static String OPEN_API_SPECIFICATION = "2.0";
    
    @Test
    public void validateYamlFile() throws Exception
    {
        final JsonSchema swaggerSchema = getSwaggerSchema(SWAGGER_2_SCHEMA);
        
        final Set<String> yamlFileNames = getYamlFileNames();
        assertFalse(yamlFileNames.isEmpty());
        
        for (String yamlFilePath : yamlFileNames)
        {
            // check the yaml file is valid against Swagger JSON schema
            assertTrue(validateYamlFile(yamlFilePath, swaggerSchema));

            // check we can read the swagger object for the swagger version
            Swagger swagger = new SwaggerParser().read(yamlFilePath);
            assertEquals(swagger.getSwagger(), OPEN_API_SPECIFICATION);
        }
    }
    
    private JsonSchema getSwaggerSchema(final String filePath) throws IOException, ProcessingException
    {
        final String swaggerSchema = new String(Files.readAllBytes(Paths.get(filePath)));
        final JsonNode schemaNode = JsonLoader.fromString(swaggerSchema);
        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        return factory.getJsonSchema(schemaNode);
    }

    /**
     * Helper method to return a list of Yaml filenames
     */
    private Set<String> getYamlFileNames()
    {
        /*
        String basePackageName = "org.alfresco.module.org_alfresco_module_rm";
        Reflections reflections = new Reflections(basePackageName, new ResourcesScanner());
        Set<String> yamlFileNames = reflections.getResources(Pattern.compile("\\*"));
        
        String s = this.getClass().getClassLoader().getResource("").getPath();
        */
        Set<String> yamlFileNames = new HashSet<>();
        yamlFileNames.add("C:\\dev5\\records-management\\rm-community\\rm-community-rest-api-explorer\\src\\main\\webapp\\definitions\\gs-core-api.yaml");
        yamlFileNames.add("C:\\dev5\\records-management\\rm-enterprise\\rm-enterprise-rest-api-explorer\\src\\main\\webapp\\definitions\\ig-classification-api.yaml");
        //yamlFileNames.add("C:\\dev5\\records-management\\rm-community\\rm-community-rest-api-explorer\\src\\main\\webapp\\definitions\\gs-core-api02.yaml");
        
        return yamlFileNames;
    }

    private boolean validateYamlFile(final String swaggerFilePath, final JsonSchema jsonSchema) throws IOException, ProcessingException
    {
        // Get yaml string and convert to JSON string
        final String yaml = new String(Files.readAllBytes(Paths.get(swaggerFilePath)));
        final ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        final Object obj = yamlReader.readValue(yaml, Object.class);
        final ObjectMapper jsonWriter = new ObjectMapper();
        final String yamlAsJson = jsonWriter.writeValueAsString(obj);

        return validateJSON(yamlAsJson, jsonSchema);
    }
    
    /**
     * Helper method to validate JSON string against JSON schema
     */
    private boolean validateJSON(final String jsonData, final JsonSchema schema) throws IOException, ProcessingException
    {
        final JsonNode dataNode = JsonLoader.fromString(jsonData);
        final ProcessingReport report = schema.validate(dataNode);
        return report.isSuccess();
    }
}

