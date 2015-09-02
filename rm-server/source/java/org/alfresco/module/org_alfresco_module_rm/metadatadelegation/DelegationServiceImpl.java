/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.metadatadelegation;

import static org.alfresco.module.org_alfresco_module_rm.metadatadelegation.DelegationException.DelegateNotFound;
import static org.alfresco.module.org_alfresco_module_rm.metadatadelegation.DelegationException.DelegationNotFound;
import static org.alfresco.util.collections.CollectionUtils.filterKeys;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.collections.Function;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public class DelegationServiceImpl implements DelegationService
{
    private DelegationAdminService delegationAdminService;
    private DictionaryService      dictionaryService;
    private NodeService            nodeService;

    public void setDelegationAdminService(DelegationAdminService service)
    {
        this.delegationAdminService = service;
    }

    public void setDictionaryService(DictionaryService service)
    {
        this.dictionaryService = service;
    }

    public void setNodeService(NodeService service)
    {
        this.nodeService = service;
    }

    @Override public boolean hasDelegateForAspect(NodeRef nodeRef, QName aspectName)
    {
        final Delegation delegation = delegationAdminService.getDelegationFor(aspectName);

        if ( !nodeService.exists(nodeRef))
        {
            throw new InvalidNodeRefException(nodeRef);
        }
        else if (delegation == null)
        {
            throw new DelegationNotFound("No delegation found for aspect: " + aspectName);
        }
        else
        {
            final List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(nodeRef, delegation.getAssocType());
            return !targetAssocs.isEmpty();
        }
    }

    @Override public NodeRef getDelegateFor(NodeRef nodeRef, QName aspectName)
    {
        final Delegation d = delegationAdminService.getDelegationFor(aspectName);

        if (d == null)
        {
            throw new DelegationNotFound("No delegation found for aspect: " + aspectName);
        }
        else
        {
            final QName assocType = d.getAssocType();
            final List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, assocType);

            return assocs.isEmpty() ? null : assocs.get(0).getTargetRef();
        }
    }

    @Override public Map<QName, Serializable> getDelegateProperties(NodeRef nodeRef, final QName aspectName)
    {
        final NodeRef delegateNode = getDelegateFor(nodeRef, aspectName);

        if (delegateNode == null)
        {
            throw new DelegateNotFound("No delegate node found for " + nodeRef + " " + aspectName);
        }
        else
        {
            Map<QName, Serializable> allProps = nodeService.getProperties(delegateNode);
            Map<QName, Serializable> aspectProps = filterKeys(allProps,
                                      new Function<QName, Boolean>()
                                        {
                                            @Override public Boolean apply(QName propName)
                                            {
                                                final QName containerClassname = dictionaryService.getProperty(propName)
                                                                                                  .getContainerClass()
                                                                                                  .getName();
                                                return containerClassname.equals(aspectName);
                                            }
                                        });
            return aspectProps;
        }
    }

    @Override public Serializable getDelegateProperty(NodeRef nodeRef, QName propertyName)
    {
        final PropertyDefinition propDefn = dictionaryService.getProperty(propertyName);

        if (propDefn == null)
        {
            throw new IllegalArgumentException("Property " + propertyName + " not found.");
        }

        final ClassDefinition aspectDefn = propDefn.getContainerClass();
        if (!aspectDefn.isAspect())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Property '").append(propertyName).append("' is not defined on an aspect: ")
               .append(aspectDefn.getName());

            throw new IllegalArgumentException(msg.toString());
        }

        Map<QName, Serializable> allPropValues = getDelegateProperties(nodeRef, aspectDefn.getName());
        return allPropValues.get(propertyName);
    }

    @Override public boolean hasAspectOnDelegate(NodeRef nodeRef, QName aspectName)
    {
        final NodeRef delegateNode = getDelegateFor(nodeRef, aspectName);

        if (delegateNode == null)
        {
            throw new DelegateNotFound("No delegate node found for " + nodeRef + " " + aspectName);
        }
        else
        {
            return nodeService.hasAspect(delegateNode, aspectName);
        }
    }

    @Override public Map<Delegation, NodeRef> getDelegations(NodeRef nodeRef)
    {
        Set<Delegation> delegations = delegationAdminService.getDelegationsFrom(nodeRef);

        Map<Delegation, NodeRef> result = new HashMap<>();
        for (Delegation d : delegations)
        {
            // We need only use the first aspect to get the Delegation object
            if (!d.getAspects().isEmpty())
            {
                result.put(d, getDelegateFor(nodeRef, d.getAspects().iterator().next()));
            }
        }

        return result;
    }
}

