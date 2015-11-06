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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.expectedException;
import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.MetadataReferralNotFound;
import org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.ReferentNodeNotFound;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link ReferredMetadataServiceImpl}.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class ReferredMetadataServiceImplUnitTest
{
    @InjectMocks private final ReferredMetadataServiceImpl referredMetadataService = new ReferredMetadataServiceImpl();

    @Mock DictionaryService        mockDictionaryService;
    @Mock NodeService              mockNodeService;
    @Mock ReferralAdminServiceImpl mockReferralAdminService;
    @Mock ReferralRegistry         mockReferralRegistry;

    /** This node has a referent node. */
    private final NodeRef referringNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "referringNode");
    /** This is the referent for {@link #referringNode}. */
    private final NodeRef referentNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "referentNode");
    /** This node has no referent node. */
    private final NodeRef nodeWithoutReferent = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "nodeWithoutReferent");

    /** The type of the peer association that links the referringNode to its source. */
    private final QName referralAssocType = QName.createQName("test", "referralAssocType");
    /** The instance of the association between {@link #referringNode} and {@link #referentNode}. */
    private final AssociationRef attachedReferralAssocRef = new AssociationRef(referringNode, referralAssocType, referentNode);

    /** Name of an aspect that has been referred. */
    private final QName referredAspect1 = QName.createQName("test", "referredAspect1");
    /** Name of an aspect that has been referred. */
    private final QName referredAspect2 = QName.createQName("test", "referredAspect2");
    /** Name of a content class (a type in this case) that has not been referred.
     * N.B. Types can't be referred currently. */
    private final QName unreferredType = QName.createQName("test", "unreferredType");

    private final QName referredProp = QName.createQName("test", "referredProp");
    private final Serializable referredPropValue = "hello";
    private final QName unreferredProp = QName.createQName("test", "unreferredProp");

    private final MetadataReferral referral = new MetadataReferral()
                                           {{
                                               this.setAssocType(referralAssocType);
                                               this.setAspects(asSet(referredAspect1, referredAspect2));
                                           }};

    @SuppressWarnings("serial")
    @Before public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        final PropertyDefinition aspectProp = mock(PropertyDefinition.class);
        final ClassDefinition aspectDefn = mock(ClassDefinition.class);
        when(aspectDefn.getName()).thenReturn(referredAspect1);
        when(aspectProp.getContainerClass()).thenReturn(aspectDefn);
        when(aspectDefn.isAspect()).thenReturn(true);

        final PropertyDefinition typeProp = mock(PropertyDefinition.class);
        final ClassDefinition typeDefn = mock(ClassDefinition.class);
        when(typeDefn.getName()).thenReturn(unreferredType);
        when(typeProp.getContainerClass()).thenReturn(typeDefn);
        when(typeDefn.isAspect()).thenReturn(false);

        when(mockDictionaryService.getProperty(referredProp)).thenReturn(aspectProp);

        when(mockReferralAdminService.getAttachedReferralsFrom(referringNode)).thenReturn(asSet(referral));
        for (QName referredAspect : asSet(referredAspect1, referredAspect2))
        {
            when(mockReferralRegistry.getReferralForAspect(referredAspect)).thenReturn(referral);
            when(mockNodeService.hasAspect(referentNode, referredAspect)).thenReturn(true);
        }
        when(mockNodeService.getSourceAssocs(referentNode, referralAssocType)).thenReturn(asList(attachedReferralAssocRef));
        when(mockNodeService.getTargetAssocs(referringNode, referralAssocType)).thenReturn(asList(attachedReferralAssocRef));
        when(mockNodeService.exists(any(NodeRef.class))).thenReturn(true);
        when(mockNodeService.getProperties(referentNode))
                .thenReturn(new HashMap<QName, Serializable>()
                {{
                    this.put(referredProp, referredPropValue);
                }});
    }

    @Test public void isReferringMetadata()
    {
        assertTrue(referredMetadataService.isReferringMetadata(referringNode, referredAspect1));
        expectedException(MetadataReferralNotFound.class,
                () -> referredMetadataService.isReferringMetadata(nodeWithoutReferent, unreferredType));
        assertFalse(referredMetadataService.isReferringMetadata(nodeWithoutReferent, referredAspect1));
    }

    @Test public void getReferentNode()
    {
        assertEquals(referentNode, referredMetadataService.getReferentNode(referringNode, referredAspect1));
        expectedException(MetadataReferralNotFound.class,
                () -> {
                          referredMetadataService.getReferentNode(referringNode, unreferredType);
                          return null;
                      });
        assertNull(referredMetadataService.getReferentNode(nodeWithoutReferent, referredAspect1));
    }

    @Test public void getReferredProperties()
    {
        final Map<QName, Serializable> expectedProps = new HashMap<>();
        expectedProps.put(referredProp, referredPropValue);

        assertEquals(expectedProps, referredMetadataService.getReferredProperties(referringNode, referredAspect1));

        expectedException(MetadataReferralNotFound.class,
                () -> referredMetadataService.getReferredProperties(referringNode, unreferredType));

        expectedException(ReferentNodeNotFound.class,
                () -> referredMetadataService.getReferredProperties(nodeWithoutReferent, referredAspect1));
    }

    @Test public void getReferredProperty()
    {
        assertEquals(referredPropValue, referredMetadataService.getReferredProperty(referringNode, referredProp));

        expectedException(IllegalArgumentException.class,
                () -> referredMetadataService.getReferredProperty(referringNode, unreferredProp));

        expectedException(MetadataReferralNotFound.class,
                () -> referredMetadataService.getReferredProperties(nodeWithoutReferent, referredProp));
    }

    @Test public void hasReferredAspect()
    {
        assertTrue(referredMetadataService.hasReferredAspect(referringNode, referredAspect1));

        expectedException(MetadataReferralNotFound.class,
                () -> referredMetadataService.hasReferredAspect(referringNode, unreferredType));

        expectedException(ReferentNodeNotFound.class,
                () -> referredMetadataService.hasReferredAspect(nodeWithoutReferent, referredAspect1));
    }

    @Test public void getAttachedReferrals()
    {
        final Map<MetadataReferral, NodeRef> expectedReferrals = new HashMap<>();
        expectedReferrals.put(referral, referentNode);

        assertEquals(expectedReferrals, referredMetadataService.getAttachedReferrals(referringNode));
        assertEquals(emptyMap(), referredMetadataService.getAttachedReferrals(nodeWithoutReferent));
    }
}
