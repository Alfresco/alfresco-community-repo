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

import org.alfresco.service.cmr.repository.Period;

/**
 * Vital record definition interface
 * 
 * @author Roy Wetherall
 */
public interface VitalRecordDefinition
{   
    /**
     * Indicates whether the vital record definition is enabled or not.
     * <p>
     * Note:  a result of false indicates that the vital record definition is inactive
     * therefore does not impose the rules associated with vital record review on 
     * associated nodes.
     * 
     * @return  boolean true if enabled, false otherwise
     */
    boolean isEnabled();
    
    /**
     * Review period for vital records
     * 
     * @return Period   review period
     */
    Period getReviewPeriod();
    
    /**
     * Gets the next review date based on the review period
     * 
     * @return Date date of the next review
     */
    Date getNextReviewDate();
}
