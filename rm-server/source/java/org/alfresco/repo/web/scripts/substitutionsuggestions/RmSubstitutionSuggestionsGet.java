/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.substitutionsuggestions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.repo.action.parameter.ParameterProcessorComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.cxf.common.util.StringUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get substitution suggestions
 * given a text fragment (e.g. date.month for 'mon').
 *
 * @author Mark Hibbins
 * @since 2.2
 */
public class RmSubstitutionSuggestionsGet extends DeclarativeWebScript
{
    private final static String FRAGMENT_PARAMETER = "fragment";
    private final static String PATH_PARAMETER = "path";

    private final static String SUBSTITUTIONS_MODEL_KEY = "substitutions";

    private final static String RECORD_FOLDER_TYPE = "recordFolder";
    private final static String RECORD_CATEGORY_TYPE = "recordCategory";

    private final static String CREATE_CAPABILITY = "Create";
    private final static String VIEW_CAPABILITY = "ViewRecords";

    private final static int DEFAULT_SUBSTITUTION_MINIMUM_FRAGMENT_LENGTH = 0;
    private final static int DEFAULT_MAXIMUM_NUMBER_PATH_SUGGESTIONS = 10;

    private int pathSubstitutionMaximumNumberSuggestions = DEFAULT_MAXIMUM_NUMBER_PATH_SUGGESTIONS;
    private int substitutionMinimumFragmentSize = DEFAULT_SUBSTITUTION_MINIMUM_FRAGMENT_LENGTH;

    private ParameterProcessorComponent parameterProcessorComponent;
    private NodeService nodeService;
    private FilePlanService filePlanService;
    private CapabilityService capabilityService;

    /**
     * Set the parameter processor component bean
     *
     * @param parameterProcessorComponent
     */
    public void setParameterProcessorComponent(ParameterProcessorComponent parameterProcessorComponent)
    {
        this.parameterProcessorComponent = parameterProcessorComponent;
    }

    /**
     * Set the parameter processor component bean
     *
     * @param parameterProcessorComponent
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * Set the minimum fragment size to process for suggestion processing
     *
     * @param maximumNumberSuggestions
     */
    public void setSubstitutionMinimumFragmentSize(int substitutionMinimumFragmentSize)
    {
        this.substitutionMinimumFragmentSize = Math.max(substitutionMinimumFragmentSize, DEFAULT_SUBSTITUTION_MINIMUM_FRAGMENT_LENGTH);
    }

    /**
     * Set the maxmimum number of suggestions returned from the global property
     *
     * @param maximumNumberSuggestions
     */
    public void setPathSubstitutionMaximumNumberSuggestions(int pathSubstitutionMaximumNumberSuggestions)
    {
        this.pathSubstitutionMaximumNumberSuggestions = (pathSubstitutionMaximumNumberSuggestions <= 0 ? DEFAULT_MAXIMUM_NUMBER_PATH_SUGGESTIONS: pathSubstitutionMaximumNumberSuggestions);
    }

