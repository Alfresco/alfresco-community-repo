/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.security.person;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * Check and fix permission for people. For each person check the permission config matches that configured for the
 * person service.
 * 
 * @author andyh
 */
public class CheckAndFixPersonPermissionsBootstrapBean extends AbstractLifecycleBean
{
    protected final static Log log = LogFactory.getLog(CheckAndFixPersonPermissionsBootstrapBean.class);  
    
    private NodeService nodeService;

    private PersonService personService;

    private TransactionService transactionService;

    private PermissionsManager permissionsManager;

    private Set<String> excludedUsers;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setPermissionsManager(PermissionsManager permissionsManager)
    {
        this.permissionsManager = permissionsManager;
    }
    
    public void setExcludedUsers(Set<String> excludedUsers)
    {
        this.excludedUsers = excludedUsers;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        log.info("Checking person permissions ...");
        int count = checkandFixPermissions();
        log.info("... updated " + count);
    }

    private int checkandFixPermissions()
    {
        Integer count = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Integer>()
        {
            public Integer execute() throws Exception
            {
                int count = 0;
                
                Set<NodeRef> people = personService.getAllPeople();
                for (NodeRef person : people)
                {
                    String uid = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(person, ContentModel.PROP_USERNAME));
                    if((excludedUsers != null) && excludedUsers.contains(uid))
                    {
                        continue;
                    }
                    if(!permissionsManager.validatePermissions(person, uid, uid))
                    {
                        permissionsManager.setPermissions(person, uid, uid);
                        count++;
                    }
                }
                return count;
            }

        });
        return count.intValue();

    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // TODO Auto-generated method stub

    }

}
