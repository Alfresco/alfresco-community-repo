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

package org.alfresco.module.org_alfresco_module_rm.report;

import org.alfresco.service.namespace.QName;

/**
 * Helper class containing records management report qualified names
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public interface ReportModel
{
    /** Namespace details */
    String RMR_URI = "http://www.alfresco.org/model/recordsmanagementreport/1.0";
    String RMR_PREFIX = "rmr";

    /** base report type */
    QName TYPE_REPORT = QName.createQName(RMR_URI, "report");
    
    /** destruction report type */
    QName TYPE_DESTRUCTION_REPORT = QName.createQName(RMR_URI, "destructionReport");
    
    /** transfer report type */
    QName TYPE_TRANSFER_REPORT = QName.createQName(RMR_URI, "transferReport");
    
    /** 
     * hold report type
     * @since 2.2
     */
    QName TYPE_HOLD_REPORT = QName.createQName(RMR_URI, "holdReport");
}
