/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.integration.issue.rm3314;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminServiceImpl;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * Simple bean used to test RM-3314
 * 
 * @author rwetherall
 * @since 2.2.1.5
 */
public class RM3314TestListener implements ApplicationListener<ContextRefreshedEvent>, 
                                           Ordered,
                                           BeanNameAware
{
    private RecordsManagementAdminServiceImpl recordsManagementAdminService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private Repository repository;
    
    private String name;
    private int order = Ordered.LOWEST_PRECEDENCE;

    public void setRecordsManagementAdminService(RecordsManagementAdminServiceImpl recordsManagementAdminService)
    {
        this.recordsManagementAdminService = recordsManagementAdminService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }
    
    @Override
    public void setBeanName(String name)
    {
        this.name = name;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        // call back to show whether the custom map is initialised or not
        RM3314Test.callback.put(name, recordsManagementAdminService.isCustomMapInit());
        
        // Do some work on a node to show that reguardless of whether the custom map is
        // init or not, things still work.
        // Note: using public services to ensure new transaction for each service call        
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                try
                {
                    NodeRef companyHome = repository.getCompanyHome();
                    
                    if (fileFolderService.searchSimple(companyHome, name) == null)
                    {
                        // create node
                        NodeRef folder = fileFolderService.create(
                                    repository.getCompanyHome(), 
                                    name, 
                                    ContentModel.TYPE_FOLDER).getNodeRef();
                        try
                        {                
                            // add aspect
                            nodeService.addAspect(folder, ContentModel.ASPECT_CLASSIFIABLE, null);
                            
                            // remove aspect
                            nodeService.removeAspect(folder, ContentModel.ASPECT_CLASSIFIABLE);
                        }
                        finally
                        {
                            // delete node
                            nodeService.deleteNode(folder);
                        }
                    }
                }
                catch (BadSqlGrammarException e)
                {
                    // ignore and carry on
                }

                return null;
            }
        });
    }

    @Override
    public int getOrder()
    {
        return order;
    }
}
