/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.patch.v22;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.repo.domain.qname.QNameDAO;

/**
 * DOD compliant site patch.
 * 
 * Makes all existing sites of type dod:site to preserve their DOD compliance status.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class RMv22DODCompliantSitePatch extends AbstractModulePatch 
                                        implements RecordsManagementModel
{
    /** QName DAO */
    private QNameDAO qnameDAO;

    /**
     * @param qnameDAO  QName DAO
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
        // ensure all existing sites are of the correct type
        if (qnameDAO.getQName(RecordsManagementModel.TYPE_RM_SITE) != null &&
            qnameDAO.getQName(DOD5015Model.TYPE_DOD_5015_SITE) == null)
        {
            qnameDAO.updateQName(RecordsManagementModel.TYPE_RM_SITE, DOD5015Model.TYPE_DOD_5015_SITE);
        }
        
        // ensure all the existing file plans are of the correct type
        if (qnameDAO.getQName(RecordsManagementModel.TYPE_FILE_PLAN) != null &&
            qnameDAO.getQName(DOD5015Model.TYPE_DOD_5015_FILE_PLAN) == null)
        {
            qnameDAO.updateQName(RecordsManagementModel.TYPE_FILE_PLAN, DOD5015Model.TYPE_DOD_5015_FILE_PLAN);
        }
    }
}
