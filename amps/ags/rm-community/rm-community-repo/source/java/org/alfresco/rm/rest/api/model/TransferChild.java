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

package org.alfresco.rm.rest.api.model;

/**
 * POJO object carrying information of a transfer child
 * 
 * @author Silviu Dinuta
 * @since 2.6
 */
public class TransferChild extends RMNode
{
    public static final String PARAM_IS_COMPLETED = "isCompleted";
    public static final String PARAM_IS_RECORD_FOLDER = "isRecordFolder";
    public static final String PARAM_IS_RECORD = "isRecord";

    protected Boolean isClosed;
    protected Boolean isRecordFolder;
    protected Boolean isRecord;

    public TransferChild()
    {
        //Default constructor
    }

    public Boolean getIsClosed()
    {
        return isClosed;
    }

    public void setIsClosed(Boolean isClosed)
    {
        this.isClosed = isClosed;
    }

    public Boolean getIsRecordFolder()
    {
        return isRecordFolder;
    }

    public void setIsRecordFolder(Boolean isRecordFolder)
    {
        this.isRecordFolder = isRecordFolder;
    }

    public Boolean getIsRecord()
    {
        return isRecord;
    }

    public void setIsRecord(Boolean isRecord)
    {
        this.isRecord = isRecord;
    }
}
