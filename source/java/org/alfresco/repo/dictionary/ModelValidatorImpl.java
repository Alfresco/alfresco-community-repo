package org.alfresco.repo.dictionary;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Model change validation covering model deletes, model constituent changes e.g. property deletes,
 * additions, etc.
 *  
 * @author sglover
 */
public class ModelValidatorImpl implements ModelValidator
{
    private static final Log logger = LogFactory.getLog(ModelValidatorImpl.class);

    private DictionaryDAO dictionaryDAO;
    private QNameDAO qnameDAO;
    private NamespaceService namespaceService;
    private TransactionService transactionService;
    private WorkflowService workflowService;
    private TenantService tenantService;
    private TenantAdminService tenantAdminService;
    private boolean enforceTenantInNamespace = false;

    public void setEnforceTenantInNamespace(boolean enforceTenantInNamespace)
    {
        this.enforceTenantInNamespace = enforceTenantInNamespace;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    private void checkCustomModelNamespace(M2Model model, String tenantDomain)
    {
        if(tenantDomain != null && !tenantDomain.equals("") && enforceTenantInNamespace)
        {
            // check only for "real" tenants
            for(M2Namespace namespace : model.getNamespaces())
            {
                String namespaceURI = namespace.getUri();
                if(namespaceURI.indexOf(tenantDomain) == -1)
                {
                    throw new DictionaryException("Namespace " + namespaceURI + " does not contain the tenant " + tenantDomain);
                }
            }
        }
    }

    private boolean canDeleteModel(Collection<NamespaceDefinition> namespaceDefs, Collection<TypeDefinition> typeDefs,
            Collection<AspectDefinition> aspectDefs, Tenant tenant)
    {
        boolean canDelete = true;

        String tenantDomain = "for tenant ["
              + (tenant == null ? TenantService.DEFAULT_DOMAIN : tenant.getTenantDomain()) + "]";

        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        
        if (workflowDefs.size() > 0)
        {
            if (namespaceDefs.size() > 0)
            {
                // check workflow namespace usage
                for (WorkflowDefinition workflowDef : workflowDefs)
                {
                    String workflowDefName = workflowDef.getName();
                    
                    String workflowNamespaceURI = null;
                    try
                    {
                        workflowNamespaceURI = QName.createQName(BPMEngineRegistry.getLocalId(workflowDefName), namespaceService).getNamespaceURI();
                    }
                    catch (NamespaceException ne)
                    {
                        logger.warn("Skipped workflow when validating model delete - unknown namespace: "+ne);
                        continue;
                    }
                    
                    for (NamespaceDefinition namespaceDef : namespaceDefs)
                    {
                        if (workflowNamespaceURI.equals(namespaceDef.getUri()))
                        {
                            logger.warn("Failed to validate model delete" + tenantDomain + " - found workflow process definition "
                                    + workflowDefName + " using model namespace '" + namespaceDef.getUri() + "'");
                            canDelete = false;
                        }
                    }
                }
            }
        }
        
        // check for type usages
        outer:
        for (TypeDefinition type : typeDefs)
        {
            try
            {
                validateDeleteClass(tenant, type);
            }
            catch(ModelInUseException e)
            {
                canDelete = false;
                break outer;
            }
            catch(ModelNotInUseException e)
            {
                // ok, continue
            }
        }
        
        // check for aspect usages
        outer:
        for (AspectDefinition aspect : aspectDefs)
        {
            try
            {
                validateDeleteClass(tenant, aspect);
            } catch(ModelInUseException e)
            {
                canDelete = false;
                break outer;
            }
            catch(ModelNotInUseException e)
            {
                // ok, continue
            }
        }

        return canDelete;
    }

    private void validateDeleteClass(final Tenant tenant, final ClassDefinition classDef)
    {
        final String classType = "TYPE";
        final QName className = classDef.getName();

        String tenantDomain = "for tenant ["
                + (tenant == null ? TenantService.DEFAULT_DOMAIN : tenant.getTenantDomain()) + "]";

        // We need a separate transaction to do the qname delete "check"
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                try
                {
                    // The class QName may not have been created in the database if no
                    // properties have been created that use it, so check first and then
                    // try to delete it.
                    if(qnameDAO.getQName(className) != null)
                    {
                        qnameDAO.deleteQName(className);
                    }
                    throw new ModelNotInUseException("Class " + className + " not in use");
                }
                catch(DataIntegrityViolationException e)
                {
                    // catch data integrity violation e.g. foreign key constraint exception
                    logger.debug(e);
                    throw new ModelInUseException("Cannot delete model, class "
                            + className + " is in use");
                }
            }
        }, false, true);


        // check against workflow task usage
        for (WorkflowDefinition workflowDef : workflowService.getDefinitions())
        {
            for (WorkflowTaskDefinition workflowTaskDef : workflowService.getTaskDefinitions(workflowDef.getId()))
            {
                TypeDefinition workflowTypeDef = workflowTaskDef.metadata;
                if (workflowTypeDef.getName().equals(className))
                {
                    throw new AlfrescoRuntimeException("Failed to validate model delete" + tenantDomain + " - found task definition in workflow " 
                            + workflowDef.getName() + " with " + classType + " '" + className + "'");
                }
            }
        }
    }

    private void validateDeleteProperty(QName modelName, QName propertyQName, boolean sharedModel)
    {
        String tenantDomain = TenantService.DEFAULT_DOMAIN;
        if (sharedModel)
        {
            tenantDomain = " for tenant [" + tenantService.getCurrentUserDomain() + "]";
        }

        PropertyDefinition prop = dictionaryDAO.getProperty(propertyQName);
        if(prop != null && prop.getName().equals(propertyQName) && prop.getModel().getName().equals(modelName))
        {
            validateDeleteProperty(tenantDomain, prop);
        }
        else
        {
            throw new AlfrescoRuntimeException("Cannot delete model " + modelName + " in tenant " + tenantDomain
                    + " - property definition '" + propertyQName + "' not defined in model '" + modelName + "'");
        }
    }

    private void validateDeleteProperty(final String tenantDomain, final PropertyDefinition propDef)
    {
        final QName propName = propDef.getName();

        // We need a separate transaction to do the qname delete "check"
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                return TenantUtil.runAsTenant(new TenantRunAsWork<Void>()
                {
                    @Override
                    public Void doWork() throws Exception
                    {
                        try
                        {
                            // The property QName may not have been created in the database if no
                            // properties have been created that use it, so check first and then
                            // try to delete it.
                            if(qnameDAO.getQName(propName) != null)
                            {
                                qnameDAO.deleteQName(propName);
                            }
                        }
                        catch(DataIntegrityViolationException e)
                        {
                            // catch data integrity violation e.g. foreign key constraint exception
                            logger.debug(e);
                            throw new ModelInUseException("Failed to validate property delete, property " + propName + " is in use");
                        }

                        return null;
                    }
                }, tenantDomain);
            }
        }, false, true);
    }

    // validate delete of a referencable constraint def
    private void validateDeleteConstraint(CompiledModel compiledModel, QName constraintName, boolean sharedModel)
    {
        String tenantDomain = TenantService.DEFAULT_DOMAIN;
        if (sharedModel)
        {
            tenantDomain = " for tenant [" + tenantService.getCurrentUserDomain() + "]";
        }
        
        Set<QName> referencedBy = new HashSet<QName>(0);
        
        // check for references to constraint definition
        // note: could be anon prop constraint (if no referenceable constraint)
        Collection<QName> allModels = dictionaryDAO.getModels();
        for (QName model : allModels)
        {
            Collection<PropertyDefinition> propDefs = null;
            if (compiledModel.getModelDefinition().getName().equals(model))
            {
                // TODO deal with multiple pending model updates
                propDefs = compiledModel.getProperties();
            }
            else
            {
                propDefs = dictionaryDAO.getProperties(model);
            }
            
            for (PropertyDefinition propDef : propDefs)
            {
                for (ConstraintDefinition conDef : propDef.getConstraints())
                {
                    if (constraintName.equals(conDef.getRef()))
                    {
                        referencedBy.add(conDef.getName());
                    }
                }
            }
        }
        
        if (referencedBy.size() == 1)
        {
            throw new AlfrescoRuntimeException("Failed to validate constraint delete" + tenantDomain + " - constraint definition '" + constraintName + "' is being referenced by '" + referencedBy.toArray()[0] + "' property constraint");
        }
        else if (referencedBy.size() > 1)
        {
            throw new AlfrescoRuntimeException("Failed to validate constraint delete" + tenantDomain + " - constraint definition '" + constraintName + "' is being referenced by " + referencedBy.size() + " property constraints");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canDeleteModel(final QName modelName)
    {
        boolean canDeleteModel = true;

        // TODO add model locking during delete (would need to be tenant-aware & cluster-aware) to avoid potential 
        //      for concurrent addition of new content/workflow as model is being deleted
        
        final Collection<NamespaceDefinition> namespaceDefs;
        final Collection<TypeDefinition> typeDefs;
        final Collection<AspectDefinition> aspectDefs;
        
        try
        {
            namespaceDefs = dictionaryDAO.getNamespaces(modelName);
            typeDefs = dictionaryDAO.getTypes(modelName);
            aspectDefs = dictionaryDAO.getAspects(modelName);

            // TODO - in case of MT we do not currently allow deletion of an overridden model (with usages) ... but could allow if (re-)inherited model is equivalent to an incremental update only ?
            canDeleteModel &= canDeleteModel(namespaceDefs, typeDefs, aspectDefs, null);
            if(canDeleteModel)
            {
                if (tenantService.isEnabled() && tenantService.isTenantUser() == false)
                {
                    // TODO should fix this up - won't scale
                    // shared model - need to check all tenants (whether enabled or disabled) unless they have overridden
                    List<Tenant> tenants = tenantAdminService.getAllTenants();
                    for (final Tenant tenant : tenants)
                    {
                        // validate model delete within context of tenant domain
                        canDeleteModel &= AuthenticationUtil.runAs(new RunAsWork<Boolean>()
                        {
                            public Boolean doWork()
                            {
                                boolean canDelete = canDeleteModel(namespaceDefs, typeDefs, aspectDefs, tenant);
                                return canDelete;
                            }
                        }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));

                        if(!canDeleteModel)
                        {
                            break;
                        }
                    }
                }
            }
        }
        catch (DictionaryException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Dictionary model '" + modelName + "' does not exist ... skip delete validation : " + e);
            }
            // we must return true here - there is no model 
            canDeleteModel = true;
        }

        return canDeleteModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateModel(CompiledModel compiledModel)
    {
        ModelDefinition modelDef = compiledModel.getModelDefinition();
        QName modelName = modelDef.getName();
        M2Model model = compiledModel.getM2Model();

        checkCustomModelNamespace(model, TenantUtil.getCurrentDomain());

        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(model);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            if (modelDiff.getDiffType().equals(M2ModelDiff.DIFF_DELETED))
            {
                // TODO - check tenants if model is shared / inherited
                if (modelDiff.getElementType().equals(M2ModelDiff.TYPE_PROPERTY))
                {
                    validateDeleteProperty(modelName, modelDiff.getElementName(), false);
                }
                else if (modelDiff.getElementType().equals(M2ModelDiff.TYPE_CONSTRAINT))
                {
                    validateDeleteConstraint(compiledModel, modelDiff.getElementName(), false);
                }
                else
                {
                    throw new AlfrescoRuntimeException("Failed to validate model update - found deleted " + modelDiff.getElementType() + " '" + modelDiff.getElementName() + "'");
                }
            }

            if (modelDiff.getDiffType().equals(M2ModelDiff.DIFF_UPDATED))
            {
                throw new AlfrescoRuntimeException("Failed to validate model update - found non-incrementally updated " + modelDiff.getElementType() + " '" + modelDiff.getElementName() + "'");
            }
        }
        
        // TODO validate that any deleted constraints are not being referenced - else currently will become anon - or push down into model compilation (check backwards compatibility ...)
    }
}
