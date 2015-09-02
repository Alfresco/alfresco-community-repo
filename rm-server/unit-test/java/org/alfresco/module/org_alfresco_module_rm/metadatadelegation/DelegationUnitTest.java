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

import static java.util.Collections.emptySet;
import static org.alfresco.module.org_alfresco_module_rm.metadatadelegation.DelegationException.InvalidDelegation;
import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.expectedException;
import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asListFrom;
import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

/**
 * Unit tests for {@link Delegation}.
 *
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public class DelegationUnitTest
{
    @Mock DictionaryService mockDictionaryService;
    @Mock NodeService       mockNodeService;

    private final DelegationAdminServiceImpl metadataDelegationService = new DelegationAdminServiceImpl();

    private final NodeRef node1 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "node1");
    private final NodeRef node2 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "node2");
    private final QName aspect1 = QName.createQName("test", "aspect1");
    private final QName aspect2 = QName.createQName("test", "aspect2");
    private final QName assoc1  = QName.createQName("test", "assoc1");

    @Before public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        metadataDelegationService.setNodeService(mockNodeService);
    }

    @Test public void nullOrEmptyDelegatesAreForbidden()
    {
        List<Delegation> invalidDelegations = asListFrom(() -> new Delegation(),
                                                               () -> {
                                                                   Delegation d = new Delegation();
                                                                   d.setAssocType(assoc1);
                                                                   d.setAspects(null);
                                                                   d.setDictionaryService(mockDictionaryService);
                                                                   return d;
                                                               },
                                                               () -> {
                                                                   Delegation d = new Delegation();
                                                                   d.setAssocType(assoc1);
                                                                   d.setAspects(emptySet());
                                                                   d.setDictionaryService(mockDictionaryService);
                                                                   return d;
                                                               },
                                                               () -> {
                                                                   Delegation d = new Delegation();
                                                                   d.setAssocType(null);
                                                                   d.setAspects(asSet(aspect1, aspect2));
                                                                   d.setDictionaryService(mockDictionaryService);
                                                                   return d;
                                                               });

        invalidDelegations.stream()
                        .forEach(d -> expectedException(InvalidDelegation.class, () -> {
                                    d.validateAndRegister();
                                    return null;
                                }
                        ));
    }

    @Test(expected=InvalidDelegation.class)
    public void delegateMustHaveAssocThatExists()
    {
        when(mockDictionaryService.getAssociation(assoc1)).thenReturn(null);
        when(mockDictionaryService.getAspect(aspect1)).thenReturn(mock(AspectDefinition.class));

        Delegation d = new Delegation();
        d.setAssocType(assoc1);
        d.setAspects(asSet(aspect1));
        d.setDictionaryService(mockDictionaryService);
        d.validateAndRegister();
    }

    @Test(expected=InvalidDelegation.class)
    public void delegateMustHaveAspectsAllOfWhichExist()
    {
        when(mockDictionaryService.getAssociation(assoc1)).thenReturn(mock(AssociationDefinition.class));
        when(mockDictionaryService.getAspect(aspect1)).thenReturn(mock(AspectDefinition.class));
        when(mockDictionaryService.getAspect(aspect2)).thenReturn(null);

        Delegation d = new Delegation();
        d.setAssocType(assoc1);
        d.setAspects(asSet(aspect1, aspect2));
        d.setDictionaryService(mockDictionaryService);
        d.validateAndRegister();
    }
}
