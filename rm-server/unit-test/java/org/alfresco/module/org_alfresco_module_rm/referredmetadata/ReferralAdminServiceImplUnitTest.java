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
import static org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.ChainedMetadataReferralUnsupported;
import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.expectedException;
import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.dictionary.DictionaryService;
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
 * Unit tests for {@link ReferralAdminServiceImpl}.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class ReferralAdminServiceImplUnitTest
{
    @InjectMocks private final ReferralAdminServiceImpl referralAdminService = new ReferralAdminServiceImpl();

    @Mock DictionaryService           mockDictionaryService;
    @Mock NodeService                 mockNodeService;
    @Mock ReferralRegistry            mockRegistry;
    @Mock ReferredMetadataServiceImpl mockReferredMetadataService;

    private final NodeRef node1 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "node1");
    private final NodeRef node2 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "node2");
    private final NodeRef node3 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "node3");

    private final QName assoc1 = QName.createQName("test", "assoc1");
    private final QName aspect1 = QName.createQName("test", "aspect1");
    private final QName aspect2 = QName.createQName("test", "aspect2");

    private final QName assoc2 = QName.createQName("test", "assoc2");
    private final QName aspect3 = QName.createQName("test", "aspect3");

    private final MetadataReferral referral1 = new MetadataReferral()
                                           {{
                                               this.setAssocType(assoc1);
                                               this.setAspects(asSet(aspect1, aspect2));
                                           }};
    private final MetadataReferral referral2 = new MetadataReferral()
                                           {{
                                               this.setAssocType(assoc2);
                                               this.setAspects(asSet(aspect3));
                                           }};

    @Before public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        when(mockRegistry.getMetadataReferrals()).thenReturn(asSet(referral1, referral2));
    }

    @Test(expected=IllegalArgumentException.class)
    public void attachingReferrerWithNoAspectConfiguredShouldFail()
    {
        referralAdminService.attachReferrer(node2, node1, aspect1);
    }

    @Test public void attachDetach()
    {
        when(mockRegistry.getReferralForAspect(aspect1)).thenReturn(referral1);

        // attach
        MetadataReferral d = attachReferrer(node1, node2, aspect1);

        // validate
        assertEquals(assoc1, d.getAssocType());
        assertEquals(asSet(aspect1, aspect2), d.getAspects());
        assertTrue(mockReferredMetadataService.isReferringMetadata(node1, aspect1));
        assertFalse(mockReferredMetadataService.isReferringMetadata(node1, aspect3));

        // detach
        assertEquals(d, referralAdminService.detachReferrer(node1, aspect1));
    }

    private MetadataReferral attachReferrer(NodeRef referrer, NodeRef referent, QName aspectName)
    {
        MetadataReferral mr = referralAdminService.attachReferrer(referrer, referent, aspectName);
        final QName assocType = mr.getAssocType();
        when(mockNodeService.getSourceAssocs(referent, assocType)).thenReturn(asList(new AssociationRef(referrer, assocType, referent)));
        when(mockNodeService.getTargetAssocs(referrer, assocType)).thenReturn(asList(new AssociationRef(referrer, assocType, referent)));
        for (QName aspect : mr.getAspects())
        {
            when(mockReferredMetadataService.isReferringMetadata(referrer, aspect)).thenReturn(true);
        }
        return mr;
    }

    @Test public void chainsOfDelegationShouldBePrevented()
    {
        when(mockRegistry.getReferralForAspect(aspect1)).thenReturn(referral1);

        // The node already has a delegation in place: node1 -> node2. We're trying to add to the
        // end of the chain: node2 -> node3
        when(mockNodeService.getSourceAssocs(node2, assoc1)).thenReturn(asList(new AssociationRef(node1, assoc1, node2)));
        when(mockNodeService.getTargetAssocs(node1, assoc1)).thenReturn(asList(new AssociationRef(node1, assoc1, node2)));

        expectedException(ChainedMetadataReferralUnsupported.class, () -> {
            referralAdminService.attachReferrer(node2, node3, aspect1);
            return null;
        });

        // Now try to add to the start of the chain: node3 -> node1
        expectedException(ChainedMetadataReferralUnsupported.class, () -> {
            referralAdminService.attachReferrer(node3, node1, aspect1);
            return null;
        });
    }
}
