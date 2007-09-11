/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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

    // Aspect emailed
    static final QName ASPECT_EMAILED = QName.createQName(NamespaceService.EMAILSERVER_MODEL_URI, "emailed");
}
