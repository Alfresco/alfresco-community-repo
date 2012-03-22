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
package org.alfresco.module.org_alfresco_module_rm.email;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.RFC822MetadataExtracter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CustomEmailMappingServiceImpl implements CustomEmailMappingService
{
    
    private RFC822MetadataExtracter extracter;
    private NodeService nodeService;
    private NamespacePrefixResolver nspr;
    private PolicyComponent policyComponent;
    private ContentService contentService;
    private TransactionService transactionService;
    
    private Set<CustomMapping> customMappings = Collections.synchronizedSet(new HashSet<CustomMapping>());
    
    private static Log logger = LogFactory.getLog(CustomEmailMappingServiceImpl.class);
    
    
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Get the name space prefix resolver
     * @return the name space prefix resolver
     */
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return nspr;
    }

    /**
     * Set the name space prefix resolver
     * @param nspr
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver nspr)
    {
        this.nspr = nspr;
    }
    
    /**
     * 
     */
    public void init()
    { 
        CustomMapping[] rmHardCodedMappings =  {
                    new CustomMapping("Date", "rma:dateReceived"),
                    new CustomMapping("messageTo", "rma:address"),
                    new CustomMapping("messageFrom", "rma:originator"),
                    new CustomMapping("messageSent", "rma:publicationDate"),
                    new CustomMapping("messageCc", "rma:otherAddress")
        };     
        
        NodeRef configNode = getConfigNode();
        if(configNode != null)
        {   
            /**
             * Get any custom mappings.
             */
            customMappings = readConfig(configNode);
        }
        
        /**
         * ensure that the customMappings contain the RM specific mappings
         */
        for(CustomMapping mapping : rmHardCodedMappings)
        {
            if(!customMappings.contains(mapping))
            {
                customMappings.add(mapping);
            }
        }

        // Get the read only existing configuration
        Map<String, Set<QName>> currentMapping = extracter.getCurrentMapping();

        Map<String, Set<QName>> newMapping = new HashMap<String, Set<QName>>(17);
        newMapping.putAll(currentMapping);

        for(CustomMapping mapping : customMappings)
        {
            QName newQName = QName.createQName(mapping.getTo(), nspr);
            Set<QName> values = newMapping.get(mapping.getFrom());
            if(values == null)
            {
                values = new HashSet<QName>();
                newMapping.put(mapping.getFrom(), values);
            }
            values.add(newQName);
        }

        // Now update the metadata extracter
        extracter.setMapping(newMapping);       
         
        // Register interest in the onContentUpdate policy
        policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                RecordsManagementModel.TYPE_EMAIL_CONFIG,
                new JavaBehaviour(this, "onContentUpdate"));        
        
    }
    
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        NodeRef configNode = getConfigNode();
        if(configNode != null)
        {
            Set<CustomMapping> newMappings = readConfig(configNode);
            
            customMappings.addAll(newMappings);
            
            for(CustomMapping mapping : customMappings)
            {
                if(!newMappings.contains(mapping))
                {
                    customMappings.remove(mapping);
                }
            }
        }
    }
    
    public void beforeDeleteNode(NodeRef nodeRef)
    {
    }
    
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {

    }
    
    public Set<CustomMapping> getCustomMappings()
    {  
        // add all the lists data to a Map
        Set<CustomMapping> emailMap = new HashSet<CustomMapping>();
        
        Map<String, Set<QName>> currentMapping = extracter.getCurrentMapping();
        
        for(String key : currentMapping.keySet())
        {
            Set<QName> set = currentMapping.get(key);
            
            for(QName qname : set)
            {
                CustomMapping value = new CustomMapping();
                value.setFrom(key);
                QName resolvedQname = qname.getPrefixedQName(nspr);
                value.setTo(resolvedQname.toPrefixString());  
                emailMap.add(value);
            }
        }
        
        return emailMap;
    }
    

    public void addCustomMapping(String from, String to)
    {
        // Get the read only existing configuration
        Map<String, Set<QName>> currentMapping = extracter.getCurrentMapping();
        
        Map<String, Set<QName>> newMapping = new HashMap<String, Set<QName>>(17);
        newMapping.putAll(currentMapping);
        
        QName newQName = QName.createQName(to, nspr);
        
        Set<QName> values = newMapping.get(from);
        if(values == null)
        {
            values = new HashSet<QName>();
            newMapping.put(from, values);
        }
        values.add(newQName);
        
        CustomMapping xxx = new CustomMapping();
        xxx.setFrom(from);
        xxx.setTo(to);
        customMappings.add(xxx);
        
        updateOrCreateEmailConfig(customMappings);
        
        // Crash in the new config.
        extracter.setMapping(newMapping);
    }

    public void deleteCustomMapping(String from, String to)
    {
        // Get the read only existing configuration
        Map<String, Set<QName>> currentMapping = extracter.getCurrentMapping();
        
        Map<String, Set<QName>> newMapping = new HashMap<String, Set<QName>>(17);
        newMapping.putAll(currentMapping);
        
        QName oldQName = QName.createQName(to, nspr);
        
        Set<QName> values = newMapping.get(from);
        if(values != null)
        {
            values.remove(oldQName);
        }
        
        CustomMapping toDelete = new CustomMapping(from, to);
        customMappings.remove(toDelete);
        
        updateOrCreateEmailConfig(customMappings);
        
        // Crash in the new config.
        extracter.setMapping(newMapping);
    }

    public void setExtracter(RFC822MetadataExtracter extractor)
    {
        this.extracter = extractor;
    }

    public RFC822MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }
    
    // Default
    private StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    
    private static final String CONFIG_NAME = "imapConfig.json";
    
    /**
     * 
     * @param nodeRef
     * @return
     */
    private Set<CustomMapping> readConfig(NodeRef nodeRef)
    {
        Set<CustomMapping> newMappings = new HashSet<CustomMapping>();
        
        ContentReader cr = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (cr != null)
        {
            String text = cr.getContentString();
            
            try
            {  
                JSONArray jsonArray = new JSONArray(new JSONTokener(text));
                for(int i = 0 ; i < jsonArray.length(); i++)
                {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    CustomMapping mapping = new CustomMapping();
                    mapping.setFrom(obj.getString("from"));
                    mapping.setTo(obj.getString("to"));
                    newMappings.add(mapping);
                }
                return newMappings;
            }
            catch (JSONException je)
            {
                logger.warn("unable to read custom email configuration", je);
                return newMappings;
            }
            
        }
        return newMappings;
    } 
    
    public NodeRef updateOrCreateEmailConfig(Set<CustomMapping> customMappings)
    {
        NodeRef caveatConfig = updateOrCreateEmailConfig();
        
        try
        {
            JSONArray mappings = new JSONArray();
            for(CustomMapping mapping : customMappings)
            {
                JSONObject obj = new JSONObject();
                obj.put("from", mapping.getFrom());
                obj.put("to", mapping.getTo());
                mappings.put(obj);
            }
         
            // Update the content
            ContentWriter writer = this.contentService.getWriter(caveatConfig, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(mappings.toString());
        }
        catch (JSONException je)
        {
            
        }
    
        
        return caveatConfig;
    }
    
    public NodeRef updateOrCreateEmailConfig(String txt)
    {
        NodeRef caveatConfig = updateOrCreateEmailConfig();
        
        // Update the content
        ContentWriter writer = this.contentService.getWriter(caveatConfig, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(txt);
        
        return caveatConfig;
    }
    
    private NodeRef updateOrCreateEmailConfig()
    {
        NodeRef caveatConfig = getConfigNode();
        if (caveatConfig == null)
        {
            logger.debug("custom email configuration does not exist - creating new");
            NodeRef rootNode = nodeService.getRootNode(storeRef);
            //nodeService.addAspect(rootNode, VersionModel.ASPECT_VERSION_STORE_ROOT, null);
            
            // Create caveat config
            caveatConfig = nodeService.createNode(rootNode,
                                                  RecordsManagementModel.ASSOC_EMAIL_CONFIG,
                                                  QName.createQName(RecordsManagementModel.RM_URI, CONFIG_NAME),
                                                  RecordsManagementModel.TYPE_EMAIL_CONFIG).getChildRef();
            
            nodeService.setProperty(caveatConfig, ContentModel.PROP_NAME, CONFIG_NAME);
        }
        
        return caveatConfig;
    }
    
    public NodeRef getConfigNode()
    {
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        return nodeService.getChildByName(rootNode, RecordsManagementModel.ASSOC_EMAIL_CONFIG, CONFIG_NAME);
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public ContentService getContentService()
    {
        return contentService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public TransactionService getTransactionService()
    {
        return transactionService;
    }
    
    
}
