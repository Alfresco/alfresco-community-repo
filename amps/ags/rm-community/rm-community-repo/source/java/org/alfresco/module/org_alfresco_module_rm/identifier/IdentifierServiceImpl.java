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

package org.alfresco.module.org_alfresco_module_rm.identifier;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roy Wetherall
 */
public class IdentifierServiceImpl implements IdentifierService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(IdentifierServiceImpl.class);

    /** Registry map */
    private Map<QName, IdentifierGenerator> register = new HashMap<>(5);

    /** Node service */
    private NodeService nodeService;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /**
     * Set the node service
     *
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the dictionary service
     *
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService#generateIdentifier(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String generateIdentifier(QName type, NodeRef parent)
    {
        ParameterCheck.mandatory("type", type);

        // Build the context
        Map<String, Serializable> context = new HashMap<>(2);
        if (parent != null)
        {
            context.put(CONTEXT_PARENT_NODEREF, parent);
        }
        context.put(CONTEXT_ORIG_TYPE, type);

        // Generate the id
        return generateIdentifier(type, context);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService#generateIdentifier(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String generateIdentifier(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        Map<String, Serializable> context = new HashMap<>(3);

        // Set the original type
        QName type = nodeService.getType(nodeRef);
        context.put(CONTEXT_ORIG_TYPE, type);

        // Set the parent reference
        ChildAssociationRef assocRef = nodeService.getPrimaryParent(nodeRef);
        if (assocRef != null && assocRef.getParentRef() != null)
        {
            context.put(CONTEXT_PARENT_NODEREF, assocRef.getParentRef());
        }

        // Set the node reference
        context.put(CONTEXT_NODEREF, nodeRef);

        // Generate the identifier
        return generateIdentifier(type, context);

    }

    /**
     * Generate an identifier for a given type of object with the accompanying context.
     *
     * @param type      content type
     * @param context   context
     * @return String   identifier
     */
    private String generateIdentifier(QName type, Map<String, Serializable> context)
    {
        ParameterCheck.mandatory("type", type);
        ParameterCheck.mandatory("context", context);

        // Get the identifier generator
        IdentifierGenerator idGen = lookupGenerator(type);
        if (idGen == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unable to generate id for object of type " + type.toString() + ", because no identifier generator was found.");
            }
            throw new AlfrescoRuntimeException("Unable to generate id for object of type " + type.toString() + ", because no identifier generator was found.");
        }

        // Generate the identifier
        return idGen.generateId(context);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService#register(org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierGenerator)
     */
    public void register(IdentifierGenerator idGen)
    {
        register.put(idGen.getType(), idGen);
    }

    /**
     *
     * @param type content type (could be aspect or type)
     * @return
     */
    private IdentifierGenerator lookupGenerator(QName type)
    {
        ParameterCheck.mandatory("type", type);

        if (logger.isDebugEnabled())
        {
            logger.debug("Looking for idGenerator for type " + type.toString());
        }

        // Look for the generator related to the type
        IdentifierGenerator result = register.get(type);
        if (result == null)
        {
            // Check the parent type
            ClassDefinition typeDef = dictionaryService.getClass(type);
            if (typeDef != null)
            {
                QName parentType = typeDef.getParentName();
                if (parentType != null)
                {
                    // Recurse to find parent type generator
                    result = lookupGenerator(parentType);
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Unable to find type definition for " + type.toString() + " when generating identifier.");
                }
            }
        }
        return result;
    }
}
