/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.dictionary;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.AlfrescoCmisServiceFactory;
import org.alfresco.opencmis.CMISTest.SimpleCallContext;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * @author sglover
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:alfresco/application-context.xml"})
public class DictionaryDAOIntegrationTest
{
    public static final String TEST_RESOURCE_MESSAGES = "alfresco/messages/dictionary-messages";

    @Autowired
    private DictionaryService service;

    @Autowired
    private DictionaryDAOImpl dictionaryDAO;

    @Autowired
    private CMISDictionaryService cmisDictionaryService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
	private AlfrescoCmisServiceFactory factory;

    @Autowired
    private NodeService nodeService;
    
    @Autowired
    private FileFolderService fileFolderService;
    
    @Autowired
    private ContentService contentService;

    @Autowired
    private PersonService personService;

    @Autowired
    private MutableAuthenticationService authenticationService;

	private String tenant1;
    private String tenant1Username1;

    private void createUser(final String tenant, final String userName,
    		final String firstName, final String lastName)
    {
        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
        {
			@Override
			public Void doWork() throws Exception
			{
		        if(!authenticationService.authenticationExists(userName))
		        {
		            authenticationService.createAuthentication(userName, "password".toCharArray());
		        }
		        
		        if(!personService.personExists(userName))
		        {
		            PropertyMap ppOne = new PropertyMap(5);
		            ppOne.put(ContentModel.PROP_USERNAME, userName);
		            ppOne.put(ContentModel.PROP_FIRSTNAME, firstName);
		            ppOne.put(ContentModel.PROP_LASTNAME, lastName);
		            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
		            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
		            
		            personService.createPerson(ppOne);
		        }

				return null;
			}
        }, tenant);
    }

    @Before
    public void before()
    {
    	this.tenant1 = "tenant1";
    	this.tenant1Username1 = "user" + System.currentTimeMillis();

    	createUser(tenant1, tenant1Username1, tenant1Username1, tenant1Username1);
    }

    private void addCustomModelToRepository(M2Model customModel)
    		throws UnsupportedEncodingException, FileNotFoundException
    {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		customModel.toXML(out);
		String modelContent = out.toString("UTF-8");
		NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
		FileInfo info = fileFolderService.resolveNamePath(rootNodeRef, Arrays.asList(""));
		NodeRef modelsNodeRef = info.getNodeRef();
        FileInfo fileInfo = fileFolderService.create(
        		modelsNodeRef, "contentModel" + System.currentTimeMillis() + ".xml",
        		ContentModel.TYPE_DICTIONARY_MODEL);
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_MODEL_ACTIVE, Boolean.TRUE);
        nodeService.setProperties(fileInfo.getNodeRef(), properties);
        ContentWriter writer = contentService.getWriter(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.putContent(modelContent);
    }

    @Test
    public void test1()
    {
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
			@Override
			public Void doWork() throws Exception
			{
				M2Model customModel = M2Model.createModel(
						Thread.currentThread().getContextClassLoader().
						getResourceAsStream("dictionary/dictionarytest_model1.xml"));
				addCustomModelToRepository(customModel);

				CallContext context = new SimpleCallContext("user1", "admin", CmisVersion.CMIS_1_1);

				CmisService cmisService = factory.getService(context);
				try
				{
					assertNotNull(cmisService.getTypeDefinition(tenant1, "D:cm:type1", null));
				}
				finally
				{
					cmisService.close();
				}

				return null;
			}
		}, tenant1Username1, tenant1);
    }
}
