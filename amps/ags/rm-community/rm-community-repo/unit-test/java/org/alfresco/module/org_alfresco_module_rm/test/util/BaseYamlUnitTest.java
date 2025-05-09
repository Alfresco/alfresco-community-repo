/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Base class for unit tests for Yaml files.
 *
 * @author Sara Aspery
 * @since 2.6
 */
public class BaseYamlUnitTest
{
    private static final String SWAGGER_2_SCHEMA_LOCATION = "/rest/schema.json";
    private static final String OPEN_API_SPECIFICATION = "2.0";

    /**
     * Helper method to obtain path names for all yaml files found on the given path
     */
    protected Set<String> getYamlFilesList(String pathName) throws Exception
    {
        Set<String> yamlFilePathNames = new HashSet<>();
        File directory = new File(pathName);
        Collection<File> yamlFiles = FileUtils.listFiles(directory, new WildcardFileFilter("*.yaml"), null);
        for (File file : yamlFiles) {
            yamlFilePathNames.add(file.getCanonicalPath());
        }
        return yamlFilePathNames;
    }

    /**
     * Helper method to validate that all given yaml files are valid readable Swagger format
     */
    protected void validateYamlFiles(final Set<String> yamlFileNames) throws ValidationException, IOException
    {
        assertFalse("Expected at least 1 yaml file to validate", yamlFileNames.isEmpty());

        final JsonSchema swaggerSchema = getSwaggerSchema();
        assertNotNull("Failed to obtain the Swagger schema", swaggerSchema);
        
        for (String yamlFilePath : yamlFileNames)
        {
            try
            {
                // check the yaml file is valid against Swagger JSON schema
                assertTrue("Yaml file is not valid Swagger " + OPEN_API_SPECIFICATION + ": " + yamlFilePath, 
                        validateYamlFile(yamlFilePath, swaggerSchema));

                // check can read the swagger object to obtain the swagger version
                Swagger swagger = new SwaggerParser().read(yamlFilePath);
                assertEquals("Failed to obtain Swagger version from yaml file " + yamlFilePath, 
                        swagger.getSwagger(), OPEN_API_SPECIFICATION);
            }
            catch (ValidationException ex)
            {
                // ensure the yaml filename is included in the message
                String context = String.format(yamlFilePath + ": %n" + ex.getMessage());
                throw new ValidationException(context) ;
            }
        }
    }

    /**
     * Helper method to read in the Swagger JSON schema file
     */
    private JsonSchema getSwaggerSchema() throws IOException
    {
        JsonSchema swaggerSchema = null;
        final InputStream in = this.getClass().getResourceAsStream(SWAGGER_2_SCHEMA_LOCATION);
        if (in != null)
        {
            final String swaggerSchemaAsString = IOUtils.toString(in);
            final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V4);
            swaggerSchema = factory.getSchema(swaggerSchemaAsString);
        }
        return swaggerSchema;
    }

    /**
     * Helper method to validate Yaml file against JSON schema
     */
    private boolean validateYamlFile(final String yamlFilePath, final JsonSchema jsonSchema) throws IOException, ValidationException
    {
        // Get yaml file as a string
        final String yaml = new String(Files.readAllBytes(Paths.get(yamlFilePath)));

        // Convert yaml string to JSON string
        final ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        final Object obj = yamlReader.readValue(yaml, Object.class);
        final ObjectMapper jsonWriter = new ObjectMapper();
        final String yamlAsJsonString = jsonWriter.writeValueAsString(obj);
        
        return validateJSON(yamlAsJsonString, jsonSchema);
    }

    /**
     * Helper method to validate JSON string against JSON schema
     */
    private boolean validateJSON(final String jsonData, final JsonSchema schema) throws IOException, ValidationException
    {
        final JsonNode dataNode = new ObjectMapper().readTree(jsonData);

        final Iterator<ValidationMessage> errors = schema.validate(dataNode).iterator();
        if (!errors.hasNext()) return true;

        final ValidationMessage errorMessage = errors.next();
        throw new ValidationException(errorMessage.toString());
    }

    private static class ValidationException extends Exception
    {
        public ValidationException(String message)
        {
            super(message);
        }
    }
}