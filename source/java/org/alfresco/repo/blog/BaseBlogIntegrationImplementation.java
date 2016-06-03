package org.alfresco.repo.blog;

/**
 * Base blog implementation class.  Extend this when writting a blog integration implementation.
 * 
 * @author Roy Wetherall
 */
public abstract class BaseBlogIntegrationImplementation implements BlogIntegrationImplementation
{
    /** Blog integration service */
    private BlogIntegrationService blogIntegrationService;
    
    /** Integration name */
    private String name;
    
    /** Display name */
    private String displayName;
    
    /**
     * Sets the blog integration service
     * 
     * @param blogIntegrationService    the blog integration service
     */
    public void setBlogIntegrationService(BlogIntegrationService blogIntegrationService)
    {
        this.blogIntegrationService = blogIntegrationService;
    }
    
    /**
     * Registers the blog implementation with the blog integration service.
     */
    public void register()
    {
        this.blogIntegrationService.register(this);
    }
    
    /**
     * Sets the name of the blog integration service
     * 
     * @param name  the name
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @see org.alfresco.repo.blog.BlogIntegrationImplementation#getName()
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Sets the display name
     * 
     * @param displayName   the display name
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
    
    /**
     * @see org.alfresco.repo.blog.BlogIntegrationImplementation#getDisplayName()
     */
    public String getDisplayName()
    {
       return this.displayName;
    }
}
