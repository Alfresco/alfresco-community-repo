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
