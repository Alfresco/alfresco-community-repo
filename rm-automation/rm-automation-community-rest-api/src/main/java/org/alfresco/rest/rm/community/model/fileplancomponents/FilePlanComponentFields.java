/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
    public static final String IS_CLOSED="isclosed";
    public static final String PROPERTIES_REVIEW_PERIOD="rma:reviewPeriod";
    public static final String PROPERTIES_LOCATION="rma:location";
}
