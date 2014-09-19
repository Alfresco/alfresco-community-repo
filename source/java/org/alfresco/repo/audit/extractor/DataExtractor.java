/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.audit.extractor;

import java.io.Serializable;

import org.alfresco.api.AlfrescoPublicApi;   

/**
 * Interface for Audit data value extractors.  These are used to extract auditable values
 * from those arguments, return values, exceptions and any other value passed into the audit
 * components for recording.
 * <p/>
 * The framework will first determine if data passed into the instance is {@link #isSupported(Object) supported}
 * and will then pass it in for {@link #extractData(Serializable) conversion} to the type that will be
 * recorded.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
@AlfrescoPublicApi
public interface DataExtractor
{
    /**
     * Determines if the extractor will be able to pull any data from the given value.
     * 
     * @param data          the data that might be useful to this extractor (could be <tt>null</tt>)
     * @return              Returns <tt>true</tt> if the data is meaningful to this extractor
     */
    public boolean isSupported(Serializable data);
    
    /**
     * Convert an value passed into the audit components into a value to be recorded.
     * 
     * @param value                 the source data
     * @return                      the extracted data including <tt>null</tt>
     * @throws Throwable            All errors will be handled by the calling framework
     */
    public Serializable extractData(Serializable value) throws Throwable;
}
