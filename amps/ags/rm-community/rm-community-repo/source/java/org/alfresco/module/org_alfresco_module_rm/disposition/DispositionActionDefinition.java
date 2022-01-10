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

package org.alfresco.module.org_alfresco_module_rm.disposition;

import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.QName;

/**
 * Disposition action interface
 *
 * @author Roy Wetherall
 */
// Not @AlfrescoPublicApi because it depends on Period which is not part of the public API.
public interface DispositionActionDefinition
{
    /**
     * Get the NodeRef that represents the disposition action definition
     *
     * @return NodeRef of disposition action definition
     */
    NodeRef getNodeRef();

    /**
     * Get disposition action id
     *
     * @return String id
     */
    String getId();

    /**
     * Get the index of the action within the disposition instructions
     *
     * @return int disposition action index
     */
    int getIndex();

    /**
     * Get the name of disposition action
     *
     * @return String name
     */
    String getName();

    /**
     * Get the display label of the disposition action
     *
     * @return String name's display label
     */
    String getLabel();

    /**
     * Get the description of the disposition action
     *
     * @return String description
     */
    String getDescription();

    /**
     * Get the period for the disposition action
     *
     * @return Period disposition period
     */
    Period getPeriod();

    /**
     * Property to which the period is relative to
     *
     * @return QName property name
     */
    QName getPeriodProperty();

    /**
     * List of events for the disposition
     *
     * @return List<RecordsManagementEvent> list of events
     */
    List<RecordsManagementEvent> getEvents();

    /**
     * Indicates whether the disposition action is eligible when the earliest
     * event is complete, otherwise all events must be complete before
     * eligibility.
     *
     * @return boolean true if eligible on first action complete, false
     *         otherwise
     */
    boolean eligibleOnFirstCompleteEvent();

    /**
     * Get the location of the disposition (can be null)
     *
     * @return String disposition location
     */
    String getLocation();

    /**
     * Get the ghost on destroy from the disposition
     *
     * @return boolean the gost on destroy flag (on applicable to destroy
     *         actions)
     */
    String getGhostOnDestroy();

}
