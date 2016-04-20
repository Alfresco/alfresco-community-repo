package org.alfresco.service.cmr.repository;

import org.alfresco.api.AlfrescoPublicApi;    
import org.alfresco.service.cmr.rendition.RenditionServiceException;

/**
 * Defines methods for retrieving parameter values for use in building
 * transformation options.
 * 
 * @author Ray Gauss II
 */
@AlfrescoPublicApi
public interface SerializedTransformationOptionsAccessor
{
    
    /**
     * Gets the value for the named parameter. Checks the type of
     * the parameter is correct and throws and Exception if it isn't.
     * Returns <code>null</code> if the parameter value is <code>null</code>
     * 
     * @param paramName the name of the parameter being checked.
     * @param clazz the expected {@link Class} of the parameter value.
     * @return the parameter value or <code>null</code>.
     */
    public <T> T getCheckedParam(String paramName, Class<T> clazz);
    
    /**
     * Gets the value for the named parameter. Checks the type of the
     * parameter is the same as the type of <code>defaultValue</code> and
     * throws a {@link RenditionServiceException} if it isn't. Returns
     * <code>defaultValue</code> if the parameter value is <code>null</code>
     * 
     * @param <T> <T>
     * @param paramName String
     * @param defaultValue <T>
     * @return <T>
     */
    public <T> T getParamWithDefault(String paramName, T defaultValue);
    
    /**
     * Gets the int value for the named parameter.  Returns
     * <code>defaultValue</code> if the parameter value is <code>null</code>.
     * 
     * @param key String
     * @param defaultValue int
     * @return int
     */
    public int getIntegerParam(String key, int defaultValue);

}
