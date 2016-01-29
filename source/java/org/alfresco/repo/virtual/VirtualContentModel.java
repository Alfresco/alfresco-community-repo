/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual;

import org.alfresco.service.namespace.QName;

/**
 * Virtual Content Model Constants
 *
 * @author Bogdan Horje
 */
public interface VirtualContentModel
{
    static final String VIRTUAL_CONTENT_MODEL_1_0_URI = "http://www.alfresco.org/model/content/smartfolder/1.0";

    static final QName ASPECT_VIRTUAL = QName.createQName(VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                          "smartFolder");

    static final QName ASPECT_VIRTUAL_DOCUMENT = QName.createQName(VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                                   "smartFolderChild");

    static final QName TYPE_VIRTUAL_FOLDER_TEMPLATE = QName.createQName(VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                                        "smartFolderTemplate");

    static final QName PROP_ACTUAL_NODE_REF = QName.createQName(VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                                "actualNodeRef");
}
