/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.i18n.MessageServiceImpl;
import org.alfresco.repo.usage.RepoUsageComponent;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.alfresco.service.cmr.admin.RepoUsageStatus;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Repository Admin Service Implementation.
 * <p>
 * @see RepoAdminService interface
 */
public class RepoAdminServiceImpl implements RepoAdminService
{   
    // Logging support
    private static Log logger = LogFactory.getLog(RepoAdminServiceImpl.class);
    
    // dependencies  
    private DictionaryDAO dictionaryDAO;
    private SearchService searchService;
    private NodeService nodeService;
    private ContentService contentService;
    private NamespaceService namespaceService;
    private MessageService messageService;
    private RepoUsageComponent repoUsageComponent;
    
    private RepositoryLocation repoModelsLocation;
    private RepositoryLocation repoMessagesLocation;
    
    public final static String CRITERIA_ALL = "/*"; // immediate children only
   
    public final static String defaultSubtypeOfDictionaryModel = "subtypeOf('cm:dictionaryModel')";
    public final static String defaultSubtypeOfContent = "subtypeOf('cm:content')";
    
    
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
 
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setMessageService(MessageService messageService)
    {
        this.messageService = messageService;
    }

    public void setRepoUsageComponent(RepoUsageComponent repoUsageComponent)
    {
        this.repoUsageComponent = repoUsageComponent;
    }

    public void setRepositoryModelsLocation(RepositoryLocation repoModelsLocation)
    {
        this.repoModelsLocation = repoModelsLocation;
    }
    
    public void setRepositoryMessagesLocation(RepositoryLocation repoMessagesLocation)
    {
        this.repoMessagesLocation = repoMessagesLocation;
    }
    
    public List<RepoModelDefinition> getModels()
    {
        StoreRef storeRef = repoModelsLocation.getStoreRef();
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        
        List<RepoModelDefinition> modelsInRepo = new ArrayList<RepoModelDefinition>();
        
        Collection<QName> models = dictionaryDAO.getModels();  
        
        List<String> dictionaryModels = new ArrayList<String>();
        for (QName model : models)
        {
            dictionaryModels.add(model.toPrefixString());
        }
        
        List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, repoModelsLocation.getPath()+CRITERIA_ALL+"["+defaultSubtypeOfDictionaryModel+"]", null, namespaceService, false);
        
        if (nodeRefs.size() > 0)
        {
            for (NodeRef nodeRef : nodeRefs)
            {
                String modelFileName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                String repoVersion = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
                
                try
                {
                    String modelName = null;
                    
                    ContentReader cr = contentService.getReader(nodeRef, ContentModel.TYPE_CONTENT);
                    
                    if (cr != null)
                    {
                        InputStream is = cr.getContentInputStream();
                        try
                        {
                            M2Model model = M2Model.createModel(is);
                            modelName = model.getName();
                        }
                        finally
                        {
                            is.close();
                        }
                    }
                    
                    // check against models loaded in dictionary and give warning if not found
                    if (dictionaryModels.contains(modelName))
                    {
                        // note: uses dictionary model cache, rather than getting content from repo and re-compiling
                        modelsInRepo.add(new RepoModelDefinition(modelFileName, repoVersion, dictionaryDAO.getModel(QName.createQName(modelName, namespaceService)), true));
                    }
                    else
                    {
                        modelsInRepo.add(new RepoModelDefinition(modelFileName, repoVersion, null, false));
                    }
                }
                catch (Throwable t)
                {
                    logger.warn("Skip model: "+modelFileName+" ("+t.getMessage()+")");
                }
            }
        }
        
