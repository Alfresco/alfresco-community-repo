package org.alfresco.repo.dictionary;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class ModelValidatorImpl implements ModelValidator
{
    private static final Log logger = LogFactory.getLog(ModelValidatorImpl.class);

	private DictionaryDAO dictionaryDAO;
	private NamespaceService namespaceService;
	private WorkflowService workflowService;
	private TenantService tenantService;
	private TenantAdminService tenantAdminService;
	private SearchService searchService;

    private List<String> storeUrls; // stores against which model deletes should be validated

    public void setStoreUrls(List<String> storeUrls)
    {
        this.storeUrls = storeUrls;
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

	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

    private void checkCustomModelNamespace(M2Model model, String tenantDomain)
    {
    	if(tenantDomain != null && !tenantDomain.equals(""))
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

	/**
     * validate against repository contents / workflows (e.g. when deleting an existing model)
     * 
     * @param modelName
     */
    public void validateModelDelete(final QName modelName)
    {
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
        }
        catch (DictionaryException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Dictionary model '" + modelName + "' does not exist ... skip delete validation : " + e);
            }
            return;
        }
        
        // TODO - in case of MT we do not currently allow deletion of an overridden model (with usages) ... but could allow if (re-)inherited model is equivalent to an incremental update only ?
        validateModelDelete(namespaceDefs, typeDefs, aspectDefs, false);
        
        if (tenantService.isEnabled() && tenantService.isTenantUser() == false)
        {
            // shared model - need to check all tenants (whether enabled or disabled) unless they have overridden
            List<Tenant> tenants = tenantAdminService.getAllTenants();
            for (Tenant tenant : tenants)
            {
                // validate model delete within context of tenant domain
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        if (dictionaryDAO.isModelInherited(modelName))
                        {
                            validateModelDelete(namespaceDefs, typeDefs, aspectDefs, true);
                        }
                        return null;
                    }
                }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
            }
        }
    }
    
    private void validateModelDelete(Collection<NamespaceDefinition> namespaceDefs, Collection<TypeDefinition> typeDefs, Collection<AspectDefinition> aspectDefs, boolean sharedModel)
    {
        String tenantDomain = TenantService.DEFAULT_DOMAIN;
        if (sharedModel)
        {
            tenantDomain = " for tenant [" + tenantService.getCurrentUserDomain() + "]";
        }
        
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
                            throw new AlfrescoRuntimeException("Failed to validate model delete" + tenantDomain + " - found workflow process definition " + workflowDefName + " using model namespace '" + namespaceDef.getUri() + "'");
                        }
                    }
                }
            }
        }
        
        // check for type usages
        for (TypeDefinition type : typeDefs)
        {
            validateClass(tenantDomain, type);
        }
        
        // check for aspect usages
        for (AspectDefinition aspect : aspectDefs)
        {
            validateClass(tenantDomain, aspect);
        }
    }
    
    private void validateClass(String tenantDomain, ClassDefinition classDef)
    {
    	QName className = classDef.getName();
        
        String classType = "TYPE";
        if (classDef instanceof AspectDefinition)
        {
        	classType = "ASPECT";
        }
        
        for (String storeUrl : this.storeUrls)
        {
            StoreRef store = new StoreRef(storeUrl);
            
            // search for TYPE or ASPECT - TODO - alternative would be to extract QName and search by namespace ...
            ResultSet rs = searchService.query(store, SearchService.LANGUAGE_LUCENE, classType+":\""+className+"\"");
            try
            {
                if (rs.length() > 0)
                {
                    throw new AlfrescoRuntimeException("Failed to validate model delete" + tenantDomain + " - found " + rs.length() + " nodes in store " + store + " with " + classType + " '" + className + "'" );
                }
            }
            finally
            {
                rs.close();
            }
        }
        
        // check against workflow task usage
        for (WorkflowDefinition workflowDef : workflowService.getDefinitions())
        {
            for (WorkflowTaskDefinition workflowTaskDef : workflowService.getTaskDefinitions(workflowDef.getId()))
            {
                TypeDefinition workflowTypeDef = workflowTaskDef.metadata;
                if (workflowTypeDef.getName().equals(className))
                {
                    throw new AlfrescoRuntimeException("Failed to validate model delete" + tenantDomain + " - found task definition in workflow " + workflowDef.getName() + " with " + classType + " '" + className + "'");
                }
            }
        }
    }
    
    /**
     * validate against dictionary
     * 
     * if new model 
     * then nothing to validate
     * 
     * else if an existing model 
     * then could be updated (or unchanged) so validate to currently only allow incremental updates
     *   - addition of new types, aspects (except default aspects), properties, associations
     *   - no deletion of types, aspects or properties or associations
     *   - no addition, update or deletion of default/mandatory aspects
     * 
     * @paramn modelName
     * @param newOrUpdatedModel
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
                    validatePropertyDelete(modelName, modelDiff.getElementName(), false);
                    
                    continue;
                }
                else if (modelDiff.getElementType().equals(M2ModelDiff.TYPE_CONSTRAINT))
                {
                    validateConstraintDelete(compiledModel, modelDiff.getElementName(), false);
                    continue;
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
    
    private void validatePropertyDelete(QName modelName, QName propertyName, boolean sharedModel)
    {
        String tenantDomain = TenantService.DEFAULT_DOMAIN;
        if (sharedModel)
        {
            tenantDomain = " for tenant [" + tenantService.getCurrentUserDomain() + "]";
        }
        
        boolean found = false;
        
        // check for property usages
        for (PropertyDefinition prop : dictionaryDAO.getProperties(modelName, null))
        {
            // TODO ... match property
            if (prop.getName().equals(propertyName))
            {
                // found
                found = true;
                validateIndexedProperty(tenantDomain, prop);
                break;
            }
        }
        
        if (! found)
        {
            throw new AlfrescoRuntimeException("Failed to validate property delete" + tenantDomain + " - property definition '" + propertyName + "' not defined in model '" + modelName + "'");
        }
    }
    
    private void validateIndexedProperty(String tenantDomain, PropertyDefinition propDef)
    {
        QName propName = propDef.getName();
        
        if (! propDef.isIndexed())
        {
            // TODO ... implement DB-level referential integrity
            throw new AlfrescoRuntimeException("Failed to validate property delete" + tenantDomain + " - cannot delete unindexed property definition '" + propName);
        }
        
        for (String storeUrl : this.storeUrls)
        {
            StoreRef store = new StoreRef(storeUrl);
            
            // search for indexed PROPERTY
            String escapePropName = propName.toPrefixString().replace(":", "\\:");
            ResultSet rs = searchService.query(store, SearchService.LANGUAGE_LUCENE, "@"+escapePropName+":*");
            try
            {
                if (rs.length() > 0)
                {
                    throw new AlfrescoRuntimeException("Failed to validate property delete" + tenantDomain + " - found " + rs.length() + " nodes in store " + store + " with PROPERTY '" + propName + "'" );
                }
            }
            finally
            {
                rs.close();
            }
        }
    }
    
    // validate delete of a referencable constraint def
    private void validateConstraintDelete(CompiledModel compiledModel, QName constraintName, boolean sharedModel)
    {
        String tenantDomain = TenantService.DEFAULT_DOMAIN;
        if (sharedModel)
        {
            tenantDomain = " for tenant [" + tenantService.getCurrentUserDomain() + "]";
        }
        
        Set<QName> referencedBy = new HashSet<QName>(0);
        
        // check for references to constraint definition
        // note: could be anon prop constraint (if no referenceable constraint)
        Collection<QName> allModels = dictionaryDAO.getModels(true);
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
}
