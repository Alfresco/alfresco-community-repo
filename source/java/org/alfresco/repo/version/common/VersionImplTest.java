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
package org.alfresco.repo.version.common;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionServiceException;
import org.alfresco.service.cmr.version.VersionType;

import junit.framework.TestCase;

/**
 * VersionImpl Unit Test
 * 
 * @author Roy Wetherall
 */
public class VersionImplTest extends TestCase
{
    /**
     * Property names and values
     */
    private final static String PROP_1 = "prop1";
    private final static String PROP_2 = "prop2";
    private final static String PROP_3 = "prop3";
    private final static String VALUE_1 = "value1";
    private final static String VALUE_2 = "value2";
    private final static String VALUE_3 = "value3";  
    private final static String VALUE_DESCRIPTION = "This string describes the version details.";
    private final static VersionType VERSION_TYPE = VersionType.MINOR;
    private final static String USER_NAME = "userName";
    
    /**
     * Version labels
     */
    private final static String VERSION_1 = "1";
    
    /**
     * Data used during tests
     */
    private VersionImpl version = null;
    private NodeRef nodeRef = null;
    private Map<String, Serializable> versionProperties = null;
    private Date createdDate = new Date();

    /**
     * Test case set up
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // Create the node reference
        this.nodeRef = new NodeRef(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "testWS"), "testID");
        assertNotNull(this.nodeRef);
        
        // Create the version property map
        this.versionProperties = new HashMap<String, Serializable>();
        this.versionProperties.put(VersionModel.PROP_VERSION_LABEL, VERSION_1);
        this.versionProperties.put(VersionModel.PROP_CREATED_DATE, this.createdDate);
        this.versionProperties.put(VersionModel.PROP_CREATOR, USER_NAME);
        this.versionProperties.put(Version.PROP_DESCRIPTION, VALUE_DESCRIPTION);
        this.versionProperties.put(VersionModel.PROP_VERSION_TYPE, VERSION_TYPE);
        this.versionProperties.put(PROP_1, VALUE_1);
        this.versionProperties.put(PROP_2, VALUE_2);
        this.versionProperties.put(PROP_3, VALUE_3);
        
        // Create the root version
        this.version = new VersionImpl(this.versionProperties, this.nodeRef);
        assertNotNull(this.version);
    }
    

    /**
     * Test getCreatedDate()
     */
    public void testGetCreatedDate()
    {
        Date createdDate1 = this.version.getCreatedDate();
        assertEquals(this.createdDate, createdDate1);
    }
    
    /**
     * Test getCreator
     */
    public void testGetCreator()
    {
        assertEquals(USER_NAME, this.version.getCreator());
    }

    /**
     * Test getVersionLabel()
     */
    public void testGetVersionLabel()
    {
        String versionLabel1 = this.version.getVersionLabel();
        assertEquals(VersionImplTest.VERSION_1, versionLabel1);
    }
    
    /**
     * Test getDescription
     */
    public void testGetDescription()
    {
        String description = this.version.getDescription();
        assertEquals(VALUE_DESCRIPTION, description);
    }
    
    /**
     * Test getVersionType
     */
    public void testGetVersionType()
    {
        VersionType versionType = this.version.getVersionType();
        assertEquals(VERSION_TYPE, versionType);
    }
    
    /**
     * Test getVersionProperties
     *
     */
    public void testGetVersionProperties()
    {
        Map<String, Serializable> versionProperties = version.getVersionProperties();
        assertNotNull(versionProperties);
        assertEquals(this.versionProperties.size(), versionProperties.size());
    }

    /**
     * Test getVersionProperty
     */
    public void testGetVersionProperty()
    {
        String value1 = (String)version.getVersionProperty(VersionImplTest.PROP_1);
        assertEquals(value1, VersionImplTest.VALUE_1);
        
        String value2 = (String)version.getVersionProperty(VersionImplTest.PROP_2);
        assertEquals(value2, VersionImplTest.VALUE_2);
        
        String value3 = (String)version.getVersionProperty(VersionImplTest.PROP_3);
        assertEquals(value3, VersionImplTest.VALUE_3);
    }

    /**
     * Test getNodeRef()
     */
    public void testGetNodeRef()
    {
        NodeRef nodeRef = this.version.getFrozenStateNodeRef();
        assertNotNull(nodeRef);
        assertEquals(nodeRef.toString(), this.nodeRef.toString());
    }
    
    /**
     * Exception case - no node ref supplied when creating a verison
     */
    public void testNoNodeRefOnVersionCreate()
    {
        try
        {
            new VersionImpl(this.versionProperties, null);
            fail("It is invalid to create a version object without a node ref specified.");
        }
        catch (VersionServiceException exception)
        {
        }
    }    
}
