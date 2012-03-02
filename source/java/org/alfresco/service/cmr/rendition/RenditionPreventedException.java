/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import org.alfresco.repo.rendition.RenditionPreventionRegistry;

/**
 * This exception is thrown if an attempt is made to render a node which has a {@link RenditionPreventionRegistry content class}
 * registered to prevent rendition.
 * 
 * @author Neil Mc Erlean
 * @since 4.0.1
 */
public class RenditionPreventedException extends RenditionServiceException
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a Rendition Service Exception with the specified message.
     * 
     * @param message   the message string
     */
    public RenditionPreventedException(String message) 
    {
        super(message);
    }

    /**
     * Constructs a Rendition Service Exception with the specified message and source exception.
     * 
     * @param message   the message string
     * @param source    the source exception
     */
    public RenditionPreventedException(String message, Throwable source) 
    {
        super(message, source);
    }

    /**
     * Constructs a Rendition Service Exception with the specified message and {@link RenditionDefinition}.
     * 
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     */
    public RenditionPreventedException(String message, RenditionDefinition renditionDefinition) 
    {
        super(message);
    }
    
    /**
     * Constructs a Rendition Service Exception with the specified message, {@link RenditionDefinition} and
     * source exception
     * .
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     * @param source the source exception.
     */
    public RenditionPreventedException(String message, RenditionDefinition renditionDefinition, Throwable source) 
    {
        super(message, source);
    }
}
