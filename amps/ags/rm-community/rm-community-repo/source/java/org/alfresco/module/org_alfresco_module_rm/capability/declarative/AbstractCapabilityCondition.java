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

package org.alfresco.module.org_alfresco_module_rm.capability.declarative;

import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.util.TransactionalResourceHelper;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Abstract capability condition.
 *
 * @author Roy Wetherall
 */
public abstract class AbstractCapabilityCondition implements CapabilityCondition,
                                                             BeanNameAware,
                                                             RecordsManagementModel
{
    /** transaction cache key */
    private static final String KEY_EVALUATE = "rm.transaction.evaluate";
    
    /** Capability condition name */
    protected String name;

    /** Services */
    protected RecordService recordService;
    protected PermissionService permissionService;
    protected NodeService nodeService;
    protected FreezeService freezeService;
    protected FilePlanService filePlanService;
    protected DispositionService dispositionService;
    protected RecordFolderService recordFolderService;
    
    /** transaction resource helper */
    private TransactionalResourceHelper transactionalResourceHelper;

    /**
     * @param recordService     record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param freezeService   freeze service
     */
    public void setFreezeService(FreezeService freezeService)
    {
       this.freezeService = freezeService;
    }

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    /**
     * @param dispositionService disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @param recordFolderService record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }
    
    /**
     * @param transactionalResourceHelper transactional resource helper
     */
    public void setTransactionalResourceHelper(TransactionalResourceHelper transactionalResourceHelper)
    {
        this.transactionalResourceHelper = transactionalResourceHelper;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#getName()
     */
    @Override
    public String getName()
    {
        return name;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluate(NodeRef nodeRef)
    {
        boolean result = false;
        
        // check transaction cache
        Map<String, Boolean> map = transactionalResourceHelper.getMap(KEY_EVALUATE);
        String key = getName() + "|" + nodeRef.toString() + "|" + AuthenticationUtil.getRunAsUser();
        if (map.containsKey(key))
        {
            result = map.get(key);
        }
        else
        {
            result = evaluateImpl(nodeRef);
            map.put(key, result);
        }
        
        return result;
    }
    
    public abstract boolean evaluateImpl(NodeRef nodeRef);

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    @Override
    public void setBeanName(String name)
    {
        this.name = name;
    }
}
