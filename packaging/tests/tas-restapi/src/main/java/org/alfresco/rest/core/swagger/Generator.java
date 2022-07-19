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
