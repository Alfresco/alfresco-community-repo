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

package org.alfresco.module.org_alfresco_module_rm.compatibility;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.service.namespace.QName;

/**
 * RM 1.0 compatibility model
 *
 * @author Roy Wetherall
 */
public interface CompatibilityModel extends DOD5015Model
{
    // Record series DOD type
    QName TYPE_RECORD_SERIES = QName.createQName(DOD_URI, "recordSeries");

    // V1.0 custom property aspect names
    String NAME_CUSTOM_RECORD_PROPERTIES = "customRecordProperties";
    String NAME_CUSTOM_RECORD_FOLDER_PROPERTIES = "customRecordFolderProperties";
    String NAME_CUSTOM_RECORD_CATEGORY_PROPERTIES = "customRecordCategoryProperties";
    String NAME_CUSTOM_RECORD_SERIES_PROPERTIES = "customRecordSeriesProperties";
}
