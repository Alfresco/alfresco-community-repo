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
package org.alfresco.repo.version.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionDoesNotExistException;
import org.alfresco.service.cmr.version.VersionServiceException;
import org.alfresco.util.TempFileProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * VersionHistoryImpl Unit Test Class
 * 
 * @author Roy Wetherall
 */
public class VersionHistoryImplTest extends TestCase
{
    /**
     * Data used in the tests
     */
    private NodeRef nodeRef;
    private Version rootVersion = null;    
    private Version childVersion1 = null;
    private Version childVersion2 = null;
    
    /**
     * Set up
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // Create dummy node ref
        nodeRef = new NodeRef(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "test"), "test");
        
        this.rootVersion = newVersion(nodeRef, "1");
        this.childVersion1 = newVersion(nodeRef, "2");
        this.childVersion2 = newVersion(nodeRef, "3");
    }

    private VersionImpl newVersion(NodeRef nodeRef, String label)
    {
        HashMap<String, Serializable> versionProperties1 = new HashMap<String, Serializable>();
        versionProperties1.put(VersionModel.PROP_VERSION_LABEL, label);
        versionProperties1.put(VersionModel.PROP_CREATED_DATE, new Date());
        versionProperties1.put("testProperty", "testValue");
        return new VersionImpl(versionProperties1, nodeRef);
    }
    
    /**
     * Test constructor
     */
    public void testConstructor()
    {
        testContructorImpl();
    }
    
    /**
     * Test construtor helper
     * 
     * @return new version history
     */
    private VersionHistoryImpl testContructorImpl()
    {
        VersionHistoryImpl vh = new VersionHistoryImpl(this.rootVersion, null);
        assertNotNull(vh);
        
        return vh;
    }
    
    /**
     * Exception case - a root version must be specified when creating a 
     *                  version history object
     */
    public void testRootVersionSpecified()
    {
        try
        {
            new VersionHistoryImpl(null, null);
            fail();
        }
        catch(VersionServiceException exception)
        {
        }
    }

    /**
     * Test getRootVersion
     *
     *@return root version
     */
    public void testGetRootVersion()
    {
        VersionHistoryImpl vh = testContructorImpl();
        
        Version rootVersion = vh.getRootVersion();
        assertNotNull(rootVersion);
        assertEquals(rootVersion, this.rootVersion);        
    }
    
    /**
     * Test getAllVersions
     */
    public void testGetAllVersions()
    {
        VersionHistoryImpl vh = testAddVersionImpl();
        
        Collection<Version> allVersions = vh.getAllVersions();
        assertNotNull(allVersions);
        assertEquals(3, allVersions.size());
    }
    
    /**
     * Test getAllVersions using a comparator to resort versions which are in the
     * wrong order.
     */
    public void testGetAllVersionsComparator()
    {
        String[] labels = new String[] { "1.0", "1.1", "1.2", "2.0", "2.1" };
        List<Version> versions = new ArrayList<Version>(labels.length);
        for (String label: labels)
        {
            versions.add(newVersion(nodeRef, label));
        }
        Collections.shuffle(versions);

        Iterator<Version> itr = versions.iterator();
        Version version = itr.next();
        Version predecessor;
        VersionHistoryImpl vh = new VersionHistoryImpl(version,
                Collections.reverseOrder(new VersionLabelComparator()));
        while (itr.hasNext())
        {
            predecessor = version;
            version = itr.next();
            vh.addVersion(version, predecessor);
        }

        Collection<Version> allVersions = vh.getAllVersions();
        assertNotNull(allVersions);
        assertEquals(labels.length, allVersions.size());
        itr = allVersions.iterator();
        for (String label: labels)
        {
            assertEquals(label, itr.next().getVersionLabel());
        }
    }
    
    /**
     * Test addVersion
     * 
     * @return version history
     */
    public void testAddVersion()
    {
        testAddVersionImpl();
    }
    
    /**
     * Test addVersion helper
     * 
     * @return version history with version tree built
     */
    private VersionHistoryImpl testAddVersionImpl()
    {
        VersionHistoryImpl vh = testContructorImpl();
        Version rootVersion = vh.getRootVersion();
        
        vh.addVersion(this.childVersion1, rootVersion);
        vh.addVersion(this.childVersion2, rootVersion);
        
        return vh;
    }
    
    /**
     * TODO Exception case - add version that has already been added
     */
    
    /**
     * TODO Exception case - add a version with a duplicate version label
     */
    
    /**
     * Test getPredecessor
     */
    public void testGetPredecessor()
    {
        VersionHistoryImpl vh = testAddVersionImpl();
        
        Version version1 = vh.getPredecessor(this.childVersion1);
        assertEquals(version1.getVersionLabel(), this.rootVersion.getVersionLabel());
        
        Version version2 = vh.getPredecessor(this.childVersion2);
        assertEquals(version2.getVersionLabel(), this.rootVersion.getVersionLabel());
        
        Version version3 = vh.getPredecessor(this.rootVersion);
        assertNull(version3);
        
        try
        {
            Version version4 = vh.getPredecessor(null);
            assertNull(version4);
        }
        catch (Exception exception)
        {
            fail("Should continue by returning null.");
        }
    }
    
