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

package org.alfresco.module.org_alfresco_module_rm.capability;

import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Group implementation
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class GroupImpl implements Group
{
    /** The group id */
    private String id;

    /** The group title */
    private String title;

    /** The group index */
    private int index;

    /** Capability service */
    private CapabilityService capabilityService;

    /**
     * Sets the capability service
     *
     * @param capabilityService the capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    public void init()
    {
        this.capabilityService.addGroup(this);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Group#getId()
     */
    @Override
    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Group#getTitle()
     */
    @Override
    public String getTitle()
    {
        String title = this.title;
        if (StringUtils.isBlank(title))
        {
            title = I18NUtil.getMessage("capability.group." + getId() + ".title");
        }
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Group#getIndex()
     */
    @Override
    public int getIndex()
    {
        return this.index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }
}
