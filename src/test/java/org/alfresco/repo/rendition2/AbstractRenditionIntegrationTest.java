/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;

/**
 * Class unites common utility methods for {@link org.alfresco.repo.rendition2} package tests.
 */
public abstract class AbstractRenditionIntegrationTest extends BaseSpringTest
{
    @Autowired
    NodeService nodeService;

    @Autowired
    ContentService contentService;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    PersonService personService;

    @Autowired
    MutableAuthenticationService authenticationService;

    static String PASSWORD = "password";

    NodeRef createContentNodeFromQuickFile(String fileName) throws FileNotFoundException
    {
        NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        NodeRef folderNodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(getName() + GUID.generate()),
                ContentModel.TYPE_FOLDER).getChildRef();

        File file = ResourceUtils.getFile("classpath:quick/" + fileName);
        NodeRef contentRef = nodeService.createNode(
                folderNodeRef,
                ContentModel.ASSOC_CONTAINS,
                ContentModel.ASSOC_CONTAINS,
                ContentModel.TYPE_CONTENT,
                Collections.singletonMap(ContentModel.PROP_NAME, fileName))
                .getChildRef();
        ContentWriter contentWriter = contentService.getWriter(contentRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(mimetypeService.guessMimetype(fileName));
        contentWriter.putContent(file);

        return contentRef;
    }

    static String generateNewUsernameString()
    {
        return "user-" + GUID.generate();
    }

    String createRandomUser()
    {
        return AuthenticationUtil.runAs(() ->
        {
            String username = generateNewUsernameString();
            createUser(username);
            return username;
        }, AuthenticationUtil.getAdminUserName());
    }

    void createUser(String username)
    {
        createUser(username, "firstName", "lastName", "jobTitle", 0);
    }

    void createUser(final String username,
                            final String firstName,
                            final String lastName,
                            final String jobTitle,
                            final long quota)
    {
        RetryingTransactionHelper.RetryingTransactionCallback<Void> createUserCallback = () ->
        {
            authenticationService.createAuthentication(username, PASSWORD.toCharArray());

            PropertyMap personProperties = new PropertyMap();
            personProperties.put(ContentModel.PROP_USERNAME, username);
            personProperties.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, "title" + username);
            personProperties.put(ContentModel.PROP_FIRSTNAME, firstName);
            personProperties.put(ContentModel.PROP_LASTNAME, lastName);
            personProperties.put(ContentModel.PROP_EMAIL, username+"@example.com");
            personProperties.put(ContentModel.PROP_JOBTITLE, jobTitle);
            if (quota > 0)
            {
                personProperties.put(ContentModel.PROP_SIZE_QUOTA, quota);
            }
            personService.createPerson(personProperties);
            return null;
        };

        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.doInTransaction(createUserCallback);
    }
}
