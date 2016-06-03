/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.links;

import org.alfresco.service.namespace.QName;

/**
 * Links models constants
 * 
 * @author Nick Burch
 */
public interface LinksModel
{
    /** Links Model */
    public static final String LINKS_MODEL_URL = "http://www.alfresco.org/model/linksmodel/1.0";
    public static final String LINKS_MODEL_PREFIX = "lnk";
    
    /** Link */
    public static final QName TYPE_LINK = QName.createQName(LINKS_MODEL_URL, "link"); 
    public static final QName PROP_TITLE = QName.createQName(LINKS_MODEL_URL, "title"); 
    public static final QName PROP_DESCRIPTION = QName.createQName(LINKS_MODEL_URL, "description"); 
    public static final QName PROP_URL = QName.createQName(LINKS_MODEL_URL, "url"); 
    
    /** Internal Link */
    public static final QName ASPECT_INTERNAL_LINK = QName.createQName(LINKS_MODEL_URL, "internal");
    public static final QName PROP_IS_INTERNAL = QName.createQName(LINKS_MODEL_URL, "isInternal");
}