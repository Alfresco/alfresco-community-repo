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

package org.alfresco.module.org_alfresco_module_rm.vital;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;

/**
 * Vital Record Service.
 *
 * @author Roy Wetherall
 * @since 2.0
 */
// Not @AlfrescoPublicApi due to e.g. Period.
public interface VitalRecordService
{
    /** Period 'none' */
    Period PERIOD_NONE = new Period("none|0");

    /**
     * Setup the vital record definition for the given node.
     *
     * @param nodeRef   node reference
     */
    void setupVitalRecordDefinition(NodeRef nodeRef);

    /**
     * Gets the vital record definition details for the node.
     *
     * @param nodeRef                   node reference
     * @return VitalRecordDefinition    vital record definition details
     */
    VitalRecordDefinition getVitalRecordDefinition(NodeRef nodeRef);

    /**
     * Sets the vital record definition values for a given node.
     *
     * @param nodeRef
     * @param enabled
     * @param reviewPeriod
     * @return
     */
    VitalRecordDefinition setVitalRecordDefintion(NodeRef nodeRef, boolean enabled, Period reviewPeriod);

    /**
     * Indicates whether the record is a vital one or not.
     *
     * @param nodeRef   node reference
     * @return boolean  true if this is a vital record, false otherwise
     */
    boolean isVitalRecord(NodeRef nodeRef);

    /**
     * Initialises the vital record details.
     *
     * @param nodeRef   node reference to initialise with vital record details
     */
    //void initialiseVitalRecord(NodeRef nodeRef);
}
