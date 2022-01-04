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

package org.alfresco.module.org_alfresco_module_rm.script.hold;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Hold POJO
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class Hold
{
    /** Hold name */
    private String name;

    /** Hold node reference */
    private NodeRef nodeRef;

    /**
     * Constructor
     *
     * @param name The name of the hold
     * @param nodeRef The {@link NodeRef} of the hold
     */
    public Hold(String name, NodeRef nodeRef)
    {
        this.name = name;
        this.nodeRef = nodeRef;
    }

    /**
     * Gets the hold name
     *
     * @return The name of the hold
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Gets the hold node reference
     *
     * @return The {@link NodeRef} of the hold
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }
}
