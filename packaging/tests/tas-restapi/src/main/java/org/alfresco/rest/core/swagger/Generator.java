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

/**
 * Executed via command line/terminal from root of this project
 * Just execute <code>mvn exec:java</code> passing as arguments:
 * -Dcoverage           => this will show on screen the actual coverage of TAS (vs requests that exists in each YAML file - defined in pom.xml)
 * -Dmodels             => this will show all MISSING models that are NOT already implemented in TAS.
 * -Dmodels=a,b,d       => this will generate ONLY the models 'a', 'b' and 'd' passed as parameter
 * -Dhelp               => show help
 * 
 * @author Paul Brodner
 */
public class Generator
{
    public static String line = "********\n------------------------------------------------------------------------";
  
    public static void main(String[] args)
    {
        
        if (!System.getProperties().containsKey("coverage") && !System.getProperties().containsKey("models") || System.getProperties().containsKey("help") )
        {
            System.out.println(line);
            System.out.println("No parameters provided, please use the following values:\n");
            System.out.println(
                               "mvn exec:java -Dcoverage           => this will show on screen the actual coverage of TAS (vs requests that exists in each YAML file - defined in pom.xml).");
            System.out.println("mvn exec:java -Dmodels             => this will show all MISSING models that are NOT already implemented in TAS.");
            System.out.println("mvn exec:java -Dmodels=a,b,d       => this will generate ONLY the models 'a', 'b' and 'd' passed as parameter.");
            System.out.println(line);

        }

        for (String url : args)
        {
            if (System.getProperty("coverage") != null)
            {
                /*
                 * <code>mvn exec:java -Dcoverage</code>
                 */
                new SwaggerYamlParser(url).computeCoverage();
            }
            else if (System.getProperty("models") != null)
            {
                /*
                 * <code>mvn exec:java -Dmodels</code>
                 * <code>mvn exec:java -Dmodels=a,c,e</code>
                 */
                new SwaggerYamlParser(url).generateMissingModules();
            }
        }
    }
}
