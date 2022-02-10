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

import java.util.Date;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;

/**
 * Vital record definition implementation class
 * 
 * @author Roy Wetherall
 */
public class VitalRecordDefinitionImpl implements VitalRecordDefinition, RecordsManagementModel
{
    /** Indicates whether the vital record definition is enabled or not */
    private boolean enabled = false;
    
    /** Vital record review period */
    private Period reviewPeriod = new Period("none|0");
    
    /**
     * Constructor.
     * 
     * @param enabled     
     * @param reviewPeriod
     */
    /* package */ VitalRecordDefinitionImpl(boolean enabled, Period reviewPeriod)
    {
        this.enabled = enabled;
        if (reviewPeriod != null)
        {
            this.reviewPeriod = reviewPeriod;
        }
    }
    
    /**
     * Helper method to create vital record definition from node reference.
     * 
     * @param nodeService
     * @param nodeRef
     * @return
     */
    /* package */ static VitalRecordDefinition create(NodeService nodeService, NodeRef nodeRef)
    {
        Boolean enabled = (Boolean)nodeService.getProperty(nodeRef, PROP_VITAL_RECORD_INDICATOR);
        if (enabled == null)
        {
            enabled = Boolean.FALSE;
        }
        Period reviewPeriod = (Period)nodeService.getProperty(nodeRef, PROP_REVIEW_PERIOD);
        return new VitalRecordDefinitionImpl(enabled, reviewPeriod);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition#isEnabled()
     */
    @Override
    public boolean isEnabled()
    {
        return enabled;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition#getNextReviewDate()
     */
    public Date getNextReviewDate()
    {
        return getReviewPeriod().getNextDate(new Date());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition#getReviewPeriod()
     */
    public Period getReviewPeriod()
    {
        return reviewPeriod;
    }
}
