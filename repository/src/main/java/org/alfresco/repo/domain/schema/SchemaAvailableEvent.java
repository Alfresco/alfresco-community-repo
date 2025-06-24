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
package org.alfresco.repo.domain.schema;

import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of the availability of the Alfresco Database Schema. Any class requiring the database must wait until after this event.
 * 
 * @author dward
 */
public class SchemaAvailableEvent extends ApplicationEvent
{

    private static final long serialVersionUID = -1882393521985043422L;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public SchemaAvailableEvent(Object source)
    {
        super(source);
    }

}
