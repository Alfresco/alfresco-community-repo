package org.alfresco.rest.core.swagger;

import org.alfresco.utility.exception.TestConfigurationException;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

/**
 * This will handle the Swagger YAML file
 * It will contain all the models, request or any other properties needed for out generator
 * 
 * @author Paul Brodner
 */
public class SwaggerYamlParser
{
    private Swagger swagger;
    private String swaggerFilePath;

    public SwaggerYamlParser(String swaggerFilePath) throws TestConfigurationException
    {
        if (swaggerFilePath == null)
            throw new TestConfigurationException("'swaggerFilePath' not defined. Please update your pom.xml file with all '<swagger-file-location.yml>'");

        this.swaggerFilePath = swaggerFilePath;
        this.swagger = new SwaggerParser().read(this.swaggerFilePath);

    }

    public void computeCoverage()
    {
        new SwaggerPaths(swagger, this.swaggerFilePath).computeCoverage();
    }

    public void generateMissingModules()
    {
        String modelParamValue = System.getProperty("models");
        
        SwaggerDefinitions swaggerDefinitions = new SwaggerDefinitions(swagger);
        
        System.out.println(Generator.line);
        System.out.println("Using SWAGGER FILE: " + this.swaggerFilePath);
        System.out.println(Generator.line);
        /*
         * multiple models, separated by comma: 'mvn exec:java -Dmodels=a,b,d'
         */
        if (modelParamValue.contains(","))
        {
            String[] models = {};
            models = modelParamValue.split(",");

            for (int i = 0; i < models.length; i++)
            {
                // generate model
                swaggerDefinitions.generateDefinition(models[i]);                
            }
        }
        else
        {
            /*
             * if no value is added after models "mvn exec:java -Dmodels"
             * then we assume that we want to generate all models
             */
            if (modelParamValue.equals("true"))
            {
                swaggerDefinitions.generateMissingDefinitions();
            }
            /*
             * there is just one model passed as value "mvn exec:java -Dmodels=a"
             * so only model "a" is generated locally
             */
            else
            {
                swaggerDefinitions.generateDefinition(modelParamValue);
            }
        }
        
    }

}
