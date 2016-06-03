package org.alfresco.repo.importer;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;

/**
 * Collection of views to import
 * 
 * @author David Caruana
 */
public class ImporterBootstrapViews implements InitializingBean
{
    // Dependencies
    private ImporterBootstrap importer;
    private List<Properties> bootstrapViews;
    

    /**
     * Sets the importer
     * 
     * @param importer ImporterBootstrap
     */
    public void setImporter(ImporterBootstrap importer)
    {
        this.importer = importer;
    }
    
    /**
     * Sets the bootstrap views
     * 
     * @param bootstrapViews List<Properties>
     */
    public void setBootstrapViews(List<Properties> bootstrapViews)
    {
        this.bootstrapViews = bootstrapViews;
    }

    
    public void afterPropertiesSet() throws Exception
    {
        importer.addBootstrapViews(bootstrapViews);
    }

}
