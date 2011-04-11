/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.email.server;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Class defines the costants for Email Server Model
 * 
 * @see alfresco/model/emailServerModel.xml
 * @author Yan O
 * @since 2.2
 */
public interface EmailServerModel
{
    // Attachable aspect
    static final QName ASPECT_ATTACHED = QName.createQName(NamespaceService.EMAILSERVER_MODEL_URI, "attached");

    static final QName ASSOC_ATTACHMENT = QName.createQName(NamespaceService.EMAILSERVER_MODEL_URI, "attachment");

    // Aliasable aspect
    static final QName ASPECT_ALIASABLE = QName.createQName(NamespaceService.EMAILSERVER_MODEL_URI, "aliasable");

    static final QName PROP_ALIAS = QName.createQName(NamespaceService.EMAILSERVER_MODEL_URI, "alias");

}
