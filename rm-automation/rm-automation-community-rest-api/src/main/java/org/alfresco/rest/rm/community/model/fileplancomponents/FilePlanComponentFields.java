/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.model.fileplancomponents;

/**
 * File plan component field names constants
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class FilePlanComponentFields
{
    public static final String NAME = "name";
    public static final String NODE_TYPE = "nodeType";
    public static final String NODE_PARENT_ID = "parentId";
    public static final String ENTRY = "entry";
    public static final String PROPERTIES = "properties";
    public static final String PROPERTIES_TITLE = "cm:title";
    public static final String PROPERTIES_VITAL_RECORD_INDICATOR = "rma:vitalRecordIndicator";
    public static final String PROPERTIES_HOLD_REASON = "rma:holdReason";
    public static final String PROPERTIES_DESCRIPTION = "cm:description";
    public static final String PROPERTIES_SUPPLEMENTAL_MARKING_LIST = "rmc:supplementalMarkingList";
    public static final String ALLOWABLE_OPERATIONS = "allowableOperations";
    public static final String IS_CLOSED = "isClosed";
    public static final String PROPERTIES_REVIEW_PERIOD = "rma:reviewPeriod";
    public static final String PROPERTIES_LOCATION = "rma:location";
    public static final String PROPERTIES_IS_CLOSED = "rma:isClosed"; // not to be confused with IS_CLOSED!

    // for non-electronic records
    public static final String PROPERTIES_BOX = "rma:box";
    public static final String PROPERTIES_FILE = "rma:file";
    public static final String PROPERTIES_NUMBER_OF_COPIES = "rma:numberOfCopies";
    public static final String PROPERTIES_PHYSICAL_SIZE = "rma:physicalSize";
    public static final String PROPERTIES_SHELF = "rma:shelf";
    public static final String PROPERTIES_STORAGE_LOCATION = "rma:storageLocation";

    //RelativePath specifies the container structure to create relative to the node nodeId.
    public static final String RELATIVE_PATH = "relativePath";
    public static final String PATH = "path";
}
