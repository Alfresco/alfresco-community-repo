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

package org.alfresco.module.org_alfresco_module_rm.email;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.RFC822MetadataExtracter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Custom Email Mapping Service
 */
public class CustomEmailMappingServiceImpl extends AbstractLifecycleBean implements CustomEmailMappingService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(CustomEmailMappingServiceImpl.class);

    /** Node reference's to configuration elements */
    private static final NodeRef CONFIG_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_emailmapping_config");
    private static final NodeRef CONFIG_FOLDER_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_config_folder");

    /** Config file name */
    private static final String CONFIG_NAME = "imapConfig.json";

    /** Default custom mappings (TODO move to spring config) */
    private static final CustomMapping[] DEFAULT_MAPPINGS =
    {
        new CustomMapping("Date", "dod:dateReceived"),
        new CustomMapping("messageTo", "dod:address"),
        new CustomMapping("messageFrom", "dod:originator"),
        new CustomMapping("messageSent", "dod:publicationDate"),
        new CustomMapping("messageCc", "dod:otherAddress")
    };

    /** Extractor */
    private RFC822MetadataExtracter extracter;

    /** Services */
    private NodeService nodeService;
    private NamespacePrefixResolver nspr;
    private ContentService contentService;
    private TransactionService transactionService;

    /** Transient set of custom mappings */
    private Set<CustomMapping> customMappings;

    /** List of email mapping keys */
    private List<String> emailMappingKeys;

    /**
     * @param nspr  namespace service
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver nspr)
    {
        this.nspr = nspr;
    }

    /**
     * @param extractor extractor component
     */
    public void setExtracter(RFC822MetadataExtracter extractor)
    {
        this.extracter = extractor;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param transactionService    transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param emailMappingKeys    email mapping keys
     */
    public void setEmailMappingKeys(List<String> emailMappingKeys)
    {
        this.emailMappingKeys = emailMappingKeys;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.email.CustomEmailMappingService#getCustomMappings()
     */
    public Set<CustomMapping> getCustomMappings()
    {
        if (customMappings == null)
        {
            // if we have a config file
            if (nodeService.exists(CONFIG_NODE_REF))
            {
                // load the contents of the config file
                customMappings = loadConfig();
            }
            else
            {
                customMappings = new HashSet<>();

                // load the contents of the extractors property file
                Map<String, Set<QName>> currentMapping = extracter.getCurrentMapping();
                for (Map.Entry<String, Set<QName>> entry : currentMapping.entrySet())
                {
                    Set<QName> set = entry.getValue();

                    for (QName qname : set)
                    {
                        CustomMapping value = new CustomMapping();
                        value.setFrom(entry.getKey());
                        QName resolvedQname = qname.getPrefixedQName(nspr);
                        value.setTo(resolvedQname.toPrefixString());
                        customMappings.add(value);
                    }
                }

                // if we have an old config file
                NodeRef oldConfigNode = getOldConfigNode();
                if (oldConfigNode != null)
                {
                    // load the contents of the old config file
                    Set<CustomMapping> oldMappings = readOldConfig(oldConfigNode);
                    customMappings.addAll(oldMappings);
                }

                // load the hard coded mappings
                for(CustomMapping mapping : DEFAULT_MAPPINGS)
                {
                    customMappings.add(mapping);
                }

                // create the config file
                saveConfig(customMappings);
            }
        }

        return customMappings;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.email.CustomEmailMappingService#addCustomMapping(java.lang.String, java.lang.String)
     */
    public void addCustomMapping(String from, String to)
    {
        if (StringUtils.isBlank(from) || StringUtils.isBlank(to))
        {
            throw new AlfrescoRuntimeException("Invalid values for from/to.");
        }

        // create custom mapping
        CustomMapping customMapping = new CustomMapping(from, to);

        // check whether we already have this mapping or not
        Set<CustomMapping> customMappings = getCustomMappings();
        if (customMappings.contains(customMapping))
        {
            throw new AlfrescoRuntimeException("Can not add custom email mapping, because duplicate mapping already exists.");
        }

        // else add the custom mapping (since we have already called getCustomMapping we can be sure
        // the member variable is populated)
        customMappings.add(customMapping);

        // save the changes into the config file
        saveConfig(customMappings);

        // update the extractors configuration
        updateExtractor();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.email.CustomEmailMappingService#deleteCustomMapping(java.lang.String, java.lang.String)
     */
    public void deleteCustomMapping(String from, String to)
    {
        if (StringUtils.isBlank(from) || StringUtils.isBlank(to))
        {
            throw new AlfrescoRuntimeException("Invalid values for from/to.");
        }

        // create custom mapping
        CustomMapping customMapping = new CustomMapping(from, to);

        // check whether we already have this mapping or not
        Set<CustomMapping> customMappings = getCustomMappings();
        if (customMappings.contains(customMapping))
        {
            // else remove the custom mapping (since we have already called getCustomMapping we can be sure
            // the member variable is populated)
            customMappings.remove(customMapping);

            // save the changes into the config file
            saveConfig(customMappings);

            // update the extractors configuration
            updateExtractor();
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.email.CustomEmailMappingService#getEmailMappingKeys()
     */
    @Override
    public List<String> getEmailMappingKeys()
    {
        return emailMappingKeys;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.email.CustomEmailMappingService#registerEMailMappingKey(java.lang.String)
     */
    @Override
    public void registerEMailMappingKey(String emailMappingKey)
    {
        ParameterCheck.mandatoryString("emailMappingKey", emailMappingKey);

        emailMappingKeys.add(emailMappingKey);
    }

    /**
     * Updates the extractor with the custom configuration.
     */
    private void updateExtractor()
    {
        // convert the mapping information into the form understood by the extractor
        Map<String, Set<QName>> newMapping = new HashMap<>(17);
        for(CustomMapping mapping : getCustomMappings())
        {
            QName newQName = QName.createQName(mapping.getTo(), nspr);
            Set<QName> values = newMapping.get(mapping.getFrom());
            if(values == null)
            {
                values = new HashSet<>();
                newMapping.put(mapping.getFrom(), values);
            }
            values.add(newQName);
        }

        // update the metadata extracter
        extracter.setMapping(newMapping);
    }

    /**
     * Loads the custom mappings from the configuration file.
     *
     * @return
     */
    private Set<CustomMapping> loadConfig()
    {
        Set<CustomMapping> result = new HashSet<>();
        ContentReader cr = contentService.getReader(CONFIG_NODE_REF, ContentModel.PROP_CONTENT);
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
                    result.add(mapping);
                }
            }
            catch (JSONException je)
            {
                throw new AlfrescoRuntimeException("Unable to read custom email configuration", je);
            }
        }

        return result;
    }

    /**
     *
     * @param customMappingsToSave
     */
    private void saveConfig(Set<CustomMapping> customMappingsToSave)
    {
        if (!nodeService.exists(CONFIG_NODE_REF))
        {
            // create the config node
            Map<QName, Serializable> properties = new HashMap<>(2);
            properties.put(ContentModel.PROP_NAME, CONFIG_NAME);
            properties.put(ContentModel.PROP_NODE_UUID, CONFIG_NODE_REF.getId());
            nodeService.createNode(
                    CONFIG_FOLDER_NODE_REF,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, CONFIG_NAME),
                    ContentModel.TYPE_CONTENT,
                    properties);
        }

        // build JSON array of mappings
        JSONArray jsonMappings = new JSONArray();
        try
        {
            for(CustomMapping mapping : customMappingsToSave)
            {
                JSONObject obj = new JSONObject();
                obj.put("from", mapping.getFrom());
                obj.put("to", mapping.getTo());
                jsonMappings.put(obj);
            }
        }
        catch (JSONException je)
        {
            throw new AlfrescoRuntimeException("Unable to create JSON email mapping configuration during save.", je);
        }

        // update the content
        ContentWriter writer = this.contentService.getWriter(CONFIG_NODE_REF, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(jsonMappings.toString());
    }

    /**
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // run as System on bootstrap
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
                {
                    public Void execute()
                    {
                        try
                        {
                            // update the extractor with the custom mappings
                            updateExtractor();
                        }
                        catch (RuntimeException e)
                        {
                            // log a warning
                            if (logger.isWarnEnabled())
                            {
                                logger.warn(e.getMessage());
                            }

                            // reset the mappings
                            customMappings = null;

                            // rethrow
                            throw e;
                        }
                        return null;
                    }
                };
                transactionService.getRetryingTransactionHelper().doInTransaction(callback);

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent arg0)
    {
        // No implementation
    }

    /**
     * Helper method to get the old configuration node.  This is used during the migration
     * from 1.0 to 2.0.
     * <p>
     * Returns null if it does not exist.
     *
     * @return  {@link NodeRef} node reference of the old configuration node, null otherwise
     */
    private NodeRef getOldConfigNode()
    {
        NodeRef rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        return nodeService.getChildByName(rootNode, RecordsManagementModel.ASSOC_EMAIL_CONFIG, CONFIG_NAME);
    }

    /**
     * Reads the old configuration node.  This is used during the migration from 1.0 to 2.0.
     *
     * @param nodeRef   the old configuration node reference
     * @return {@link Set}<{@link CustomMapping}>   set of the custom mappings stored in the old configuration
     */
    private Set<CustomMapping> readOldConfig(NodeRef nodeRef)
    {
        Set<CustomMapping> newMappings = new HashSet<>();

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
}
