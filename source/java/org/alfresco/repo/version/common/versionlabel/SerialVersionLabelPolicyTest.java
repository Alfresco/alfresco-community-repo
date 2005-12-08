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
