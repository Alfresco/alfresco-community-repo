package org.alfresco.repo.web.scripts;

import org.alfresco.service.descriptor.Descriptor;
import org.springframework.extensions.webscripts.ServerModel;


/**
 * Script / Template Model representing Repository Server meta-data
 * 
 * @author davidc
 */
public class RepositoryServerModel implements ServerModel
{
    private Descriptor currentDescriptor;
    private Descriptor serverDescriptor;
    
    /**
     * Construct
     * 
     * @param currentDescriptor Descriptor
     * @param serverDescriptor Descriptor
     */
    /*package*/ RepositoryServerModel(Descriptor currentDescriptor, Descriptor serverDescriptor)
    {
        this.currentDescriptor = currentDescriptor;
        this.serverDescriptor = serverDescriptor;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getContainerName()
     */
    public String getContainerName()
    {
        return "Repository";
    }

    /*(non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getId()
     */
    public String getId()
    {
        return currentDescriptor.getId();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getName()
     */
    public String getName()
    {
        return currentDescriptor.getName();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersionMajor()
     */
    public String getVersionMajor()
    {
        return currentDescriptor.getVersionMajor();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersionMinor()
     */
    public String getVersionMinor()
    {
        return currentDescriptor.getVersionMinor();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersionRevision()
     */
    public String getVersionRevision()
    {
        return currentDescriptor.getVersionRevision();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersionLabel()
     */
    public String getVersionLabel()
    {
        return currentDescriptor.getVersionLabel();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersionBuild()
     */
    public String getVersionBuild()
    {
        return currentDescriptor.getVersionBuild();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersion()
     */
    public String getVersion()
    {
        return currentDescriptor.getVersion();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getEdition()
     */
    public String getEdition()
    {
        return serverDescriptor.getEdition();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getSchema()
     */
    public int getSchema()
    {
        return currentDescriptor.getSchema();
    }
    
}
