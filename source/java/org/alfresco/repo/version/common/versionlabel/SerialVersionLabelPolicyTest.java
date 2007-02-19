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
package org.alfresco.repo.version.common.versionlabel;

import java.io.Serializable;
import java.util.HashMap;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.common.VersionImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;

/**
 * Unit test class for SerialVersionLabelPolicy class
 * 
 * @author Roy Wetherall
 */
public class SerialVersionLabelPolicyTest extends TestCase
{
    /**
     * Test getVersionLabelValue
     */
    public void testGetVersionLabelValue()
    {
        SerialVersionLabelPolicy policy = new SerialVersionLabelPolicy();
        
        NodeRef dummyNodeRef = new NodeRef(new StoreRef("", ""), "");
        
        HashMap<String, Serializable> versionProp1 = new HashMap<String, Serializable>();
        versionProp1.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        
        String initialVersion = policy.calculateVersionLabel(
				ContentModel.TYPE_CMOBJECT,
                null,
                0,
                versionProp1);
        assertEquals("1.0", initialVersion);
        
        HashMap<String, Serializable> versionProp2 = new HashMap<String, Serializable>();
        versionProp2.put(VersionModel.PROP_VERSION_LABEL, "1.0");
        Version version1 = new VersionImpl(versionProp2, dummyNodeRef);
        
        String verisonLabel1 = policy.calculateVersionLabel(
				ContentModel.TYPE_CMOBJECT,                
                version1,
                1,
                versionProp1);
        assertEquals("1.1", verisonLabel1);
        
        HashMap<String, Serializable> versionProp3 = new HashMap<String, Serializable>();
        versionProp3.put(VersionModel.PROP_VERSION_LABEL, "1.1");
        Version version2 = new VersionImpl(versionProp3, dummyNodeRef);
        
        HashMap<String, Serializable> versionProp4 = new HashMap<String, Serializable>();
        versionProp4.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        
        String verisonLabel2 = policy.calculateVersionLabel(
				ContentModel.TYPE_CMOBJECT,
                version2,
                1,
                versionProp4);
        assertEquals("2.0", verisonLabel2);
    }

}
