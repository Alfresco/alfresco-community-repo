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
package org.alfresco.rest.rm.community.model.fileplancomponents;

/**
 * File plan component type
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class FilePlanComponentType
{
    public static final String FILE_PLAN_TYPE = "rma:filePlan";
    public static final String RECORD_CATEGORY_TYPE = "rma:recordCategory";
    public static final String RECORD_FOLDER_TYPE = "rma:recordFolder";
    public static final String RECORD_TYPE = "rma:record"; // generic record type
    public static final String UNFILED_RECORD_FOLDER_TYPE = "rma:unfiledRecordFolder";
    public static final String TRANSFER_TYPE = "rma:transfer";
    public static final String TRANSFER_CONTAINER_TYPE = "rma:transferContainer";
    public static final String UNFILED_CONTAINER_TYPE = "rma:unfiledRecordContainer";
    public static final String FOLDER_TYPE = "cm:folder";
    public static final String CONTENT_TYPE = "cm:content";
    public static final String NON_ELECTRONIC_RECORD_TYPE = "rma:nonElectronicDocument";
}
