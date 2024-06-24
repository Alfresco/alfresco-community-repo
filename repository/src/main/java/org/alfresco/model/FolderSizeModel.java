/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.model;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;

@AlfrescoPublicApi
public class FolderSizeModel {

    /** Folder Model URI */
    private static final String SIZE_MODEL_1_0_URI = "http://www.alfresco.org/model/size/1.0";

    public static final QName PROP_STATUS = QName.createQName(SIZE_MODEL_1_0_URI, "status");
    public static final QName PROP_OUTPUT = QName.createQName(SIZE_MODEL_1_0_URI, "result");
}
