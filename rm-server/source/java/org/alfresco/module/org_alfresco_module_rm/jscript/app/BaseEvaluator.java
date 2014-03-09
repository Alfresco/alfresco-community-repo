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
package org.alfresco.module.org_alfresco_module_rm.jscript.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Base evaluator.
 *
 * @author Roy Wetherall
 */
public abstract class BaseEvaluator implements RecordsManagementModel
{
    /** Name */
    protected String name;

    /** JSON conversion component */
    protected JSONConversionComponent jsonConversionComponent;

    /** Record service */
    protected RecordService recordService;

    /** Node service */
    protected NodeService nodeService;

    /** Namespace service */
    protected NamespaceService namespaceService;

    /** Capability service */
    protected CapabilityService capabilityService;

    /** Freeze service */
    protected FreezeService freezeService;

    /** File plan component kinds */
    protected Set<FilePlanComponentKind> kinds;

    /** Capabilities */
    protected List<String> capabilities;

    /** File plan service */
    protected FilePlanService filePlanService;

    /** Disposition service */
    protected DispositionService dispositionService;

    /** Record folder service */
    protected RecordFolderService recordFolderService;

    /**
     * @param jsonConversionComponent   json conversion component
     */
    public void setJsonConversionComponent(JSONConversionComponent jsonConversionComponent)
    {
        this.jsonConversionComponent = jsonConversionComponent;
    }

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @param freezeService freeze service
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
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @param recordFolderService   record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param kinds
     */
    public void setKinds(Set<FilePlanComponentKind> kinds)
    {
        this.kinds = kinds;
    }

    /**
     * @param capabilties
     */
    public void setCapabilities(List<String> capabilties)
    {
        this.capabilities = capabilties;
    }

    /**
     * Helper method which sets on capability.
     *
     * @param capability    capability name
     */
    public void setCapability(String capability)
    {
        List<String> list = new ArrayList<String>(1);
        list.add(capability);
        this.capabilities = list;
    }

    /**
     * Registers this instance as an indicator (evaluator)
     */
    public void registerIndicator()
    {
        jsonConversionComponent.registerIndicator(this);
    }

   /**
    * Registers this instance as an action (evaluator)
    */
    public void registerAction()
    {
        jsonConversionComponent.registerAction(this);
    }

    /**
     * Executes the evaluation.
     *
     * @param nodeRef
     * @return
     */
    public boolean evaluate(NodeRef nodeRef)
    {
        boolean result = false;

        // Check that we are dealing with the correct kind of RM object
        if ((kinds == null || checkKinds(nodeRef) == true) &&
                // Check we have the required capabilities
                (capabilities == null || checkCapabilities(nodeRef) == true))
        {
            result = evaluateImpl(nodeRef);
        }

        return result;
    }

    /**
     * Checks the file plan component kind.
     *
     * @param nodeRef
     * @return
     */
    private boolean checkKinds(NodeRef nodeRef)
    {
        FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRef);
        return kinds.contains(kind);
    }

    /**
     * Checks the capabilities.
     *
     * @param nodeRef
     * @return
     */
    private boolean checkCapabilities(NodeRef nodeRef)
    {
        boolean result = true;
        if (capabilities != null && capabilities.isEmpty() == false)
        {
            Map<Capability, AccessStatus> accessStatus = capabilityService.getCapabilitiesAccessState(nodeRef, capabilities);
            for (AccessStatus value : accessStatus.values())
            {
                if (AccessStatus.DENIED.equals(value) == true)
                {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Evaluation execution implementation.
     *
     * @param nodeRef
     * @return
     */
    protected abstract boolean evaluateImpl(NodeRef nodeRef);
}
