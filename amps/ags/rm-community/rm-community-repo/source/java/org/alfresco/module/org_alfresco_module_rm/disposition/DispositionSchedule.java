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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Disposition schedule interface
 *
 * @author Roy Wetherall
 */
// Not @AlfrescoPublicApi because it depends on DispositionActionDefinition which is not part of the public API.
public interface DispositionSchedule
{
    /**
     * Get the NodeRef that represents the disposition schedule
     *
     * @return {@link NodeRef} of disposition schedule
     */
    NodeRef getNodeRef();

    /**
     * Get the disposition authority
     *
     * @return  {@link String}  disposition authority
     */
    String getDispositionAuthority();

    /**
     * Get the disposition instructions
     *
     * @return  {@link String}  disposition instructions
     */
    String getDispositionInstructions();

    /**
     * Indicates whether the disposal occurs at record level or not
     *
     * @return  boolean true if at record level, false otherwise
     */
    boolean isRecordLevelDisposition();

    /**
     * Gets all the disposition action definitions for the schedule
     *
     * @return  List&lt;{@link DispositionActionDefinition}&gt;   disposition action definitions
     */
    List<DispositionActionDefinition> getDispositionActionDefinitions();

    /**
     * Get the disposition action definition
     *
     * @param id    the action definition id
     * @return {@link DispositionActionDefinition}  disposition action definition
     */
    DispositionActionDefinition getDispositionActionDefinition(String id);

    /**
     * Get the disposition action definition by the name of the disposition action
     *
     * @param name  disposition action name
     * @return {@link DispositionActionDefinition}  disposition action definition, null if none
     */
    DispositionActionDefinition getDispositionActionDefinitionByName(String name);
}
