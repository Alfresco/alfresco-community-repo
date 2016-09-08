/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.report;

import org.alfresco.service.namespace.QName;

/**
 * Helper class containing records management report qualified names
 *
 * @author Roy Wetherall
 */
public interface ReportModel 
{
	// Namespace details
	public static final String RMR_URI = "http://www.alfresco.org/model/recordsmanagementreport/1.0";
	public static final String RMR_PREFIX = "rmr";
	
	public static final QName TYPE_REPORT = QName.createQName(RMR_URI, "report");
	public static final QName TYPE_DESTRUCTION_REPORT = QName.createQName(RMR_URI, "destructionReport");
}
