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
package org.alfresco.repo.admin;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Repository Admin Service Implementation.
 * <p>
 * @see RepoAdminService interface
 *
 */
   
public class RepoAdminServiceImpl implements RepoAdminService
{   
    // Logging support
    private static Log logger = LogFactory.getLog("org.alfresco.repo.admin.RepoAdminServiceImpl");
    
    // dependencies  
    private DictionaryDAO dictionaryDAO;
    private SearchService searchService;
    private NodeService nodeService;
    private ContentService contentService;
    private NamespaceService namespaceService;
    private MessageService messageService;
    
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
    
    public void setmessageService(MessageService messageService)
    {
        this.messageService = messageService;
    }
    
    
    public void setRepositoryModelsLocation(RepositoryLocation repoModelsLocation)
    {
        this.repoModelsLocation = repoModelsLocation;
    }
    
    public void setRepositoryMessagesLocation(RepositoryLocation repoMessagesLocation)
    {
        this.repoMessagesLocation = repoMessagesLocation;
    }
    
      
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.admin.RepoAdminService#getModels()
     */
    public List<RepoModelDefinition> getModels()
    {
        StoreRef storeRef = repoModelsLocation.getStoreRef();
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        
        Collection<QName> models = dictionaryDAO.getModels();  
        
        List<String> dictionaryModels = new ArrayList<String>();
        for (QName model : models)
        {
            dictionaryModels.add(model.toPrefixString());
        }

        List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, repoModelsLocation.getPath()+CRITERIA_ALL+"["+defaultSubtypeOfDictionaryModel+"]", null, namespaceService, false);
                
        List<RepoModelDefinition> modelsInRepo = new ArrayList<RepoModelDefinition>();
        
