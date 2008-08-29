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
package org.alfresco.repo.site;

import org.alfresco.service.namespace.QName;

/**
 * Site models constants
 * 
 * @author Roy Wetherall
 */
public interface SiteModel
{
    /** Site Model */
    public static final String SITE_MODEL_URL = "http://www.alfresco.org/model/site/1.0";
    public static final String SITE_MODEL_PREFIX = "st";
    
    /** Site */
    public static final QName TYPE_SITE = QName.createQName(SITE_MODEL_URL, "site");
    public static final QName PROP_SITE_PRESET = QName.createQName(SITE_MODEL_URL, "sitePreset");
    
    /** Site Container */
    public static final QName ASPECT_SITE_CONTAINER = QName.createQName(SITE_MODEL_URL, "siteContainer");
    public static final QName PROP_COMPONENT_ID = QName.createQName(SITE_MODEL_URL, "componentId");
    
    /** Site Permission */
    public static final String SITE_MANAGER = "SiteManager";
    public static final String SITE_COLLABORATOR = "SiteCollaborator";
    public static final String SITE_CONTRIBUTOR = "SiteContributor";
    public static final String SITE_CONSUMER = "SiteConsumer";
}