/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.module;

import org.alfresco.util.VersionNumber;

/**
 * Module details, contains the details of an installed alfresco
 * module.
 * 
 * @author Roy Wetherall
 */
public class ModuleDetails
{
    private String id;
    private VersionNumber version;
    private String title;
    private String description;
    
    public ModuleDetails(String id, VersionNumber version, String title, String description)
    {
        this.id = id;
        this.version = version;
        this.title = title;
    }
    
    public String getId()
    {
        return id;
    }
    
    public VersionNumber getVersion()
    {
        return version;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public String getDescription()
    {
        return description;
    }
}
