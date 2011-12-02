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
package org.alfresco.repo.cmis.ws.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.ws.Holder;

import junit.framework.TestCase;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.repo.cmis.ws.CmisException;
import org.alfresco.repo.cmis.ws.CmisExtensionType;
import org.alfresco.repo.cmis.ws.CmisPropertiesType;
import org.alfresco.repo.cmis.ws.CmisPropertyId;
import org.alfresco.repo.cmis.ws.CmisPropertyString;
import org.alfresco.repo.cmis.ws.ObjectServicePort;
import org.alfresco.repo.cmis.ws.PolicyServicePort;
import org.alfresco.repo.cmis.ws.RepositoryServicePort;
import org.alfresco.repo.cmis.ws.VersioningServicePort;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.Assert;
import org.springframework.context.ApplicationContext;

/**
 * Base class for tests to Alfresco CMIS WebService API extensions.
 * 
 * @author dward
 */
public abstract class BaseCMISTest extends TestCase
{
    protected RepositoryServicePort repositoryServicePort;
    protected ObjectServicePort objectServicePort;
    protected VersioningServicePort versioningServicePort;
    protected PolicyServicePort policyServicePort;
    protected String defaultRunAs = "admin";
    protected String repositoryId;
    protected String testFolderId;

    protected ApplicationContext ctx;

    public BaseCMISTest()
    {
        ctx = ApplicationContextHelper.getApplicationContext(new String[]
        {
            ApplicationContextHelper.CONFIG_LOCATIONS[0], "classpath:alfresco/web-services-application-context.xml"
        });
        repositoryServicePort = (RepositoryServicePort) ctx.getBean("dmRepositoryService");
        objectServicePort = (ObjectServicePort) ctx.getBean("dmObjectService");
        versioningServicePort = (VersioningServicePort) ctx.getBean("dmVersioningService");
        policyServicePort = (PolicyServicePort) ctx.getBean("dmPolicyService");
        try
        {
            repositoryId = repositoryServicePort.getRepositories(null).get(0).getRepositoryId();
        }
        catch (CmisException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDefaultRunAs(String defaultRunAs)
    {
        this.defaultRunAs = defaultRunAs;
    }

    @Override
    protected void setUp() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(defaultRunAs);
        String rootFolderId = repositoryServicePort.getRepositoryInfo(repositoryId, null).getRootFolderId();
        Holder<String> objectId = new Holder<String>();
        String folderName = getClass().getSimpleName() + System.currentTimeMillis() + " - " + getName();
        objectServicePort.createFolder(repositoryId, createObjectProperties(folderName, "cmis:folder"), rootFolderId,
                null, null, null, new Holder<CmisExtensionType>(), objectId);
        testFolderId = objectId.value;
    }

    @Override
    protected void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    protected CmisPropertiesType createObjectProperties(String name, String type)
    {
        CmisPropertiesType properties = new CmisPropertiesType();
        CmisPropertyString stringProperty = new CmisPropertyString();
        stringProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_NAME);
        stringProperty.getValue().add(name);
        properties.getProperty().add(stringProperty);
        CmisPropertyId idProperty = new CmisPropertyId();
        idProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        idProperty.getValue().add(type);
        properties.getProperty().add(idProperty);
        return properties;
    }

    protected void setStringProperty(CmisPropertiesType properties, String id, String value)
    {
        CmisPropertyString stringProperty = new CmisPropertyString();
        properties.getProperty().add(stringProperty);
        stringProperty.setPropertyDefinitionId(id);
        stringProperty.getValue().add(value);
    }

    protected void assertContains(Set<String> actual, String... expected)
    {
        Assert.assertTrue(actual.containsAll(Arrays.asList(expected)));
    }

    protected void assertDoesNotContain(Set<String> actual, String... unexpected)
    {
        Set<String> copy = new HashSet<String>(actual);
        copy.retainAll(Arrays.asList(unexpected));
        Assert.assertTrue(copy.isEmpty());
    }
}
