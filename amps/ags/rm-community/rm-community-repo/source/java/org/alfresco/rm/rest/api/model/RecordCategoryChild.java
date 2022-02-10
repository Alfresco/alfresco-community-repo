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
 * Concrete class carrying information for a record category child.
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
public class RecordCategoryChild extends RMNode
{
    public static final String PARAM_IS_RECORD_FOLDER = "isRecordFolder";
    public static final String PARAM_IS_RECORD_CATEGORY = "isRecordCategory";
    public static final String PARAM_IS_CLOSED = "isClosed";
    public static final String PARAM_HAS_RETENTION_SCHEDULE = "hasRetentionSchedule";

    private Boolean isRecordCategory;
    private Boolean isRecordFolder;
    private Boolean hasRetentionSchedule;
    private String relativePath;
    private Boolean isClosed;

    public RecordCategoryChild()
    {
        //Default constructor
    }

    public Boolean getIsRecordCategory()
    {
        return isRecordCategory;
    }

    public void setIsRecordCategory(Boolean isRecordCategory)
    {
        this.isRecordCategory = isRecordCategory;
    }

    public Boolean getIsRecordFolder()
    {
        return isRecordFolder;
    }

    public void setIsRecordFolder(Boolean isRecordFolder)
    {
        this.isRecordFolder = isRecordFolder;
    }

    public Boolean getHasRetentionSchedule()
    {
        return hasRetentionSchedule;
    }

    public void setHasRetentionSchedule(Boolean hasRetentionSchedule)
    {
        this.hasRetentionSchedule = hasRetentionSchedule;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public void setRelativePath(String relativePath)
    {
        this.relativePath = relativePath;
    }

    public Boolean getIsClosed()
    {
        return isClosed;
    }

    public void setIsClosed(Boolean isClosed)
    {
        this.isClosed = isClosed;
    }
}
