package org.alfresco.repo.security.person;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract class that implements {@link HomeFolderProvider2} which
 * works with the {@link PortableHomeFolderManager} (which performs most of
 * the work) to create home folders in custom locations.
 * 
 * @author Alan Davis
 */
public abstract class AbstractHomeFolderProvider2 implements
        HomeFolderProvider2, BeanNameAware, InitializingBean
{
    /**
     * The provider name
     */
    private String name;

    /**
     * The home folder manager
     */
    private PortableHomeFolderManager homeFolderManager;
    
    /**
     * The store URL.
     */
    private String storeUrl;

    /**
     * The path to the root folder
     */
    private String rootPath;
    
    /**
     * Set the authority to use as the owner of all home folder nodes.
     * May be {@code null}.
     */
    private String owner;

    /**
     * PermissionsManager used on creating the home folder
     */
    private PermissionsManager onCreatePermissionsManager;

    /**
     * PermissionsManager used on referencing the home folder
     */
    private PermissionsManager onReferencePermissionsManager;

    /**
     * Register with the homeFolderManagewr
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "homeFolderManager", homeFolderManager);
        homeFolderManager.addProvider(this);
    }

    /**
     * Get the home folder manager.
     */
    protected PortableHomeFolderManager getHomeFolderManager()
    {
        return homeFolderManager;
    }

    /**
     * Set the home folder manager.
     * @param homeFolderManager PortableHomeFolderManager
     */
    public void setHomeFolderManager(PortableHomeFolderManager homeFolderManager)
    {
        this.homeFolderManager = homeFolderManager;
    }

    /**
     * Get the provider name
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * The provider name is taken from the bean name
     */
    @Override
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * Get the path of the root folder
     */
    @Override
    public String getRootPath()
    {
        return rootPath;
    }

    /**
     * Set the path of the root folder
     */
    public void setRootPath(String rootPath)
    {
        boolean reset = this.rootPath != null;
        this.rootPath = rootPath;
        
        // If a reset need to clear caches
        if (reset)
        {
            homeFolderManager.clearCaches(this);
        }
    }
    
    @Override
    public String getStoreUrl()
    {
        return storeUrl;
    }
    
    /**
     * Set the store URL.
     */
    public void setStoreUrl(String storeUrl)
    {
        this.storeUrl = storeUrl;
    }
    
    /**
     * Sets the PermissionsManager used on creating the home folder
     */
    public void setOnCreatePermissionsManager(PermissionsManager onCreatePermissionsManager)
    {
        this.onCreatePermissionsManager = onCreatePermissionsManager;
    }
    
    @Override
    public PermissionsManager getOnCreatePermissionsManager()
    {
        return onCreatePermissionsManager;
    }

    /**
     * Sets the PermissionsManager used on referencing the home folder
     */
    public void setOnReferencePermissionsManager(PermissionsManager onReferencePermissionsManager)
    {
        this.onReferencePermissionsManager = onReferencePermissionsManager;
    }

    @Override
    public PermissionsManager getOnReferencePermissionsManager()
    {
        return onReferencePermissionsManager;
    }

    /**
     * Set the authority to use as the owner of all home folder nodes.
     */
    public void setOwner(String owner)
    {
        this.owner = owner;
    }
    
    @Override
    public String getOwner()
    {
        return owner;
    }

    @Override
    public List<String> getHomeFolderPath(NodeRef person)
    {
        return null;
    }

    @Override
    public NodeRef getTemplateNodeRef()
    {
        return null;
    }
}
