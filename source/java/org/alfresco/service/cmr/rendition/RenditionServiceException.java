/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.rendition;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Rendition Service Exception Class
 * 
 * @author Neil McErlean
 */
public class RenditionServiceException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -6947067735970465937L;
    private final RenditionDefinition renditionDefinition;

    /**
     * Constructs a Rendition Service Exception with the specified message.
     * 
     * @param message   the message string
     */
    public RenditionServiceException(String message) 
    {
        super(message);
        this.renditionDefinition = null;
    }

    /**
     * Constructs a Rendition Service Exception with the specified message and source exception.
     * 
     * @param message   the message string
     * @param source    the source exception
     */
    public RenditionServiceException(String message, Throwable source) 
    {
        super(message, source);
        this.renditionDefinition = null;
    }

    /**
     * Constructs a Rendition Service Exception with the specified message and {@link RenditionDefinition}.
     * 
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     * @since 3.5.0
     */
    public RenditionServiceException(String message, RenditionDefinition renditionDefinition) 
    {
        super(message);
        this.renditionDefinition = renditionDefinition;
    }
    
    /**
     * Constructs a Rendition Service Exception with the specified message, {@link RenditionDefinition} and
     * source exception
     * .
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     * @param source the source exception.
     * @since 3.5.0
     */
    public RenditionServiceException(String message, RenditionDefinition renditionDefinition, Throwable source) 
    {
        super(message, source);
        this.renditionDefinition = renditionDefinition;
    }

    /**
     * Retrieves the {@link RenditionDefinition} associated with this exception.
     * @return the rendition definition, which may be <tt>null</tt>.
     * @since 3.5.0
     */
    public RenditionDefinition getRenditionDefinition()
    {
        return this.renditionDefinition;
    }
}
