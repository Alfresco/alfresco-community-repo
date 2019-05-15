package org.alfresco.rest.core.swagger;

import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.models.Model;
import io.swagger.models.Swagger;

/**
 * Handles swagger definitions <code>Entry<String, Model> model : swagger.getDefinitions().entrySet()</code>
 * 
 * @author Paul Brodner
 */
public class SwaggerModel
{
    private Swagger swagger;
    private Entry<String, Model> model;
    private Path modelsPath = Paths.get(Paths.get(".").toAbsolutePath().normalize().toFile().getPath(), "src/main/java/org/alfresco/rest/model");
    private List<RestModelProperty> properties = new ArrayList<RestModelProperty>();
    private Configuration cfg;

    private Configuration getConfig() throws IOException
    {
        if (cfg == null)
        {
            cfg = new Configuration(Configuration.VERSION_2_3_23);
            cfg.setDirectoryForTemplateLoading(new File("src/main/resources"));
        }
        return cfg;
    }

    public SwaggerModel(Entry<String, Model> model, Swagger swagger)
    {
        this.model = model;
        this.swagger = swagger;

        
        if(model.getValue().getProperties()!=null)
        {
            /*
             * compute the properties of this model
             */
            for (Entry<String, io.swagger.models.properties.Property> property : model.getValue().getProperties().entrySet())
            {
                if (property.getKey().equals("entry"))
                    continue;
    
                properties.add(RestModelProperty.build(property));        
            }
        }
        
    }

    /**
     * @return boolean value if file is already generated in TAS, under 'models' package
     */
    public boolean exist()
    {
        return getPath().exists();
    }

    /**
     * @return the location of the model in TAS
     */
    public File getPath()
    {
        return Paths.get(modelsPath.toFile().getPath(), getNameInTAS() + ".java").toFile();
    }

    /**
     * @return original model name as defined in Swagger YAML
     */
    public String getName()
    {
        return model.getKey();
    }

    /**
     * @return the name as it will be used in TAS
     */
    public String getNameInTAS()
    {
        return String.format("Rest%sModel", getName());
    }

    public List<RestModelProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<RestModelProperty> properties)
    {
        this.properties = properties;
    }

    public void generate() throws IOException, TemplateException
    {
        Template template = getConfig().getTemplate("rest-model.ftl");

        // here we will store all data passed to template
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        data.put("author", System.getProperty("user.name"));
        data.put("yamlTitle", swagger.getInfo().getTitle());
        data.put("yamlBasePath", swagger.getBasePath());
        data.put("name", getNameInTAS());

        if (!getProperties().isEmpty())
            data.put("properties", getProperties());

        Writer append = new StringWriter();
        template.process(data, append);

        append.close();
        
        System.out.println("----- " + getPath().getName() + " -----\n");
        System.out.println(Generator.line);        
        System.out.println(append.toString());
        System.out.printf("\nGenerating Model: %-10s to ->'%-60s'", getName(), getPath());
        if (exist())
        {
            Console c = System.console();
            if (c != null)
            {
                System.out.printf("There is already one model created locally: \n%s\nDo you want to override it ?(ENTER=yes, any other key=no):", getPath());
                if (c.readLine().length() == 0)
                {
                    writeContent(append.toString());
                }
            }
        }
        else
        {
            writeContent(append.toString());

        }
    }
    
    private void writeContent(String content) throws IOException
    {
        FileWriter fw = new FileWriter(getPath());
        fw.write(content);
        fw.close();  
    }
}
