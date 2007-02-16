/*-----------------------------------------------------------------------------
*  Copyright 2007 Alfresco Inc.
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
 * http://www.alfresco.com/legal/licensing"*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    SandboxInfo.java
*----------------------------------------------------------------------------*/

package org.alfresco.web.bean.wcm;

/**
*  Provides information about a sandbox created by SandboxFactory.
*/
public final class SandboxInfo
{
    String [] store_names_;
    public SandboxInfo(String [] store_names)
    {
        store_names_ = store_names;
    }

    /**
    *  A list of names of the stores within this sandbox.
    *  The "main" store should come first in this list;
    *  any other stores should appear in the order that 
    *  they are overlaid on "main" (e.g.: any "preview" 
    *  layers should come afterward, in "lowest first" order).
    *  <p>
    *  Note: all sandboxes must have a "main" layer.
    */
    public String [] getStoreNames()    { return store_names_; }

    /**
    *  The name of the "main" store within this sandbox.
    */
    public String    getMainStoreName() { return store_names_[0]; }
}
