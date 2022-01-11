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

package org.alfresco.module.org_alfresco_module_rm.dod5015;

import org.alfresco.module.org_alfresco_module_rm.model.rma.type.RmSiteType;

/**
 * Bootstrap bean that registers the dod:filePlan for creation when 
 * a dod:site is created.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class DOD5015FilePlanTypeBootstrap implements DOD5015Model
{
    /** RM site type bean */
    private RmSiteType rmSiteType;
    
    /**
     * @param rmSiteType    RM site type bean
     */
    public void setRmSiteType(RmSiteType rmSiteType)
    {
        this.rmSiteType = rmSiteType;
    }
    
    /**
     * Init method
     */
    public void init()
    {
        // register dod file plan type for the dod site type
        rmSiteType.registerFilePlanType(TYPE_DOD_5015_SITE, TYPE_DOD_5015_FILE_PLAN);
    }
}
