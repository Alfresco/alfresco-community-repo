package org.alfresco.service.cmr.module;

import java.io.Serializable;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * An ensapsulated module dependency.  Since module dependencies may be range based and even
 * unbounded, it is not possible to describe a dependency using a list of module version numbers.
 * This class answers the 
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface ModuleDependency extends Serializable
{
    /**
     * Get the ID of the module that this dependency describes.  The dependency
     * may be upon specific versions or a range of versions.  Nevertheless, the
     * module given by the returned ID will be required in one version or another.
     * 
     * @return      Returns the ID of the module that this depends on
     */
    public String getDependencyId();
    
    /**
     * @return      Returns a string representation of the versions supported
     */
    public String getVersionString();

    /**
     * Check if a module satisfies the dependency requirements.
     * 
     * @param moduleDetails     the module details of the dependency.  This must be
     *                          the details of the module with the correct
     *                          {@link #getDependencyId() ID}.  This may be <tt>null</tt>
     *                          in which case <tt>false</tt> will always be returned.
     * @return                  Returns true if the module satisfies the dependency
     *                          requirements.
     */
    public boolean isValidDependency(ModuleDetails moduleDetails);
}
