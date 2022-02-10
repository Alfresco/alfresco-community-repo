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

package org.alfresco.module.org_alfresco_module_rm.test.util;

import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 * @since 2.3
 */
public interface TestModel
{
    public static final String TEST_URI = "http://www.alfresco.org/model/rmtest/1.0";
    public static final String TEST_PREFIX = "rmt";
    
    public static final QName ASPECT_RECORD_METADATA = QName.createQName(TEST_URI, "recordMetaData");
    public static final QName PROPERTY_RECORD_METADATA = QName.createQName(TEST_URI, "recordMetaDataProperty");
    public static final QName NOT_RM_FOLDER_TYPE = QName.createQName(TEST_URI, "notRmFolderType");
}
