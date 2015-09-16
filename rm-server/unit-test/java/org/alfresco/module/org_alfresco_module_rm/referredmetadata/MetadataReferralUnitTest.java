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

import static java.util.Collections.emptySet;
import static org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.InvalidMetadataReferral;
import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.expectedException;
import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asListFrom;
import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link MetadataReferral}.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class MetadataReferralUnitTest
{
    @Mock DictionaryService mockDictionaryService;
    @Mock NodeService       mockNodeService;

    private final ReferralAdminServiceImpl referralAdminService = new ReferralAdminServiceImpl();

    private final QName aspect1 = QName.createQName("test", "aspect1");
    private final QName aspect2 = QName.createQName("test", "aspect2");
    private final QName assoc1  = QName.createQName("test", "assoc1");

    @Before public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        referralAdminService.setNodeService(mockNodeService);
    }

    @Test public void nullOrEmptyReferralsAreForbidden()
    {
        asListFrom(() -> new MetadataReferral(),
                   () -> {
                       MetadataReferral mr = new MetadataReferral();
                       mr.setAssocType(assoc1);
                       mr.setAspects(null);
                       mr.setDictionaryService(mockDictionaryService);
                       return mr;
                   },
                   () -> {
                       MetadataReferral mr = new MetadataReferral();
                       mr.setAssocType(assoc1);
                       mr.setAspects(emptySet());
                       mr.setDictionaryService(mockDictionaryService);
                       return mr;
                   },
                   () -> {
                       MetadataReferral mr = new MetadataReferral();
                       mr.setAssocType(null);
                       mr.setAspects(asSet(aspect1, aspect2));
                       mr.setDictionaryService(mockDictionaryService);
                       return mr;
                   })
                .forEach(mr -> expectedException(InvalidMetadataReferral.class, () -> {
                            mr.validateAndRegister();
                            return null;
                        })
                );
    }

    @Test(expected=InvalidMetadataReferral.class)
    public void referralMustHaveAssocThatExists()
    {
        when(mockDictionaryService.getAssociation(assoc1)).thenReturn(null);
        when(mockDictionaryService.getAspect(aspect1)).thenReturn(mock(AspectDefinition.class));

        MetadataReferral mr = new MetadataReferral();
        mr.setAssocType(assoc1);
        mr.setAspects(asSet(aspect1));
        mr.setDictionaryService(mockDictionaryService);
        mr.validateAndRegister();
    }

    @Test(expected=InvalidMetadataReferral.class)
    public void referralMustHaveAspectsAllOfWhichExist()
    {
        when(mockDictionaryService.getAssociation(assoc1)).thenReturn(mock(AssociationDefinition.class));
        when(mockDictionaryService.getAspect(aspect1)).thenReturn(mock(AspectDefinition.class));
        when(mockDictionaryService.getAspect(aspect2)).thenReturn(null);

        MetadataReferral mr = new MetadataReferral();
        mr.setAssocType(assoc1);
        mr.setAspects(asSet(aspect1, aspect2));
        mr.setDictionaryService(mockDictionaryService);
        mr.validateAndRegister();
    }
}
