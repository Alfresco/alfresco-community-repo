/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.rendition;

/**
 * Rendition Service Exception Class
 * 
 * @author Neil McErlean
 * @author Ray Gauss II
 */
public class RenditionCancelledException extends RenditionServiceException
{
    private static final long serialVersionUID = 6369478812653824042L;

    /**
     * Constructs a Rendition Cancelled Exception with the specified message.
     * 
     * @param message   the message string
     */
    public RenditionCancelledException(String message) 
    {
        super(message);
    }

    /**
     * Constructs a Rendition Cancelled Exception with the specified message and source exception.
     * 
     * @param message   the message string
     * @param source    the source exception
     */
    public RenditionCancelledException(String message, Throwable source) 
    {
        super(message, source);
    }

    /**
     * Constructs a Rendition Cancelled Exception with the specified message and {@link RenditionDefinition}.
     * 
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     * @since 3.5.0
     */
    public RenditionCancelledException(String message, RenditionDefinition renditionDefinition) 
    {
        super(message);
    }
    
    /**
     * Constructs a Rendition Cancelled Exception with the specified message, {@link RenditionDefinition} and
     * source exception
     * .
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     * @param source the source exception.
     * @since 3.5.0
     */
    public RenditionCancelledException(String message, RenditionDefinition renditionDefinition, Throwable source) 
    {
        super(message, source);
    }

}
