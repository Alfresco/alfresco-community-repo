/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
