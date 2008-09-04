/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws;

import javax.transaction.UserTransaction;

import org.alfresco.cmis.CMISService;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Base class for all CMIS tests
 *
 * @author Dmitry Lazurkin
 *
 */
public class BaseServicePortTest extends AbstractDependencyInjectionSpringContextTests
{
    protected AuthenticationService authenticationService;
    protected TransactionService transactionService;
    protected NodeService nodeService;
    protected ServiceRegistry serviceRegistry;
    protected DictionaryService dictionaryService;

    protected CMISMapping cmisMapping;
    protected CMISService cmisService;

    protected AuthenticationComponent authenticationComponent;

    private UserTransaction txn;

    protected NodeRef rootNodeRef;

    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();

        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);

        authenticationService = serviceRegistry.getAuthenticationService();
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        dictionaryService = serviceRegistry.getDictionaryService();

        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");

        cmisService = (CMISService) applicationContext.getBean("CMISService");
        cmisMapping = (CMISMapping) applicationContext.getBean("CMISMapping");

        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        // start the transaction
        txn = transactionService.getUserTransaction();
        txn.begin();

        IntegrityChecker.setWarnInTransaction();

        // authenticate
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        // create a test store if need
        rootNodeRef = nodeService.createNode(cmisService.getDefaultRootNodeRef(), ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.ALFRESCO_URI, "cmis_test_folder"),
                ContentModel.TYPE_FOLDER).getChildRef();

        authenticationComponent.clearCurrentSecurityContext();
    }

    @Override
    protected void onTearDown() throws Exception
    {
        try
        {
            txn.rollback();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected String[] getConfigLocations()
    {
        return new String[] { "classpath:alfresco/application-context.xml", "classpath:alfresco/test-cmis-context.xml" };
    }

    protected String getPropertyIDValue(PropertiesType properties, String propertyName)
    {
        String result = null;
        String realPropertyName = CMISPropNamesMapping.getResponsePropertyName(propertyName);

        for (PropertyIDType property : properties.getPropertyID())
        {
            if (realPropertyName.equals(property.getName()))
            {
                result = property.getValue();
                break;
            }
        }

        return result;
    }

    protected String getPropertyStringValue(PropertiesType properties, String propertyName)
    {
        String result = null;
        String realPropertyName = CMISPropNamesMapping.getResponsePropertyName(propertyName);

        for (PropertyStringType property : properties.getPropertyString())
        {
            if (realPropertyName.equals(property.getName()))
            {
                result = property.getValue();
                break;
            }
        }

        return result;
    }

    protected boolean getPropertyBooleanValue(PropertiesType properties, String propertyName)
    {
        boolean result = false;
        String realPropertyName = CMISPropNamesMapping.getResponsePropertyName(propertyName);

        for (PropertyBooleanType property : properties.getPropertyBoolean())
        {
            if (realPropertyName.equals(property.getName()))
            {
                result = property.isValue();
                break;
            }
        }

        return result;
    }

}
