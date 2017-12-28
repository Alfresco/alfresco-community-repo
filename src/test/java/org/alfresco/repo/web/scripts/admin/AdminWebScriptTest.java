/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.admin;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.alfresco.service.cmr.admin.RepoUsageStatus;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the admin web scripts
 * 
 * @author Derek Hulley
 * @since 3.4
 */
@Category(OwnJVMTestsCategory.class)
public class AdminWebScriptTest extends BaseWebScriptTest
{
    private ApplicationContext ctx;
    private RepoAdminService repoAdminService;
    private DescriptorService descriptorService;
    private String admin;
    private String guest;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ctx = getServer().getApplicationContext();
        repoAdminService = (RepoAdminService) ctx.getBean("RepoAdminService");
        descriptorService = (DescriptorService) ctx.getBean("DescriptorService");
        admin = AuthenticationUtil.getAdminUserName();
        guest = AuthenticationUtil.getGuestUserName();

        AuthenticationUtil.setFullyAuthenticatedUser(admin);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testGetRestrictions() throws Exception
    {
        RepoUsage restrictions = repoAdminService.getRestrictions();
        
        String url = "/api/admin/restrictions";
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        
        Response response = sendRequest(req, Status.STATUS_OK, guest);
        JSONObject json = new JSONObject(response.getContentAsString());
        Long maxUsers = json.isNull(AbstractAdminWebScript.JSON_KEY_USERS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_USERS);
        assertEquals("Mismatched max users", restrictions.getUsers(), maxUsers);
        Long maxDocuments = json.isNull(AbstractAdminWebScript.JSON_KEY_DOCUMENTS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_DOCUMENTS);
        assertEquals("Mismatched max documents", restrictions.getDocuments(), maxDocuments);
    }
    
