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

package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import static org.alfresco.module.org_alfresco_module_rm.script.slingshot.ClassificationReasonsUtil.REASONS_KEY;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchParameters;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetailsCompatibility;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * RM search GET web script
 *
 * @author Roy Wetherall
 */
public class RMSearchGet extends DeclarativeWebScript
{
    /** Logger for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RMSearchGet.class);
    /** URL Parameters */
    private static final String PARAM_QUERY = "query";
    private static final String PARAM_SORTBY = "sortby";
    private static final String PARAM_FILTERS = "filters";
    private static final String PARAM_MAX_ITEMS = "maxitems";
    private static final String SYSTEM = "System";

    /** Records management search service */
    protected RecordsManagementSearchService recordsManagementSearchService;

    /** Site service */
    protected SiteService siteService;

    /** Namespace service */
    protected NamespaceService namespaceService;

    /** Node serivce */
    protected NodeService nodeService;

    /** Dictionary service */
    protected DictionaryService dictionaryService;

    /** Permission service */
    protected PermissionService permissionService;

    /** Person service */
    protected PersonService personService;

    /** Person data cache */
    private Map<String, String> personDataCache = null;

    /** Utility class for record categories */
    private RecordCategoryUtil recordCategoryUtil;

    /** Utility class for classification reasons (enterprise only) */
    private ClassificationReasonsUtil classificationReasonsUtil;

    /**
     * @param recordsManagementSearchService    records management search service
     */
    public void setRecordsManagementSearchService(RecordsManagementSearchService recordsManagementSearchService)
    {
        this.recordsManagementSearchService = recordsManagementSearchService;
    }

    /**
     * @param siteService   site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param recordCategoryUtil utility class for record categories
     */
    public void setRecordCategoryUtil(RecordCategoryUtil recordCategoryUtil)
    {
        this.recordCategoryUtil = recordCategoryUtil;
    }

    public void setClassificationReasonsUtil(ClassificationReasonsUtil classificationReasonsUtil)
    {
        this.classificationReasonsUtil = classificationReasonsUtil;
    }

    /**
     * @param personService person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<>(1);
        try
        {
            // Get the site id and confirm it is valid
            Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            String siteId = templateVars.get("site");
            if (siteId == null || siteId.length() == 0)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Site id not provided.");
            }
            if (siteService.getSite(siteId) == null)
            {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Site not found.");
            }

            // Get the query parameter
            String query = req.getParameter(PARAM_QUERY);
            // TODO check that this is there

            String sortby = req.getParameter(PARAM_SORTBY);
            // TODO this is optional

            String filters = req.getParameter(PARAM_FILTERS);
            // TODO this is optional

            //Replace any plain text reason ids with the appropriate node reference
            if(query.contains(REASONS_KEY))
            {
                query = classificationReasonsUtil.replaceReasonWithNodeRef(query);
            }

            // Convert into a rm search parameter object
            RecordsManagementSearchParameters searchParameters =
                    SavedSearchDetailsCompatibility.createSearchParameters(filters, new String[]{",", "/"}, sortby, namespaceService);

            // Set the max results
            String maxItems = req.getParameter(PARAM_MAX_ITEMS);
            if (maxItems != null && maxItems.length() != 0)
            {
                searchParameters.setMaxItems(Integer.parseInt(maxItems));
            }

            // Execute search
            List<Pair<NodeRef, NodeRef>> results = recordsManagementSearchService.search(siteId, query, searchParameters);

            // Reset person data cache
            personDataCache = new HashMap<>(57);

            // Process the result items
            List<Item> items = new ArrayList<>(results.size());
            for (Pair<NodeRef, NodeRef> pair : results)
            {
                // FIXME: See RM-478
                // TC 3-3  Create User Groups
                try
                {
                    Item item = new Item(pair.getFirst(), pair.getSecond());
                    items.add(item);
                }
                catch(Exception e)
                {
                    LOGGER.debug("Ignoring failed attempt to add item to search results.", e);
                }
            }

            // Return model
            model.put("items", items);
        }
        catch (Exception ex)
        {
            model.put("errorMessage", ex.toString());
        }
        return model;

    }

    /**
     * Item class to contain information about items being placed in model.
     */
    public class Item
    {
        private NodeRef nodeRef;
        private String type;
        private int size;
        private String parentFolder = "";
        private String browseUrl;
        private boolean isContainer;
        private String modifiedBy;
        private String createdBy;
        private Map<QName, Serializable> nodeProperties;
        private Map<String, Serializable> properties;
        private String recordCategoryId;

