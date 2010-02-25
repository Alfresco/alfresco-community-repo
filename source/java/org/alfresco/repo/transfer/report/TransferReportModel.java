/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transfer.report;

import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.service.namespace.QName;

/**
 * The transfer report model - extended for XML Manifest Model
 */
public interface TransferReportModel extends TransferModel
{
    static final String LOCALNAME_TRANSFER_REPORT = "transferReport";
    static final String LOCALNAME_TRANSFER_TARGET = "target";
    static final String LOCALNAME_TRANSFER_DEFINITION = "definition";
    static final String LOCALNAME_EXCEPTION = "exception";
    static final String LOCALNAME_TRANSFER_EVENTS = "events";
    static final String LOCALNAME_TRANSFER_EVENT = "event";
    static final String LOCALNAME_TRANSFER_NODE = "node";
    static final String LOCALNAME_TRANSFER_PRIMARY_PATH = "primaryPath";
    static final String LOCALNAME_TRANSFER_PRIMARY_PARENT = "primaryParent";
    static final String REPORT_PREFIX = "report";
    
    static final String TRANSFER_REPORT_MODEL_1_0_URI = "http://www.alfresco.org/model/transferReport/1.0";
}
