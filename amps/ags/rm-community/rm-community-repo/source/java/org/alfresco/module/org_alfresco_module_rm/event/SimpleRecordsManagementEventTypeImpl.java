/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.event;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Simple records management event type implementation
 *
 * @author Roy Wetherall
 */
public class SimpleRecordsManagementEventTypeImpl implements RecordsManagementEventType, BeanNameAware
{
    /** Display label lookup prefix */
    protected static final String LOOKUP_PREFIX = "rmeventservice.";

    /** Name */
    public static final String NAME = "rmEventType.simple";

    /** Records management event service */
    private RecordsManagementEventService recordsManagementEventService;

    /** Name */
    private String name;

    /**
     * @return Records management event service
     */
    protected RecordsManagementEventService getRecordsManagementEventService()
    {
        return this.recordsManagementEventService;
    }

    /**
     * Set the records management event service
     *
     * @param recordsManagementEventService     records management service
     */
    public void setRecordsManagementEventService(RecordsManagementEventService recordsManagementEventService)
    {
        this.recordsManagementEventService = recordsManagementEventService;
    }

    /**
     * Initialisation method
     */
    public void init()
    {
        getRecordsManagementEventService().registerEventType(this);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType#isAutomaticEvent()
     */
    public boolean isAutomaticEvent()
    {
        return false;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType#getDisplayLabel()
     */
    public String getDisplayLabel()
    {
        return I18NUtil.getMessage(LOOKUP_PREFIX + getName());
    }
}
