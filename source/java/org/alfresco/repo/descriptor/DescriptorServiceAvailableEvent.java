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
package org.alfresco.repo.descriptor;

import org.alfresco.service.descriptor.DescriptorService;
import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of the availability of the {@link DescriptorService}. Useful for
 * Monitoring purposes.
 * 
 * @author dward
 */
public class DescriptorServiceAvailableEvent extends ApplicationEvent
{

    private static final long serialVersionUID = 8217523101300405165L;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source descriptor service
     */
    public DescriptorServiceAvailableEvent(DescriptorService source)
    {
        super(source);
    }

    /**
     * Gets the descriptor service that raised the event.
     * 
     * @return the descriptor service
     */
    public DescriptorService getDescriptorService()
    {
        return (DescriptorService) getSource();
    }
}