        return modelsInRepo;
    }
        
    public void deployModel(InputStream modelStream, String modelFileName)
    {     
        try
        {   
            // Check that all the passed values are not null
            ParameterCheck.mandatory("ModelStream", modelStream);
            ParameterCheck.mandatoryString("ModelFileName", modelFileName);
            
            Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
            contentProps.put(ContentModel.PROP_NAME, modelFileName);
            
            StoreRef storeRef = repoModelsLocation.getStoreRef();
            NodeRef rootNode = nodeService.getRootNode(storeRef);
            
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, repoModelsLocation.getPath(), null, namespaceService, false);
            
            if (nodeRefs.size() == 0)
            {
                throw new AlfrescoRuntimeException("Could not find custom models location " + repoModelsLocation.getPath());
            }
            else if (nodeRefs.size() > 1)
            {
                // unexpected: should not find multiple nodes with same name
                throw new AlfrescoRuntimeException("Found multiple custom models location " + repoModelsLocation.getPath());
            }
            
            NodeRef customModelsSpaceNodeRef = nodeRefs.get(0);
            
            nodeRefs = searchService.selectNodes(customModelsSpaceNodeRef, "*[@cm:name='"+modelFileName+"' and "+defaultSubtypeOfDictionaryModel+"]", null, namespaceService, false);
            
            NodeRef modelNodeRef = null;
            	
            if (nodeRefs.size() == 1)
            {
                // re-deploy existing model to the repository       
            	
                modelNodeRef = nodeRefs.get(0);
            }
            else
            {
                // deploy new model to the repository
                
                try
                {
                    // note: dictionary model type has associated policies that will be invoked
                    ChildAssociationRef association = nodeService.createNode(customModelsSpaceNodeRef,
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, modelFileName),
                            ContentModel.TYPE_DICTIONARY_MODEL,
                            contentProps); // also invokes policies for DictionaryModelType - e.g. onUpdateProperties
                                
                    modelNodeRef = association.getChildRef();
                }
                catch (DuplicateChildNodeNameException dcnne)
                {
                    String msg = "Model already exists: "+modelFileName+" - "+dcnne;
                    logger.warn(msg);
                    // for now, assume concurrency failure
                    throw new ConcurrencyFailureException(msg);
                }
                
                // add titled aspect (for Web Client display)
                Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
                titledProps.put(ContentModel.PROP_TITLE, modelFileName);
                titledProps.put(ContentModel.PROP_DESCRIPTION, modelFileName);
                nodeService.addAspect(modelNodeRef, ContentModel.ASPECT_TITLED, titledProps);
                
                // add versionable aspect (set auto-version)
                Map<QName, Serializable> versionProps = new HashMap<QName, Serializable>();
                versionProps.put(ContentModel.PROP_AUTO_VERSION, true);
                nodeService.addAspect(modelNodeRef, ContentModel.ASPECT_VERSIONABLE, versionProps);
            }
            
            ContentWriter writer = contentService.getWriter(modelNodeRef, ContentModel.PROP_CONTENT, true);

            writer.setMimetype(MimetypeMap.MIMETYPE_XML);
            writer.setEncoding("UTF-8");
            
            writer.putContent(modelStream); // also invokes policies for DictionaryModelType - e.g. onContentUpdate
            modelStream.close();
            
            // activate the model
            nodeService.setProperty(modelNodeRef, ContentModel.PROP_MODEL_ACTIVE, new Boolean(true));
            
            // note: model will be loaded as part of DictionaryModelType.beforeCommit()
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Model deployment failed ", e);
        }     
    }
    
    public QName activateModel(String modelFileName)
    {     
        try
        {
        	return activateOrDeactivate(modelFileName, true);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Model activation failed ", e);
        }
    }  
    
    public QName deactivateModel(String modelFileName)
    { 
        try
        {
        	return activateOrDeactivate(modelFileName, false);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Model deactivation failed ", e);
        }	
    }
    
    private QName activateOrDeactivate(String modelFileName, boolean activate)
    {
        // Check that all the passed values are not null        
        ParameterCheck.mandatoryString("modelFileName", modelFileName);

        StoreRef storeRef = repoModelsLocation.getStoreRef();          
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        
        List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, repoModelsLocation.getPath()+"//.[@cm:name='"+modelFileName+"' and "+defaultSubtypeOfDictionaryModel+"]", null, namespaceService, false);
        
        if (nodeRefs.size() == 0)
        {
            throw new AlfrescoRuntimeException("Could not find custom model " + modelFileName);
        }
        else if (nodeRefs.size() > 1)
        {
            // unexpected: should not find multiple nodes with same name
            throw new AlfrescoRuntimeException("Found multiple custom models " + modelFileName);
        }
        
        NodeRef modelNodeRef = nodeRefs.get(0);
        
        boolean isActive = false;
        Boolean value = (Boolean)nodeService.getProperty(modelNodeRef, ContentModel.PROP_MODEL_ACTIVE);
        if (value != null)
        {
            isActive = value.booleanValue();
        }
        
        QName modelQName = (QName)nodeService.getProperty(modelNodeRef, ContentModel.PROP_MODEL_NAME);
        
        ModelDefinition modelDef = null;
        if (modelQName != null)
        {
	        try
	        {
	        	modelDef = dictionaryDAO.getModel(modelQName);
	        }
	        catch (DictionaryException e)
	        {
	        	logger.warn(e);
	        }
        }
        
        if (activate) 
        {
        	// activate
        	if (isActive)
        	{
	        	if (modelDef != null)
	        	{
	        		// model is already activated
	        		throw new AlfrescoRuntimeException("Model activation failed - model '" + modelQName + "' is already activated");
	        	}
	        	else
	        	{
	        		logger.warn("Model is set to active but not loaded in Dictionary - trying to load...");
	        	}
        	}
        	else
        	{
	        	if (modelDef != null)
	        	{
	        		logger.warn("Model is loaded in Dictionary but is not set to active - trying to activate...");
	        	}
        	}
        }
        else
        {
        	// deactivate
        	if (!isActive)
        	{
	        	if (modelDef == null)
	        	{
	        		// model is already deactivated
	        		throw new AlfrescoRuntimeException("Model deactivation failed - model '" + modelQName + "' is already deactivated");
	        	}
	        	else
	        	{
	        		logger.warn("Model is set to inactive but loaded in Dictionary - trying to unload...");
	        	}
        	}
        	else
        	{
	        	if (modelDef == null)
	        	{
	        		logger.warn("Model is not loaded in Dictionary but is set to active - trying to deactivate...");
	        	}
        	}
        }
         
        // activate/deactivate the model 
        nodeService.setProperty(modelNodeRef, ContentModel.PROP_MODEL_ACTIVE, new Boolean(activate));
        
        // note: model will be loaded/unloaded as part of DictionaryModelType.beforeCommit()
        return modelQName;
    }  

    public QName undeployModel(String modelFileName)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("modelFileName", modelFileName);
        
        QName modelQName = null;
        
        try
        { 
            // find model in repository
            
            StoreRef storeRef = repoModelsLocation.getStoreRef();
            NodeRef rootNode = nodeService.getRootNode(storeRef);
            
            List<NodeRef> nodeRefs = null;
            try
            {
                nodeRefs = searchService.selectNodes(rootNode, repoModelsLocation.getPath()+"//.[@cm:name='"+modelFileName+"' and "+defaultSubtypeOfDictionaryModel+"]", null, namespaceService, false);
            }
            catch (InvalidNodeRefException inre)
            {
                String msg = "Model no longer exists: "+modelFileName+" - "+inre;
                logger.warn(msg);
                // for now, assume concurrency failure
                throw new ConcurrencyFailureException(msg);
            }
            
            if (nodeRefs.size() == 0)
            {
                throw new AlfrescoRuntimeException("Could not find custom model " + modelFileName);
            }
            else if (nodeRefs.size() > 1)
            {
                // unexpected: should not find multiple nodes with same name
                throw new AlfrescoRuntimeException("Found multiple custom models " + modelFileName);
            }
            
            NodeRef modelNodeRef = nodeRefs.get(0);
            
            boolean isActive = false;
            Boolean value = (Boolean)nodeService.getProperty(modelNodeRef, ContentModel.PROP_MODEL_ACTIVE);
            if (value != null)
            {
                isActive = value.booleanValue();
            }
            
            modelQName = (QName)nodeService.getProperty(modelNodeRef, ContentModel.PROP_MODEL_NAME);
            
            ModelDefinition modelDef = null;
            if (modelQName != null)
            {
	            try
	            {
	            	modelDef = dictionaryDAO.getModel(modelQName);
	            }
	            catch (DictionaryException e)
	            {
	            	logger.warn(e);
	            }
            }
 
        	if (isActive)
        	{
        		if (modelDef == null)
        		{
        			logger.warn("Model is set to active but not loaded in Dictionary - trying to undeploy...");
        		}
            }
        	
            // permanently remove model from repository
            nodeService.addAspect(modelNodeRef, ContentModel.ASPECT_TEMPORARY, null);
            
            try
            {
                nodeService.deleteNode(modelNodeRef);
            }
            catch (DictionaryException de)
            {
                String msg = "Model undeployment failed: "+modelFileName+" - "+de;
                logger.warn(msg);
                // for now, assume concurrency failure
                throw new ConcurrencyFailureException(msg);
            }
            
            // note: deleted model will be unloaded as part of DictionaryModelType.beforeCommit()
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Model undeployment failed ", e);
        }
        
        return modelQName;
    }

    public List<String> getMessageBundles()
    {
        StoreRef storeRef = repoMessagesLocation.getStoreRef(); 
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        
        Collection<String> registeredBundles = messageService.getRegisteredBundles();

        List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, repoMessagesLocation.getPath()+CRITERIA_ALL+"["+defaultSubtypeOfContent+"]", null, namespaceService, false);
                
        List<String> resourceBundlesInRepo = new ArrayList<String>(); 
        
        for (NodeRef nodeRef : nodeRefs)
        {
            String resourceName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            String resourceBundleBaseName = null;
            int idx1 = resourceName.indexOf("_");
            if (idx1 > 0)
            {
                resourceBundleBaseName = resourceName.substring(0, idx1);
            }
            else
            {
                int idx2 = resourceName.indexOf(".");
                if (idx2 > 0)
                {
                    resourceBundleBaseName = resourceName.substring(0, idx2);
                }
                else
                {
                    // Unexpected format
                    logger.warn("Unexpected message resource name: " + resourceName);
              }
            }

            if (registeredBundles != null) 
            {
                for (String registeredBundlePath : registeredBundles)
                {
                    if (registeredBundlePath.endsWith(resourceBundleBaseName) && (! resourceBundlesInRepo.contains(resourceBundleBaseName)))
                    {
                        resourceBundlesInRepo.add(resourceBundleBaseName);
                    }
                }
            }
            else
            {
                // unexpected
                logger.error("Message bundle not registered: " + resourceBundleBaseName);
            }
        }
        
        return resourceBundlesInRepo;   
    }
    
    public String deployMessageBundle(String resourceClasspath)
    {   
        // Check that all the passed values are not null        
        ParameterCheck.mandatory("ResourceClasspath", resourceClasspath);
        
        String bundleBaseName = resourceClasspath;
        
        // note: resource path should be in form path1/path2/path3/bundlebasename
        int idx = resourceClasspath.lastIndexOf("/");
        
        if (idx != -1)
        {
            if (idx < (resourceClasspath.length()-1))
            {
                bundleBaseName = resourceClasspath.substring(idx+1);
            }
            else
            {
                bundleBaseName = null;
            }
        }
        
        checkBundleBaseName(bundleBaseName);
        
        String pattern = "classpath*:" + resourceClasspath + "*" + MessageServiceImpl.PROPERTIES_FILE_SUFFIX;
        
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
       
        try
        {
            Resource[] resources = resolver.getResources(pattern);
            
            if ((resources != null) && (resources.length > 0))
            {
                ArrayList<String> names = new ArrayList<String>();
                ArrayList<Resource> filteredResources = new ArrayList<Resource>();
                
                for (int i = 0; i < resources.length; i++)
                {
                    String filename = resources[i].getFilename();
                    if (! names.contains(filename))
                    {
                        names.add(filename);
                        filteredResources.add(resources[i]);
                    }
                }
            
                for (Resource resource : filteredResources)
                {
                    InputStream fileStream = resource.getInputStream();
                    String filename = resource.getFilename();
                    deployMessageResourceFile(resourceClasspath, filename, fileStream, false);   
                }           
                
                // register bundle
                
                StoreRef storeRef = repoMessagesLocation.getStoreRef();
                String repoBundlePath = storeRef.toString() + repoMessagesLocation.getPath() + "/cm:" + bundleBaseName;
                messageService.registerResourceBundle(repoBundlePath); 
                
                logger.info("Message resource bundle deployed: " + bundleBaseName);
            }
            else
            {
                logger.warn("No message resources found: " + resourceClasspath);
                throw new AlfrescoRuntimeException("No message resources found: " + resourceClasspath);
            }
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Message resource bundle deployment failed for resource classpath " + resourceClasspath, e);
        }
        
        return bundleBaseName;
    }
    
    /*
     * Deploy message resource file
     */
    private void deployMessageResourceFile(String bundleBasePath, String name, InputStream resourceStream, boolean registerResourceBundle)
    {    
        // Check that all the passed values are not null
        ParameterCheck.mandatory("BundleBasePath", bundleBasePath);
        ParameterCheck.mandatory("Name", name);
        ParameterCheck.mandatory("ResourceStream", resourceStream);
        
        try
        {        
            Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
            contentProps.put(ContentModel.PROP_NAME, name);
            
            StoreRef storeRef = repoMessagesLocation.getStoreRef();
            NodeRef rootNode = nodeService.getRootNode(storeRef);
            
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, repoMessagesLocation.getPath(), null, namespaceService, false);
            
            if (nodeRefs.size() == 0)
            {
                throw new AlfrescoRuntimeException("Could not find messages location " + repoMessagesLocation.getPath());
            }
            else if (nodeRefs.size() > 1)
            {
                // unexpected: should not find multiple nodes with same name                
                throw new AlfrescoRuntimeException("Found multiple messages location " + repoMessagesLocation.getPath());
            }

            NodeRef customLabelsNodeRef = nodeRefs.get(0);
                                   
            ChildAssociationRef association = nodeService.createNode(customLabelsNodeRef, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                    ContentModel.TYPE_CONTENT,
                    contentProps);
            
            NodeRef content = association.getChildRef();
            
            // add titled aspect (for Web Client display)
            Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
            titledProps.put(ContentModel.PROP_TITLE, name);
            titledProps.put(ContentModel.PROP_DESCRIPTION, name);
            nodeService.addAspect(content, ContentModel.ASPECT_TITLED, titledProps);
            
            // add inline-editable aspect
            Map<QName, Serializable> editProps = new HashMap<QName, Serializable>(1, 1.0f);
            editProps.put(ApplicationModel.PROP_EDITINLINE, true);
            nodeService.addAspect(content, ApplicationModel.ASPECT_INLINEEDITABLE, editProps);
               
            ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);

            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
        
            writer.putContent(resourceStream);
            resourceStream.close();
            
            if (registerResourceBundle == true)
            {
                String bundleBaseName = null;
                int idx = bundleBasePath.lastIndexOf("/");
                if ((idx != -1) && (idx != bundleBasePath.length() - 1))
                {
                    bundleBaseName = bundleBasePath.substring(idx+1);
                }
                else
                {
                    bundleBaseName = bundleBasePath;
                }
                
                String repoBundlePath = storeRef.toString() + repoMessagesLocation.getPath() + "/cm:" + bundleBaseName;
                messageService.registerResourceBundle(repoBundlePath);  
            }    
            
            logger.info("Message resource deployed: " + name);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Message resource deployment failed", e);
        }      
    }   
    
    public void undeployMessageBundle(String bundleBaseName)
    {   
        checkBundleBaseName(bundleBaseName);
    
        try
        {
            StoreRef storeRef = repoMessagesLocation.getStoreRef();
            
            // unregister bundle
            String repoBundlePath = storeRef.toString() + repoMessagesLocation.getPath() + "/cm:" + bundleBaseName;
            messageService.unregisterResourceBundle(repoBundlePath);
              
            NodeRef rootNode = nodeService.getRootNode(storeRef);
             
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, repoMessagesLocation.getPath()+CRITERIA_ALL, null, namespaceService, false);
                    
            boolean found = false;
            for (NodeRef nodeRef : nodeRefs)
            {
                String resourceName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                
                if (bundleBaseName.equals(messageService.getBaseBundleName(resourceName)))
                {
                    // remove message resource file from the repository
                    nodeService.deleteNode(nodeRef);
                    found = true; // continue to undeploy any others
                }               
            }           

            if (found)
            {          
                logger.info("Message resources undeployed: " + bundleBaseName);
            }
            else
            {
                throw new AlfrescoRuntimeException("Could not find message resource bundle " + repoBundlePath);
            }
        }
        catch (Throwable t)
        {
            throw new AlfrescoRuntimeException("Message resource bundle undeployment failed ", t);
        }
    }
    
    public void reloadMessageBundle(String bundleBaseName)
    {
        checkBundleBaseName(bundleBaseName);
    
        try
        {
            StoreRef storeRef = repoMessagesLocation.getStoreRef();
            
            // re-register bundle
            
            String repoBundlePath = storeRef.toString() + repoMessagesLocation.getPath() + "/cm:" + bundleBaseName;
             
            NodeRef rootNode = nodeService.getRootNode(storeRef);
             
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, repoMessagesLocation.getPath()+CRITERIA_ALL, null, namespaceService, false);
                    
            boolean found = false;
            for (NodeRef nodeRef : nodeRefs)
            {
                String resourceName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                
                if (bundleBaseName.equals(messageService.getBaseBundleName(resourceName)))
                {
                    found = true;
                    break;
                }               
            }
            
            if (found)
            {          
                messageService.unregisterResourceBundle(repoBundlePath);           
                messageService.registerResourceBundle(repoBundlePath);
    
                logger.info("Message resources re-loaded: " + bundleBaseName);
            }
            else
            {
                throw new AlfrescoRuntimeException("Could not find message resource bundle " + repoBundlePath);
            }
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Message resource re-load failed", e);
        }      
    }
    
    private void checkBundleBaseName(String bundleBaseName)
    {
        if ((bundleBaseName == null) || (bundleBaseName.equals("")))
        {
            throw new AlfrescoRuntimeException("Message deployment failed - missing bundle base name");
        }
        
        if (bundleBaseName.indexOf("_") != -1)
        {
            // currently limited due to parser in MessageServiceImpl.getBaseBundleName
            throw new AlfrescoRuntimeException("Message deployment failed - bundle base name '" + bundleBaseName + "' should not contain '_' (underscore)");  
        }
        
        if (bundleBaseName.indexOf(".") != -1)
        {
            throw new AlfrescoRuntimeException("Message deployment failed - bundle base name '" + bundleBaseName + "' should not contain '.' (period)");           
        }
    }

    @Override
    public RepoUsage getRestrictions()
    {
        return repoUsageComponent.getRestrictions();
    }

    @Override
    public RepoUsage getUsage()
    {
        return repoUsageComponent.getUsage();
    }

    @Override
    public boolean updateUsage(UsageType usageType)
    {
        return repoUsageComponent.updateUsage(usageType);
    }

    @Override
    public RepoUsageStatus getUsageStatus()
    {
        return repoUsageComponent.getUsageStatus();
    }
}
