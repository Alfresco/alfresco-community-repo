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

package org.alfresco.module.org_alfresco_module_rm.script.slingshot.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * RM metadata used by form extension
 *
 * @author Roy Wetherall
 */
public class RMMetaDataGet extends DeclarativeWebScript
{
    /** Query parameters */
    private static final String PARAM_NODEREF = "noderef";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_EXTENDED = "extended";

    /** NodeRef pattern */
    private static final Pattern NODE_REF_PATTERN = Pattern.compile(".+://.+/.+");

    /** QName pattern */
    private static final Pattern QNAME_PATTERN = Pattern.compile(".+:[^=,]+");

    /** Namespace service */
    private NamespaceService namespaceService;

    /** Node service */
    private NodeService nodeService;

    /** File Plan Service */
    private FilePlanService filePlanService;

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // create model object with the lists model
        Map<String, Object> model = new HashMap<>(1);

        boolean extended = false;
        String result = "NONE";

        // Get the nodeRef and confirm it is valid
        String nodeRef = req.getParameter(PARAM_NODEREF);
        if (nodeRef == null || nodeRef.length() == 0)
        {
            String type = req.getParameter(PARAM_TYPE);
            if (type != null && type.length() != 0 && type.indexOf(':') != -1)
            {
            	Matcher m = QNAME_PATTERN.matcher(type);
            	if (m.matches())
            	{
	                QName qname = QName.createQName(type, namespaceService);
	                FilePlanComponentKind kind = filePlanService.getFilePlanComponentKindFromType(qname);
	                if (kind != null)
	                {
	                    result = kind.toString();
	                }
            	}
            }
        }
        else
        {
            // quick test before running slow match for full NodeRef pattern
            if (nodeRef.indexOf(':') != -1)
            {
                Matcher m = NODE_REF_PATTERN.matcher(nodeRef);
                if (m.matches())
                {
                    NodeRef nodeRefObj = new NodeRef(nodeRef);

                    FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRefObj);
                    if (kind != null)
                    {
                        result = kind.toString();
                    }

                    String extendedValue = req.getParameter(PARAM_EXTENDED);
                    if (extendedValue != null && extendedValue.length() != 0)
                    {
                        extended = Boolean.parseBoolean(extendedValue);
                        if (extended)
                        {
                            // get the aspects of the node
                            model.put("aspects", getAspects(nodeRefObj));
                        }
                    }
                }
            }
        }

        model.put("kind", result);
        model.put("extended", extended);
        return model;
    }

    /**
     * Gets the current node aspects
     *
     * @return node aspects
     */
    public List<Aspect> getAspects(NodeRef nodeRef)
    {
        Set<QName> qnames = nodeService.getAspects(nodeRef);
        List<Aspect> aspects = new ArrayList<>(qnames.size());
        for (QName qname : qnames)
        {
            aspects.add(new Aspect(qname));
        }
        return aspects;
    }

    /**
     * Qname wrapper class
     */
    public class QNameBean implements Serializable
    {
        private static final long serialVersionUID = 6982292337846270774L;

        protected QName name;

        public QNameBean(QName name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name.toString();
        }

        public String getPrefixedName()
        {
            return name.toPrefixString(namespaceService);
        }

        public String toString()
        {
            return getName();
        }
    }

    /**
     * Aspect wrapper class
     */
    public class Aspect extends QNameBean
    {
        private static final long serialVersionUID = -6448182941386934326L;

        public Aspect(QName name)
        {
            super(name);
        }
    }
}