        public Item(NodeRef parent, NodeRef nodeRef)
        {
            // Set node ref
            this.nodeRef = nodeRef;

            // Get type
            QName nodeRefType = nodeService.getType(nodeRef);
            this.type = nodeRefType.toPrefixString(namespaceService);

            // Get properties
            this.nodeProperties = nodeService.getProperties(nodeRef);

            // Determine if container or not
            isContainer = true;
            if (dictionaryService.isSubClass(nodeRefType, ContentModel.TYPE_CONTENT))
            {
                isContainer = false;
            }

            // Get parent node reference
//            NodeRef parent = null;
//            ChildAssociationRef assoc = nodeService.getPrimaryParent(nodeRef);
//            if (assoc != null)
//            {
//                parent = assoc.getParentRef();
//            }

            if (isContainer)
            {
                this.size = -1;

                String displayPath = nodeService.getPath(nodeRef).toDisplayPath(nodeService, permissionService);
                String[] pathElements = displayPath.split("/");
                if (pathElements.length >= 5)
                {
                    if (pathElements.length > 5)
                    {
                        this.parentFolder = (String)nodeService.getProperty(parent, ContentModel.PROP_NAME);
                    }

                    pathElements = (String[])ArrayUtils.subarray(pathElements, 5, pathElements.length);
                    String newPath = StringUtils.join(pathElements, "/");
                    StringBuilder relPath = new StringBuilder("/").append(newPath);
                    if (relPath.length() > 1)
                    {
                        relPath.append("/");
                    }
                    relPath.append(getName());
                    try
                    {
                        this.browseUrl = "documentlibrary?path=" + URLEncoder.encode(relPath.toString(), "UTF-8").replaceAll("\\+","%20");
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        throw new AlfrescoRuntimeException("Could not process search results.", e);
                    }
                }
            }
            else
            {
                // Get the document size
                ContentData contentData = (ContentData)nodeProperties.get(ContentModel.PROP_CONTENT);
                this.size = 0;
                if (contentData != null)
                {
                    this.size = (int)contentData.getSize();
                }

                // Set the document parent name
                if (parent != null)
                {
                    this.parentFolder = (String)nodeService.getProperty(parent, ContentModel.PROP_NAME);
                }

                // Set the document browse URL
                this.browseUrl = "document-details?nodeRef=" + nodeRef.toString();
            }

            this.modifiedBy = getDisplayName(getModifiedByUser());
            this.createdBy = getDisplayName(getCreatedByUser());

            // Process the custom properties
            properties = new HashMap<>(nodeProperties.size());
            for (Map.Entry<QName, Serializable> entry : nodeProperties.entrySet())
            {
                QName qName = entry.getKey().getPrefixedQName(namespaceService);
                if (!NamespaceService.SYSTEM_MODEL_1_0_URI.equals(qName.getNamespaceURI()))
                {
                    String prefixName = qName.getPrefixString().replace(":", "_");
                    properties.put(prefixName, entry.getValue());
                }
            }
            properties.put("rma_recordCategoryIdentifier", recordCategoryUtil.getCategoryIdFromNodeId(nodeRef, false));
        }

        private String getDisplayName(String userName)
        {
            //Fix for RM-6834
            if(userName.equals(SYSTEM))
            {
                return SYSTEM;
            }

            String result = personDataCache.get(userName);
            if (result == null)
            {
                NodeRef person = personService.getPerson(userName);
                if (person != null)
                {
                    StringBuilder displayName = new StringBuilder(128);
                    displayName.append(nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME))
                               .append(" ")
                               .append(nodeService.getProperty(person, ContentModel.PROP_LASTNAME));
                    result = displayName.toString();
                }
                else
                {
                    result = userName;
                }
                personDataCache.put(userName, result);
            }

            return result;
        }

        public NodeRef getNodeRef()
        {
            return nodeRef;
        }

        public String getType()
        {
            return type;
        }

        public String getName()
        {
            return (String)nodeProperties.get(ContentModel.PROP_NAME);
        }

        public String getTitle()
        {
            return (String)nodeProperties.get(ContentModel.PROP_TITLE);
        }

        public String getDescription()
        {
            return (String)nodeProperties.get(ContentModel.PROP_DESCRIPTION);
        }

        public Date getModifiedOn()
        {
            return (Date)nodeProperties.get(ContentModel.PROP_MODIFIED);
        }

        public String getModifiedByUser()
        {
            return (String)nodeProperties.get(ContentModel.PROP_MODIFIER);
        }

        public String getModifiedBy()
        {
            return modifiedBy;
        }

        public Date getCreatedOn()
        {
            return (Date)nodeProperties.get(ContentModel.PROP_CREATED);
        }

        public String getCreatedByUser()
        {
            return (String)nodeProperties.get(ContentModel.PROP_CREATOR);
        }

        public String getCreatedBy()
        {
            return createdBy;
        }

        public String getAuthor()
        {
            return (String)nodeProperties.get(ContentModel.PROP_AUTHOR);
        }

        public String getParentFolder()
        {
            return parentFolder;
        }

        public int getSize()
        {
            return size;
        }

        public String getBrowseUrl()
        {
            return browseUrl;
        }

        public Map<String, Serializable> getProperties()
        {
            return properties;
        }

        public String getRecordCategoryId()
        {
            return recordCategoryId;
        }

        public void setRecordCategoryId(String recordCategoryId)
        {
            this.recordCategoryId = recordCategoryId;
        }
    }
}