        if (nodeRefs.size() > 0)
        {
            for (NodeRef nodeRef : nodeRefs)
            {
                String modelFileName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                String repoVersion = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
               
                String modelName = null;
                    
                try
                {
                    ContentReader cr = contentService.getReader(nodeRef, ContentModel.TYPE_CONTENT);
                    InputStream is = cr.getContentInputStream();
                              
                    M2Model model = M2Model.createModel(is);
                    is.close();
    
                    modelName = model.getName();
                }
                catch (Throwable t)
                {
                    throw new AlfrescoRuntimeException("Failed to getModels " + t);
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
        }
        
        return modelsInRepo;
    }
        
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.admin.RepoAdminService#deployModel(java.io.InputStream, java.lang.String)
     */
    public QName deployModel(InputStream modelStream, String modelFileName)
    {     
        // Check that all the passed values are not null        
        ParameterCheck.mandatory("ModelStream", modelStream);
        ParameterCheck.mandatoryString("ModelFileName", modelFileName);
             
        QName modelName = null;
        
        try
        {        
            // TODO workaround due to issue with model.toXML() - see below
            BufferedReader in = new BufferedReader(new InputStreamReader(modelStream));
            StringBuffer buffer = new StringBuffer();
            String line = null;
            while ((line = in.readLine()) != null) {
              buffer.append(line);
            }
            
            InputStream is = new ByteArrayInputStream(buffer.toString().getBytes());
                                
            M2Model model = M2Model.createModel(is);
            is.close();
            
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

            NodeRef customModelsNodeRef = nodeRefs.get(0);
            
            nodeRefs = searchService.selectNodes(customModelsNodeRef, "*[@cm:name='"+modelFileName+"' and "+defaultSubtypeOfDictionaryModel+"]", null, namespaceService, false);
            
            if (nodeRefs.size() == 1)
            {
                // re-deploy existing model to the repository       
            	
                NodeRef modelNodeRef = nodeRefs.get(0);
                
                ContentWriter writer = contentService.getWriter(modelNodeRef, ContentModel.PROP_CONTENT, true);

                writer.setMimetype(MimetypeMap.MIMETYPE_XML);
                writer.setEncoding("UTF-8");
            
                is = new ByteArrayInputStream(buffer.toString().getBytes());            
                writer.putContent(is); // also invokes policies for DictionaryModelType - e.g. onContentUpdate
                is.close();
                
                /* TODO
                ByteArrayOutputStream out = new ByteArrayOutputStream();         
                model.toXML(out); // fails with NPE in JIBX - see also: http://issues.alfresco.com/browse/AR-1304
                writer.putContent(out.toString("UTF-8"));
                */
                
                // parse and update model in the dictionary
                modelName = dictionaryDAO.putModel(model); 
                
                logger.info("Model re-deployed: " + modelName);
            }
            else
            {
                // deploy new model to the repository
                
                // note: dictionary model type has associated policies that will be invoked
                ChildAssociationRef association = nodeService.createNode(customModelsNodeRef, 
                        ContentModel.ASSOC_CONTAINS, 
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, modelFileName), 
                        ContentModel.TYPE_DICTIONARY_MODEL,
                        contentProps); // also invokes policies for DictionaryModelType - e.g. onUpdateProperties
                            
                NodeRef content = association.getChildRef();
                
                // add titled aspect (for Web Client display)
                Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
                titledProps.put(ContentModel.PROP_TITLE, modelFileName);
                titledProps.put(ContentModel.PROP_DESCRIPTION, modelFileName);
                nodeService.addAspect(content, ContentModel.ASPECT_TITLED, titledProps);
                
                // add versionable aspect (set auto-version)
                Map<QName, Serializable> versionProps = new HashMap<QName, Serializable>();
                versionProps.put(ContentModel.PROP_AUTO_VERSION, true);
                nodeService.addAspect(content, ContentModel.ASPECT_VERSIONABLE, versionProps);
                                   
                ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);

                writer.setMimetype(MimetypeMap.MIMETYPE_XML);
                writer.setEncoding("UTF-8");
            
                is = new ByteArrayInputStream(buffer.toString().getBytes());            
                writer.putContent(is); // also invokes policies for DictionaryModelType - e.g. onContentUpdate
                is.close();    
                
                /* TODO
                ByteArrayOutputStream out = new ByteArrayOutputStream();         
                model.toXML(out); // fails with NPE in JIBX - see also: http://issues.alfresco.com/browse/AR-1304
                writer.putContent(out.toString("UTF-8"));
                */
                
                // parse and add model to dictionary
                modelName = dictionaryDAO.putModel(model);  
                
                logger.info("Model deployed: " + modelName);
            }
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Model deployment failed", e);
        }
        
        return modelName;                  
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.admin.RepoAdminService#reloadModel(java.lang.String)
     */
    public QName reloadModel(String modelFileName)
    {     
        // Check that all the passed values are not null        
        ParameterCheck.mandatoryString("modelFileName", modelFileName);
          
        QName modelQName = null;
        
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
        
        try
        {     
            ContentReader cr = contentService.getReader(modelNodeRef, ContentModel.TYPE_CONTENT);
            InputStream is = cr.getContentInputStream();
            
            // create model            
            M2Model model = M2Model.createModel(is);
            is.close();

            if (model != null)
            {
                String modelName = model.getName();
            
                // parse and update model in the dictionary
                modelQName = dictionaryDAO.putModel(model); 
  
                logger.info("Model loaded: " + modelName);
            }
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Model deployment failed", e);
        }
        
        return modelQName;                  
    }  

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.admin.RepoAdminService#undeployModel(java.lang.String)
     */
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
            
            String modelName = null;
            
            try
            {
                ContentReader cr = contentService.getReader(modelNodeRef, ContentModel.TYPE_CONTENT);
                InputStream is = cr.getContentInputStream();
                            
                M2Model model = M2Model.createModel(is);
                is.close();

                modelName = model.getName();
            }
            catch (Throwable t)
            {
                throw new AlfrescoRuntimeException("Failed to get model " + t);
            }
               
            // permanently remove model from repository
            nodeService.addAspect(modelNodeRef, ContentModel.ASPECT_TEMPORARY, null);
            nodeService.deleteNode(modelNodeRef);
            
            modelQName = QName.createQName(modelName, namespaceService);
            