    /**
     * Test getSuccessors
     */
    public void testGetSuccessors()
    {
        VersionHistoryImpl vh = testAddVersionImpl();
        
        Collection<Version> versions1 = vh.getSuccessors(this.rootVersion);
        assertNotNull(versions1);
        assertEquals(versions1.size(), 2);
        
        for (Version version : versions1)
        {
            String versionLabel = version.getVersionLabel();
            if (!(versionLabel == "2" || versionLabel == "3"))
            {
                fail("There is a version in this collection that should not be here.");
            }
        }
        
        Collection<Version> versions2 = vh.getSuccessors(this.childVersion1);
        assertNotNull(versions2);
        assertTrue(versions2.isEmpty());
        
        Collection<Version> versions3 = vh.getSuccessors(this.childVersion2);
        assertNotNull(versions3);
        assertTrue(versions3.isEmpty());
    }
    
    /**
     * Test getSuccessors using a comparator to resort versions which are in the
     * wrong order.
     */
    public void testGetSuccessorsComparator()
    {
        rootVersion = newVersion(nodeRef, "1.0");
        String[] labels = new String[] { "1.1", "1.2", "2.0", "2.1" };
        List<Version> versions = new ArrayList<Version>(labels.length);
        for (String label: labels)
        {
            versions.add(newVersion(nodeRef, label));
        }
        Collections.shuffle(versions);

        Iterator<Version> itr = versions.iterator();
        Version version = rootVersion;
        VersionHistoryImpl vh = new VersionHistoryImpl(version,
                Collections.reverseOrder(new VersionLabelComparator()));
        while (itr.hasNext())
        {
            vh.addVersion(itr.next(), rootVersion);
        }

        Collection<Version> allVersions = vh.getSuccessors(rootVersion);
        assertNotNull(allVersions);
        assertEquals(labels.length, allVersions.size());
        itr = allVersions.iterator();
        for (String label: labels)
        {
            assertEquals(label, itr.next().getVersionLabel());
        }
    }

    /**
     * Test getVersion
     */
    public void testGetVersion()
    {
        VersionHistoryImpl vh = testAddVersionImpl();
       
        Version version1 = vh.getVersion("1");
        assertEquals(version1.getVersionLabel(), this.rootVersion.getVersionLabel());
        
        Version version2 = vh.getVersion("2");
        assertEquals(version2.getVersionLabel(), this.childVersion1.getVersionLabel());
        
        Version version3 = vh.getVersion("3");
        assertEquals(version3.getVersionLabel(), this.childVersion2.getVersionLabel());
        
        try
        {
            vh.getVersion("invalidLabel");
            fail("An exception should have been thrown if the version can not be retrieved.");
        }
        catch (VersionDoesNotExistException exception)
        {
            System.out.println("Error message: " + exception.getMessage());
        }
    }    
    
    /**
     * Checks that the current version can be serialized and deserialized.
     */
    public void testSerialize() throws Exception
    {
        File file = TempFileProvider.createTempFile(getName(), ".bin");
        System.out.println("Test " + getName() + " writing to " + file.getPath());
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));

        VersionHistoryImpl vh = testAddVersionImpl();
        try
        {
            os.writeObject(vh);
        }
        finally
        {
            try { os.close(); } catch (Throwable e) {}
        }
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
        VersionHistoryImpl vhObj;
        try
        {
            vhObj = (VersionHistoryImpl) is.readObject();
        }
        finally
        {
            try { is.close(); } catch (Throwable e) {}
        }
        assertNotNull(vhObj);
        assertNotNull("No root version", vhObj.getRootVersion());
        assertEquals(
                "Deserialized object does not match original",
                vh.getRootVersion().getFrozenStateNodeRef(),
                vhObj.getRootVersion().getFrozenStateNodeRef());
    }
    
    public static final String DESERIALIZE_V22SP4 = "classpath:version-history/VersionHistoryImplTest-testSerialize-V2.2.4.bin";
    public static final String DESERIALIZE_V310_DEV = "classpath:version-history/VersionHistoryImplTest-testSerialize-V3.1.0-dev.bin";
    public static final String DESERIALIZE_V310 = "classpath:version-history/VersionHistoryImplTest-testSerialize-V3.1.0.bin";
    /**
     * @see {@link #DESERIALIZE_V22SP4}
     * @see {@link #DESERIALIZE_V310_DEV}
     * @see {@link #DESERIALIZE_V310}
     */
    public void testDeserializeV22SP4() throws Exception
    {
        String[] resourceLocations = new String[] {
                DESERIALIZE_V22SP4,
                DESERIALIZE_V310_DEV,
                DESERIALIZE_V310
        };
        for (String resourceLocation : resourceLocations)
        {
            Resource resource = new DefaultResourceLoader().getResource(resourceLocation);
            assertNotNull("Unable to find " + resourceLocation, resource);
            assertTrue("Unable to find " + resourceLocation, resource.exists());

            @SuppressWarnings("unused")
            VersionHistoryImpl vhObj;
            ObjectInputStream is = new ObjectInputStream(resource.getInputStream());
            try
            {
                vhObj = (VersionHistoryImpl) is.readObject();
            }
            finally
            {
                try { is.close(); } catch (Throwable e) {}
            }
        }
    }
}
