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
package org.alfresco.module.org_alfresco_module_rm.referredmetadata;

import static org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.MetadataReferralNotFound;
import static org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.ReferentNodeNotFound;
import static org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.TypeMetadataReferralUnsupported;
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
 * @since 2.4.a
 */
public class ReferredMetadataServiceImpl implements ReferredMetadataService
{
    private ReferralAdminService referralAdminService;
    private ReferralRegistry     referralRegistry;
    private DictionaryService    dictionaryService;
    private NodeService          nodeService;

    public void setReferralAdminService(ReferralAdminService service)
    {
        this.referralAdminService = service;
    }

    public void setReferralRegistry(ReferralRegistry registry)
    {
        this.referralRegistry = registry;
    }

    public void setDictionaryService(DictionaryService service)
    {
        this.dictionaryService = service;
    }

    public void setNodeService(NodeService service)
    {
        this.nodeService = service;
    }

    @Override public boolean isReferringMetadata(NodeRef potentialReferrer, QName aspectName)
    {
        if ( !nodeService.exists(potentialReferrer))
        {
            throw new InvalidNodeRefException(potentialReferrer);
        }

        final MetadataReferral metadataReferral = referralRegistry.getReferralForAspect(aspectName);

        if (metadataReferral == null)
        {
            throw new MetadataReferralNotFound("No defined referral found for aspect: " + aspectName);
        }
        else
        {
            final List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(potentialReferrer, metadataReferral.getAssocType());
            return !targetAssocs.isEmpty();
        }
    }

    @Override public NodeRef getReferentNode(NodeRef referrer, QName aspectName)
    {
        if ( !nodeService.exists(referrer))
        {
            throw new InvalidNodeRefException(referrer);
        }

        final MetadataReferral d = referralRegistry.getReferralForAspect(aspectName);

        if (d == null)
        {
            throw new MetadataReferralNotFound("No defined referral found for aspect: " + aspectName);
        }
        else
        {
            final QName assocType = d.getAssocType();
            final List<AssociationRef> assocs = nodeService.getTargetAssocs(referrer, assocType);

            return assocs.isEmpty() ? null : assocs.get(0).getTargetRef();
        }
    }

    @Override public Map<QName, Serializable> getReferredProperties(NodeRef referrer, final QName aspectName)
    {
        final NodeRef referentNode = getReferentNode(referrer, aspectName);

        if (referentNode == null)
        {
            throw new ReferentNodeNotFound("No referent node found for " + referrer + " " + aspectName);
        }
        else
        {
            final Map<QName, Serializable> allProps = nodeService.getProperties(referentNode);
            final Map<QName, Serializable> aspectProps = filterKeys(allProps,
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

    @Override public Serializable getReferredProperty(NodeRef referrer, QName propertyName)
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

            throw new TypeMetadataReferralUnsupported(msg.toString());
        }

        final Map<QName, Serializable> allPropValues = getReferredProperties(referrer, aspectDefn.getName());
        return allPropValues.get(propertyName);
    }

    @Override public boolean hasReferredAspect(NodeRef referrer, QName aspectName)
    {
        final NodeRef referentNode = getReferentNode(referrer, aspectName);

        if (referentNode == null)
        {
            throw new ReferentNodeNotFound("No referent node found for " + referrer + " " + aspectName);
        }
        else
        {
            return nodeService.hasAspect(referentNode, aspectName);
        }
    }

    @Override public Map<MetadataReferral, NodeRef> getAttachedReferrals(NodeRef potentialReferrer)
    {
        Set<MetadataReferral> metadataReferrals = referralAdminService.getAttachedReferralsFrom(potentialReferrer);

        Map<MetadataReferral, NodeRef> result = new HashMap<>();
        for (MetadataReferral mr : metadataReferrals)
        {
            // We need only use the first aspect to get the MetadataReferral object
            if (!mr.getAspects().isEmpty())
            {
                result.put(mr, getReferentNode(potentialReferrer, mr.getAspects().iterator().next()));
            }
        }

        return result;
    }
}