            dictionaryDAO.removeModel(modelQName);               

            logger.info("Model undeployed: " + modelFileName);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Model undeployment failed", e);
        }        
        
        return modelQName;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.admin.RepoAdminService#getMessageBundles()
     */
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
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.admin.RepoAdminService#deployMessageBundle(java.lang.String)
     */
    public String deployMessageBundle(String resourceClasspath)
    {   
        // Check that all the passed values are not null        
        ParameterCheck.mandatory("ResourceClasspath", resourceClasspath);
        
        String bundleBaseName = null;
        
        // note: resource path should be in form path1/path2/path3/bundlebasename
        int idx = resourceClasspath.lastIndexOf("/");
        
        if ((idx != -1) && (idx < (resourceClasspath.length()-1)))
        {
            bundleBaseName = resourceClasspath.substring(idx+1);
        }
        
        if (bundleBaseName == null)
        {
            throw new AlfrescoRuntimeException("Message deployment failed - missing bundle base name (path = " + resourceClasspath + ")");
        }
        
        if (bundleBaseName.indexOf("_") != -1)
        {
            // currently limited due to parser in DictionaryRepositoryBootstrap
            throw new AlfrescoRuntimeException("Message deployment failed - bundle base name '" + bundleBaseName + "' should not contain '_' (underscore)");  
        }
        
        if (bundleBaseName.indexOf(".") != -1)
        {
            throw new AlfrescoRuntimeException("Message deployment failed - bundle base name '" + bundleBaseName + "' should not contain '.' (period)");           
        }
        
        String pattern = "classpath*:" + resourceClasspath + "*.properties";
        
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
            throw new AlfrescoRuntimeException("Message resource bundle deployment failed ", e);
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
                throw new AlfrescoRuntimeException("Could not find custom labels location " + repoMessagesLocation.getPath());
            }
            else if (nodeRefs.size() > 1)
            {
                // unexpected: should not find multiple nodes with same name                
                throw new AlfrescoRuntimeException("Found multiple custom labels location " + repoMessagesLocation.getPath());
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
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.admin.RepoAdminService#undeployMessageBundle(java.lang.String)
     */
    public void undeployMessageBundle(String bundleBaseName)
    {   
        // Check that all the passed values are not null        
        ParameterCheck.mandatory("bundleBaseName", bundleBaseName);
    
        try
        {
            StoreRef storeRef = repoMessagesLocation.getStoreRef();
            
            // unregister bundle
            String repoBundlePath = storeRef.toString() + repoMessagesLocation.getPath() + "/cm:" + bundleBaseName;
            messageService.unregisterResourceBundle(repoBundlePath);
              
            NodeRef rootNode = nodeService.getRootNode(storeRef);
             
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, repoMessagesLocation.getPath()+CRITERIA_ALL, null, namespaceService, false);
                    
            for (NodeRef nodeRef : nodeRefs)
            {
                String customLabelName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                
                if (customLabelName.startsWith(bundleBaseName))
                {
                    // remove message resource file from the repository
                    nodeService.deleteNode(nodeRef); 
                }               
            }           
            
            logger.info("Message resources undeployed: " + bundleBaseName);
        }
        catch (Throwable t)
        {
            throw new AlfrescoRuntimeException("Messages undeployment failed ", t);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.admin.RepoAdminService#reloadMessageBundle(java.lang.String)
     */
    public void reloadMessageBundle(String bundleBaseName)
    {    
        // Check that all the passed values are not null        
        ParameterCheck.mandatory("bundleBaseName", bundleBaseName);
    
        try
        {
            StoreRef storeRef = repoMessagesLocation.getStoreRef();
            
            // re-register bundle
            
            String repoBundlePath = storeRef.toString() + repoMessagesLocation.getPath() + "/cm:" + bundleBaseName;
            
            messageService.unregisterResourceBundle(repoBundlePath);           
            messageService.registerResourceBundle(repoBundlePath);

            logger.info("Message resources re-loaded: " + bundleBaseName);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Message resource re-load failed", e);
        }      
    } 
}
