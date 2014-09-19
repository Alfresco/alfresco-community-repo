/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
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
     * @param <T>
     * @param paramName
     * @param defaultValue
     * @return
     */
    public <T> T getParamWithDefault(String paramName, T defaultValue);
    
    /**
     * Gets the int value for the named parameter.  Returns
     * <code>defaultValue</code> if the parameter value is <code>null</code>.
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public int getIntegerParam(String key, int defaultValue);

}
