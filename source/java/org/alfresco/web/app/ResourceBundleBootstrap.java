/**
 * 
 */
package org.alfresco.web.app;

import java.util.List;

/**
 * Resource bundle bootstrap bean
 * 
 * @author Roy Wetherall
 */
public class ResourceBundleBootstrap 
{
    /**
     * Set the resource bundles to be registered.  This should be a list of resource
     * bundle base names whose content will be made available to the web client.
     * 
     * @param resourceBundles   the resource bundles
     */
	public void setResourceBundles(List<String> resourceBundles)
    {
        for (String resourceBundle : resourceBundles)
        {
            ResourceBundleWrapper.addResourceBundle(resourceBundle);
        }
    }
}
