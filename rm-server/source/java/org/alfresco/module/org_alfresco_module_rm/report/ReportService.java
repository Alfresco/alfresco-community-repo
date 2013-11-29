/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Report service.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public interface ReportService
{
    /**
     * Register a report generator with the report service.
     *
     * @param reportGenerator   report generator
     */
    void registerReportGenerator(ReportGenerator reportGenerator);

    /**
     * Get a list of the available report types.
     *
     * @return
     */
    Set<QName> getReportTypes();

    /**
     *
     *
     * @param reportType
     * @param reportedUponNodeRef
     * @return
     */
    Report generateReport(QName reportType, NodeRef reportedUponNodeRef);

    /**
     *
     * @param reportType
     * @param reportedUponNodeRef
     * @return
     */
    Report generateReport(QName reportType, NodeRef reportedUponNodeRef, String mimetype);

    /**
     * @param reportType
     * @param reportedUponNodeRef
     * @param mimetype
     * @param properties
     * @return
     */
    Report generateReport(QName reportType, NodeRef reportedUponNodeRef, String mimetype, Map<String, Serializable> properties);

    /**
     * File report in the given destination. If the given node reference is a file plan node
     * reference the report will be filed in the unfiled records container.
     *
     * @param nodeRef   node reference
     * @param report    report
     * @return NodeRef  node reference of the filed report
     */
    NodeRef fileReport(NodeRef nodeRef, Report report);
}