    public void testGetUsage() throws Exception
    {
        RepoUsageStatus usageStatus = repoAdminService.getUsageStatus();
        RepoUsage usage = usageStatus.getUsage();
        LicenseDescriptor licenseDescriptor = descriptorService.getLicenseDescriptor();
        Date validUntil = (licenseDescriptor == null) ? null : licenseDescriptor.getValidUntil(); // might be null
        Integer checkLevel = new Integer(usageStatus.getLevel().ordinal());
        
        String url = "/api/admin/usage";
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(url);
        
        Response response = sendRequest(req, Status.STATUS_OK, guest);
        System.out.println(response.getContentAsString());
        JSONObject json = new JSONObject(response.getContentAsString());
        Long users = json.isNull(AbstractAdminWebScript.JSON_KEY_USERS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_USERS);
        assertEquals("Mismatched users", usage.getUsers(), users);
        Long documents = json.isNull(AbstractAdminWebScript.JSON_KEY_DOCUMENTS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_DOCUMENTS);
        assertEquals("Mismatched documents", usage.getDocuments(), documents);
        String licenseMode = json.isNull(AbstractAdminWebScript.JSON_KEY_LICENSE_MODE) ? null : json.getString(AbstractAdminWebScript.JSON_KEY_LICENSE_MODE);
        assertEquals("Mismatched licenseMode", usage.getLicenseMode().toString(), licenseMode);
        boolean readOnly = json.getBoolean(AbstractAdminWebScript.JSON_KEY_READ_ONLY);
        assertEquals("Mismatched readOnly", usage.isReadOnly(), readOnly);
        boolean updated = json.getBoolean(AbstractAdminWebScript.JSON_KEY_UPDATED);
        assertEquals("Mismatched updated", false, updated);
        Long licenseValidUntil = json.isNull(AbstractAdminWebScript.JSON_KEY_LICENSE_VALID_UNTIL) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_LICENSE_VALID_UNTIL);
        assertEquals("Mismatched licenseValidUntil",
                (validUntil == null) ? null : validUntil.getTime(),
                licenseValidUntil);
        Integer level = json.isNull(AbstractAdminWebScript.JSON_KEY_LEVEL) ? null : json.getInt(AbstractAdminWebScript.JSON_KEY_LEVEL);
        assertEquals("Mismatched level", checkLevel, level);
        json.getJSONArray(AbstractAdminWebScript.JSON_KEY_WARNINGS);
        json.getJSONArray(AbstractAdminWebScript.JSON_KEY_ERRORS);
    }
    
    public void testUpdateUsageWithoutPermissions() throws Exception
    {
        String url = "/api/admin/usage";
        TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
        sendRequest(req, 401, AuthenticationUtil.getGuestRoleName());
    }
    
    public void testUpdateUsage() throws Exception
    {
        repoAdminService.updateUsage(UsageType.USAGE_ALL);
        RepoUsage usage = repoAdminService.getUsage();
        
        String url = "/api/admin/usage";
        TestWebScriptServer.PostRequest req = new TestWebScriptServer.PostRequest(url, "", MimetypeMap.MIMETYPE_JSON);
        
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        System.out.println(response.getContentAsString());
        JSONObject json = new JSONObject(response.getContentAsString());
        Long users = json.isNull(AbstractAdminWebScript.JSON_KEY_USERS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_USERS);
        assertEquals("Mismatched users", usage.getUsers(), users);
        Long documents = json.isNull(AbstractAdminWebScript.JSON_KEY_DOCUMENTS) ? null : json.getLong(AbstractAdminWebScript.JSON_KEY_DOCUMENTS);
        assertEquals("Mismatched documents", usage.getDocuments(), documents);
        String licenseMode = json.isNull(AbstractAdminWebScript.JSON_KEY_LICENSE_MODE) ? null : json.getString(AbstractAdminWebScript.JSON_KEY_LICENSE_MODE);
        assertEquals("Mismatched licenseMode", usage.getLicenseMode().toString(), licenseMode);
        boolean readOnly = json.getBoolean(AbstractAdminWebScript.JSON_KEY_READ_ONLY);
        assertEquals("Mismatched readOnly", usage.isReadOnly(), readOnly);
        boolean updated = json.getBoolean(AbstractAdminWebScript.JSON_KEY_UPDATED);
        assertEquals("Mismatched updated", true, updated);
    }

    @Test
    // ALF-21950 We check now if the property belongs to the type of the node
    public void testResidualProperties() throws Exception
    {
        NodeBrowserPost nodeBrowserPost = new NodeBrowserPost();
        DictionaryService dictionaryService = mock(DictionaryService.class);
        NodeService nodeService = mock(NodeService.class);
        nodeBrowserPost.setDictionaryService(dictionaryService);
        nodeBrowserPost.setNodeService(nodeService);

        // make own class definition from origin type
        ClassDefinition classDefinition = mock(ClassDefinition.class);
        when(dictionaryService.getClass(any())).thenReturn(classDefinition);

        QName qnameResidualFalse1 = QName.createQName("testResidualProperties", "residualFalse1");
        QName qnameResidualFalse2 = QName.createQName("testResidualProperties", "residualFalse2");
        QName qnameResidualTrue1 = QName.createQName("testResidualProperties", "residualTrue1");
        QName qnameResidualTrue2 = QName.createQName("testResidualProperties", "residualTrue2");

        // Define residual False properties inside of class definition
        // That simulates the belonging of the properties to a given type of a node
        Map<QName, PropertyDefinition> properties = new HashMap<>();
        properties.put(qnameResidualFalse1, new SimplePropertyDefinition(false));
        properties.put(qnameResidualFalse2, new SimplePropertyDefinition(true));
        when(classDefinition.getProperties()).thenReturn(properties);

        when(dictionaryService.getProperty(eq(qnameResidualFalse1))).thenReturn(new SimplePropertyDefinition(false));
        when(dictionaryService.getProperty(eq(qnameResidualFalse2))).thenReturn(new SimplePropertyDefinition(true));
        when(dictionaryService.getProperty(eq(qnameResidualTrue1))).thenReturn(new SimplePropertyDefinition(true));
        when(dictionaryService.getProperty(eq(qnameResidualTrue2))).thenReturn(new SimplePropertyDefinition(false));

        // property found in definition so it is not residual
        String value = "abc";
        NodeBrowserPost.Property property = nodeBrowserPost.new Property(qnameResidualFalse1, value);
        assertFalse(property.getResidual());

        // property belongs to an aspect so it is not residual
        property = nodeBrowserPost.new Property(qnameResidualFalse2, value);
        assertFalse(property.getResidual());

        // property not found in definition but it is an aspect so it is not residual
        property = nodeBrowserPost.new Property(qnameResidualTrue1, value);
        assertFalse(property.getResidual());

        // property not found in definition so it is residual
        property = nodeBrowserPost.new Property(qnameResidualTrue2, value);
        assertTrue(property.getResidual());
    }

    private class SimplePropertyDefinition implements PropertyDefinition
    {
        private boolean isAspect;
        public SimplePropertyDefinition(boolean isAspect)
        {
            super();
            this.isAspect = isAspect;
        }

        public ModelDefinition getModel()
        {
            return null;
        }

        public QName getName()
        {
            return null;
        }

        public String getTitle()
        {
            return null;
        }

        public String getDescription()
        {
            return null;
        }

        public String getTitle(MessageLookup messageLookup)
        {
            return null;
        }

        public String getTitle(MessageLookup messageLookup, Locale locale)
        {
            return null;
        }

        public String getDescription(MessageLookup messageLookup)
        {
            return null;
        }

        public String getDescription(MessageLookup messageLookup, Locale locale)
        {
            return null;
        }

        public String getDefaultValue()
        {
            return null;
        }

        public DataTypeDefinition getDataType()
        {
            DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
            when(dataTypeDefinition.getName()).thenReturn(QName.createQName("notImportant"));
            return dataTypeDefinition;
        }

        public ClassDefinition getContainerClass()
        {
            ClassDefinition classDefinition = mock(ClassDefinition.class);
            when(classDefinition.isAspect()).thenReturn(isAspect);
            return classDefinition;
        }

        public boolean isOverride()
        {
            return false;
        }

        public boolean isMultiValued()
        {
            return false;
        }

        public boolean isMandatory()
        {
            return false;
        }

        public boolean isMandatoryEnforced()
        {
            return false;
        }

        public boolean isProtected()
        {
            return false;
        }

        public boolean isIndexed()
        {
            return false;
        }

        public boolean isStoredInIndex()
        {
            return false;
        }

        public IndexTokenisationMode getIndexTokenisationMode()
        {
            return null;
        }

        public Facetable getFacetable()
        {
            return null;
        }

        public boolean isIndexedAtomically()
        {
            return false;
        }

        public List<ConstraintDefinition> getConstraints()
        {
            return null;
        }

        public String getAnalyserResourceBundleName()
        {
            return null;
        }

        public String resolveAnalyserClassName(Locale locale)
        {
            return null;
        }

        public String resolveAnalyserClassName()
        {
            return null;
        }
    }
}
