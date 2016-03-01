 
package org.alfresco.module.org_alfresco_module_rm.patch.v20;

import java.io.Serializable;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.patch.compatibility.ModulePatchComponent;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.0 File Plan Node Ref Patch
 *
 * @author Roy Wetherall
 * @since 2.0
 */
@SuppressWarnings("deprecation")
public class RMv2FilePlanNodeRefPatch extends ModulePatchComponent
                                      implements BeanNameAware, RecordsManagementModel, DOD5015Model
{
    private NodeService nodeService;
    private PatchDAO patchDAO;
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private PermissionService permissionService;
    private FilePlanService filePlanService;
    private FilePlanRoleService filePlanRoleService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param filePlanRoleService	file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
		this.filePlanRoleService = filePlanRoleService;
	}

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executePatch()
    {
    	Pair<Long, QName> aspectPair = qnameDAO.getQName(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT);
        if (aspectPair != null)
        {
            List<Long> filePlanComponents = patchDAO.getNodesByAspectQNameId(aspectPair.getFirst(), 0L, patchDAO.getMaxAdmNodeID());

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("   ... updating " + filePlanComponents.size() + " items");
            }


            for (Long filePlanComponent : filePlanComponents)
            {
                Pair<Long, NodeRef> recordPair = nodeDAO.getNodePair(filePlanComponent);
                NodeRef filePlanComponentNodeRef = recordPair.getSecond();

                NodeRef filePlan =  filePlanService.getFilePlan(filePlanComponentNodeRef);

                if(filePlan != null)
                {           
	                // set the file plan node reference
	                if (nodeService.getProperty(filePlanComponentNodeRef, PROP_ROOT_NODEREF) == null)
	                {
	                   nodeService.setProperty(filePlanComponentNodeRef, PROP_ROOT_NODEREF, filePlan);
	                }
	
	                // only set the admin permissions on record categories, record folders and records
	                FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(filePlanComponentNodeRef);
	                if (FilePlanComponentKind.RECORD_CATEGORY.equals(kind) ||
	                    FilePlanComponentKind.RECORD_FOLDER.equals(kind) ||
	                    FilePlanComponentKind.RECORD.equals(kind))
	                {
	                    // ensure the that the records management role has read and file on the node
	                    Role adminRole = filePlanRoleService.getRole(filePlan, "Administrator");
	                    if (adminRole != null)
	                    {
	                        permissionService.setPermission(filePlanComponentNodeRef, adminRole.getRoleGroupName(), RMPermissionModel.FILING, true);
	                    }
	
	                    // ensure that the default vital record default values have been set (RM-753)
	                    Serializable vitalRecordIndicator = nodeService.getProperty(filePlanComponentNodeRef, PROP_VITAL_RECORD_INDICATOR);
	                    if (vitalRecordIndicator == null)
	                    {
	                        nodeService.setProperty(filePlanComponentNodeRef, PROP_VITAL_RECORD_INDICATOR, false);
	                    }
	                    Serializable reviewPeriod = nodeService.getProperty(filePlanComponentNodeRef, PROP_REVIEW_PERIOD);
	                    if (reviewPeriod == null)
	                    {
	                        nodeService.setProperty(filePlanComponentNodeRef, PROP_REVIEW_PERIOD, new Period("none|0"));
	                    }
	                }                
                }
                else
                {
                    if (LOGGER.isWarnEnabled())
                    {
                        LOGGER.warn("   ... node " + filePlanComponent.toString() + " was skiped, beacuse there was no associated file plan.");
                    }
                }
            }
        }
    }
}