    /**
     * Return a list of substitutions for the given fragment.
     *
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String fragment = req.getParameter(FRAGMENT_PARAMETER);
        String path = req.getParameter(PATH_PARAMETER);

        List<String> substitutionSuggestions = new ArrayList<String>();

        if((fragment != null) && (fragment.length() >= this.substitutionMinimumFragmentSize))
        {
            substitutionSuggestions.addAll(getSubPathSuggestions(req, path, fragment));
            substitutionSuggestions.addAll(this.parameterProcessorComponent.getSubstitutionSuggestions(fragment));
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(SUBSTITUTIONS_MODEL_KEY, substitutionSuggestions);

        return model;
    }

    /**
     * Return a list of path suggestions for the path fragment supplied.
     *
     * @param path
     * @param fragment
     * @return
     */
    private List<String> getSubPathSuggestions(WebScriptRequest req, final String path, final String fragment) {
        List<String> pathSuggestions = new ArrayList<String>();
        if((path != null) && path.startsWith("/") && (fragment != null))
        {
            String[] pathFragments = path.split("/");

            NodeRef currentNode = getFilePlan(req);
            for(String pathFragment : pathFragments)
            {
                // ignore empty elements of the path produced by split
                if(!pathFragment.isEmpty())
                {
                    boolean foundThisPathFragment = false;
                    List<ChildAssociationRef> children = nodeService.getChildAssocs(currentNode);
                    for (ChildAssociationRef childAssoc : children) {
                        NodeRef childNodeRef = childAssoc.getChildRef();
                        String fileName = (String) nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME);
                        if(fileName.equals(pathFragment) && isNodeRefAppropriateForPathSuggestion(childNodeRef))
                        {
                            foundThisPathFragment = true;
                            currentNode = childNodeRef;
                            break;
                        }
                    }
                    if(!foundThisPathFragment)
                    {
                        currentNode = null;
                        break;
                    }
                }
            }

            if(currentNode != null)
            {
                String lowerCaseFragment = fragment.toLowerCase();
                List<ChildAssociationRef> children = nodeService.getChildAssocs(currentNode);
                for (ChildAssociationRef childAssoc : children) {
                    NodeRef childNodeRef = childAssoc.getChildRef();
                    String fileName = (String) nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME);
                    if((fragment.isEmpty() || fileName.toLowerCase().startsWith(lowerCaseFragment)) && isNodeRefAppropriateForPathSuggestion(childNodeRef))
                    {
                        pathSuggestions.add("/" + fileName);
                        if(pathSuggestions.size() >= pathSubstitutionMaximumNumberSuggestions)
                        {
                            break;
                        }
                    }
                }
            }
        }
        return pathSuggestions;
    }

    /**
     * Utility method to get the file plan from the passed parameters.
     *
     * @param req
     * @return
     */
    protected NodeRef getFilePlan(WebScriptRequest req)
    {
        NodeRef filePlan = null;

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String siteId = templateVars.get("siteid");
        if (siteId != null)
        {
            filePlan = filePlanService.getFilePlanBySiteId(siteId);
        }

        if (filePlan == null)
        {
            String storeType = templateVars.get("store_type");
            String storeId = templateVars.get("store_id");
            String id = templateVars.get("id");

            if (!StringUtils.isEmpty(storeType) &&
                !StringUtils.isEmpty(storeId) &&
                !StringUtils.isEmpty(id))
            {
                StoreRef storeRef = new StoreRef(storeType, storeId);
                NodeRef nodeRef = new NodeRef(storeRef, id);
                if (filePlanService.isFilePlan(nodeRef))
                {
                    filePlan = nodeRef;
                }
            }
        }

        if (filePlan == null)
        {
            // Assume we are in a legacy repository and we will grab the default file plan
            filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        }

        return filePlan;
    }

    /**
     * Identifies record category and record folder types of nodeRef
     *
     * @param nodeRef  Instance of NodeRef to be tested
     * @return True if the passed NodeRef instance is a record category or record folder
     */
    private boolean isNodeRefAppropriateForPathSuggestion(NodeRef nodeRef)
    {
        // check node type
        QName type = nodeService.getType(nodeRef);
        String typeLocalName = type.getLocalName();
        boolean isCorrectType = (RECORD_FOLDER_TYPE.equals(typeLocalName) || RECORD_CATEGORY_TYPE.equals(typeLocalName));

        // check permissions
        boolean canView = false;
        if(isCorrectType)
        {
            Capability createCapability = capabilityService.getCapability(CREATE_CAPABILITY);
            Capability viewCapability = capabilityService.getCapability(VIEW_CAPABILITY);
            if ((createCapability != null) && (viewCapability != null))
            {
                List<String> requiredCapabilities = new ArrayList<String>();
                requiredCapabilities.add(CREATE_CAPABILITY);
                requiredCapabilities.add(VIEW_CAPABILITY);
                Map<Capability, AccessStatus> map = capabilityService.getCapabilitiesAccessState(nodeRef, requiredCapabilities);
                if (map.containsKey(createCapability) && map.containsKey(viewCapability))
                {
                    AccessStatus createAccessStatus = map.get(createCapability);
                    AccessStatus viewAccessStatus = map.get(viewCapability);
                    if (createAccessStatus.equals(AccessStatus.ALLOWED) && viewAccessStatus.equals(AccessStatus.ALLOWED))
                    {
                        canView = true;
                    }
                }
            }
        }

        return isCorrectType && canView;
    }
}