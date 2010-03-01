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
package org.alfresco.wcm.sandbox;

import java.util.Date;

import org.alfresco.service.namespace.QName;

/**
*  Provides information about a WCM sandbox created by SandboxFactory.
*/
public interface SandboxInfo
{
    /**
     * Get the name
     * 
     * @return  String  name
     */
    public String getName();
    
    /**
     *  The sandbox store id
     */
    public String getSandboxId();
    
    /**
     *  The web project store id
     */
    public String getWebProjectId();
    
    /**
     *  The sandbox type ... for now a QName, based on existing SandboxConstants
     */
    public QName getSandboxType();
    
    public Date getCreatedDate();

    public String getCreator();
    
    /**
     *   The sandbox root relative path - eg. for WCM web project with webapps, typically /www/avm_webapps
     */
    public String getSandboxRootPath();
    
    /**
    *  A list of ids of the stores within this sandbox.
    *  The "main" store should come first in this list;
    *  any other stores should appear in the order that 
    *  they are overlaid on "main" (e.g.: any "preview" 
    *  layers should come afterward, in "lowest first" order).
    *  <p>
    *  Note: all sandboxes must have a "main" layer.
    */
    public String[] getStoreNames();

    /**
    *  The id of the "main" store within this sandbox.
    */
    public String getMainStoreName();
}
