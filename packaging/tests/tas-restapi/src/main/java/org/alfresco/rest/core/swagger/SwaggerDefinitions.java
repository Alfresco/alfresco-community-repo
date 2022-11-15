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

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.alfresco.utility.exception.TestConfigurationException;

import freemarker.template.TemplateException;
import io.swagger.models.Model;
import io.swagger.models.Swagger;

/**
 * Handles all
 * <code>Entry<String, Model> model : swagger.getDefinitions().entrySet()</code>
 * 
 * @author Paul Brodner
 */
public class SwaggerDefinitions
{
    private Swagger swagger;
    List<SwaggerModel> missingSwaggerModels = new ArrayList<SwaggerModel>();
    Path modelsPath;

    public SwaggerDefinitions(Swagger swagger)
    {
        this.swagger = swagger;
        modelsPath = Paths.get(Paths.get(".").toAbsolutePath().normalize().toFile().getPath(), "src/main/java/org/alfresco/rest/model");
    }

    public void generateMissingDefinitions()
    {
        /*
         * read the content of ignore-moldels file
         */
        List<String> ignoreModel = new ArrayList<String>();
        try
        {
            try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(modelsPath.toFile().getPath(), "ignore-models").toFile())))
            {
                String line;
                while ((line = br.readLine()) != null)
                {
                    if (!line.startsWith("#") && !line.equals(""))
                        ignoreModel.add(line);
                }
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Exception while generating missing definitions.", e);
        }

        /*
         * filter all models, ignoring the ones from ignore-model or the ones that are already created locally
         */
        for (Entry<String, Model> model : swagger.getDefinitions().entrySet())
        {
            SwaggerModel swaggerModel = new SwaggerModel(model, swagger);

            //regexp
            if(swaggerModel.getName().matches(".*Paging|.*Entry"))
            {
                System.out.printf("Ignoring Model: [%s] (based on regular expression: '.*Paging|.*Entry')\n", swaggerModel.getName());
                continue;
            }
            
            if (ignoreModel.contains(swaggerModel.getName()))
            {
                System.out.printf("Ignoring Model: [%s] (based on 'ignore-models' file)\n", swaggerModel.getName());
                continue;
            }
            
            

            if (!swaggerModel.exist())
                missingSwaggerModels.add(swaggerModel);
        }

        System.out.println(Generator.line);
        int count = 0;
        /*
         * iterate on all missing models
         */
        for (SwaggerModel swaggerModel : missingSwaggerModels)
        {
            if (count == 0) // table heather
            {
                System.out.printf("MISSING MODEL ~ THE NEW FILE THAT WILL BE GENERATED\n");                
                System.out.println("  0   -[Skip all]");
            }

            count += 1;
            StringBuilder info = new StringBuilder();
            info.append("  ")
                .append(count)
                .append("   -")
                .append("[")
                .append(swaggerModel.getName())
                .append("] ~ [")
                .append(swaggerModel.getPath())
                .append("]");

            System.out.println(info.toString());
        }
        System.out.println("ENTER -[All Models]");

        /*
         * wait for input
         */
        Console c = System.console();
        if (c != null && missingSwaggerModels.size() > 0)
        {
            c.format("%s\n", Generator.line);
            c.format("\nPlease select what Models you want to generate (ex: 1,3,4) or press <<ENTER>>to generating all missing models:");
            String prompt = c.readLine();
            
            if(prompt.equals("0"))
                return;
            
            if (prompt.length() == 0)
            {
                System.out.println("\nStart generating all models...");
                for (SwaggerModel swaggerModel : missingSwaggerModels)
                {
                    generateModel(swaggerModel);
                }
            }
            else
            {
                if (prompt.contains(","))
                {
                    String[] modelsIDToGen = prompt.split(",");
                    for (int i = 0; i < modelsIDToGen.length; i++)
                    {
                        generateSelectedSwaggerModel(modelsIDToGen[i]);
                    }
                }
                else
                {
                    generateSelectedSwaggerModel(prompt);
                }
            }
        }
    }

    /**
     * Generate the model based on the ID provided
     * 
     * @param id
     */
    private void generateSelectedSwaggerModel(String id)
    {
        int choice = Integer.parseInt(id);
        if ((choice - 1) >= missingSwaggerModels.size())
        {
            throw new TestConfigurationException(
                    "You specified a wrong ID: [" + id + "] please select one value from the list displayed above. Run the command again!");
        }
        generateModel(missingSwaggerModels.get(choice - 1));
    }

    public boolean generateDefinition(String modelParamValue)
    {
        for (Entry<String, Model> model : swagger.getDefinitions().entrySet())
        {
            SwaggerModel swaggerModel = new SwaggerModel(model, swagger);
            if (swaggerModel.getName().equals(modelParamValue))
            {
                generateModel(swaggerModel);
                return true;
            }
        }
        System.err.println("Model that you provided was NOT found!");
        System.err.printf("Model [%s] not found in Swagger file: %s\n", modelParamValue, swagger.getBasePath());
        return false;
    }

    private void generateModel(SwaggerModel swaggerModel)
    {
        try
        {
            swaggerModel.generate();
        }
        catch (IOException | TemplateException e)
        {
            throw new IllegalStateException("Exception while generating model definition.", e);
        }
    }
}
