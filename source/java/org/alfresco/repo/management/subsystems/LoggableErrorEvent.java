/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.management.subsystems;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.context.ApplicationEvent;

/**
 * Alfresco custom ApplicationEvent class used for publishing errors in subsystems.
 */
public class LoggableErrorEvent extends ApplicationEvent
{
    private static final String UNSPECIFIED_ERROR_MESSAGE = "system.loggable_error_event.unspecified_error";
    
    private AlfrescoRuntimeException exception;

    /**
     * Create a new LoggableErrorEvent.
     * @param source the component that published the event (never {@code null})
     * @param exception the error to publish
     */
    public LoggableErrorEvent(Object source, AlfrescoRuntimeException exception)
    {
        super(source);
        this.exception = exception;
    }

    /**
     * Get this LoggableErrorEvent's exception.
     * @return the stored exception if not null, otherwise a new RuntimeException 
     */
    public AlfrescoRuntimeException getException()
    {
        if (exception != null)
        {
            return exception;
        }
        else
        {
            return new AlfrescoRuntimeException(UNSPECIFIED_ERROR_MESSAGE);
        }
    }
}
