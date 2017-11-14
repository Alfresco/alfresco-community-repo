package org.alfresco.module.org_alfresco_module_rm.test.util;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;


/**
 * Unit tests for Yaml files.
 *
 * @author Sara Aspery
 * @since 2.6
 */
public class BaseYamlUnitTest
{
    private static String SWAGGER_2_SCHEMA_LOCATION = "/rest/schema.json";
    private static String OPEN_API_SPECIFICATION = "2.0";

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

    protected void validateYamlFiles(final Set<String> yamlFileNames) throws Exception
    {
        assertFalse("Expected at least 1 yaml file to validate", yamlFileNames.isEmpty());

        final JsonSchema swaggerSchema = getSwaggerSchema(SWAGGER_2_SCHEMA_LOCATION);
        assertNotNull("Failed to obtain the Swagger schema", swaggerSchema);
        
        for (String yamlFilePath : yamlFileNames)
        {
            // check the yaml file is valid against Swagger JSON schema
            assertTrue("Yaml file is not a valid Swagger file", validateYamlFile(yamlFilePath, swaggerSchema));

            // check we can read the swagger object for the swagger version
            Swagger swagger = new SwaggerParser().read(yamlFilePath);
            assertEquals("Failed to obtain Swagger version from yaml file", swagger.getSwagger(), OPEN_API_SPECIFICATION);
        }
    }

    private JsonSchema getSwaggerSchema(final String schemaLocation) throws IOException, ProcessingException
    {
        JsonSchema swaggerSchema = null;
        final InputStream in = this.getClass().getResourceAsStream(schemaLocation);
        if (in != null)
        {
            final String swaggerSchemaAsString = IOUtils.toString(in);
            final JsonNode schemaNode = JsonLoader.fromString(swaggerSchemaAsString);
            final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            swaggerSchema = factory.getJsonSchema(schemaNode);
        }
        return swaggerSchema;
    }

    private boolean validateYamlFile(final String yamlFilePath, final JsonSchema jsonSchema) throws IOException, ProcessingException
    {
        // Get yaml string and convert to JSON string
        final String yaml = new String(Files.readAllBytes(Paths.get(yamlFilePath)));
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
