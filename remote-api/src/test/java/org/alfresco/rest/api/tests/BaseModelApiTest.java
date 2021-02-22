/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.rest.api.tests;

import com.google.common.collect.ImmutableList;
import org.alfresco.rest.api.model.Association;
import org.alfresco.rest.api.model.AssociationSource;
import org.alfresco.rest.api.model.Model;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.data.Aspect;
import org.alfresco.rest.api.tests.client.data.Type;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;

public class BaseModelApiTest extends AbstractBaseApiTest
{
    PublicApiClient.ListResponse<Aspect> aspects = null;
    Aspect aspect = null, childAspect = null, smartFilterAspect = null,
            rescanAspect = null, testAspect = null, testAllAspect = null;

    PublicApiClient.ListResponse<Type> types = null;
    Type type = null, whitePaperType = null, docType = null, publishableType = null, apiBaseType = null,
            apiFileType = null, apiFileDerivedType = null, apiForcedType = null, apiFileDerivedNoArchiveType = null,
            apiFolderType = null, apiOverrideType = null, apiOverride2Type = null, apiOverride3Type = null, apiNamedPropConstraintType = null;

    List<Type> allTypes = null;

    PublicApiClient.Paging paging = getPaging(0, 10);
    Map<String, String> otherParams = new HashMap<>();

    @Before
    public void setup() throws Exception
    {
        super.setup();

        Model myCompanyModel = new Model();
        myCompanyModel.setAuthor("Administrator");
        myCompanyModel.setId("mycompany:model");
        myCompanyModel.setNamespaceUri("http://www.mycompany.com/model/finance/1.0");
        myCompanyModel.setNamespacePrefix("mycompany");

        Model scanModel = new Model();
        scanModel.setAuthor("Administrator");
        scanModel.setId("test:scan");
        scanModel.setNamespaceUri("http://www.test.com/model/account/1.0");
        scanModel.setNamespacePrefix("test");

        testAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        testAspect.setId("mycompany:testAspect");
        testAspect.setTitle("Test Aspect");
        testAspect.setModel(myCompanyModel);
        testAspect.setIsContainer(false);
        testAspect.setIncludedInSupertypeQuery(true);
        testAspect.setIsArchive(true);

        childAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        childAspect.setId("mycompany:childAspect");
        childAspect.setTitle("Child Aspect");
        childAspect.setDescription("Child Aspect Description");
        childAspect.setParentId("smf:smartFolder");
        childAspect.setModel(myCompanyModel);
        childAspect.setIsContainer(false);
        childAspect.setIncludedInSupertypeQuery(true);

        rescanAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        rescanAspect.setId("test:rescan");
        rescanAspect.setTitle("rescan");
        rescanAspect.setDescription("Doc that required to scan ");
        rescanAspect.setModel(scanModel);
        rescanAspect.setIsContainer(false);
        rescanAspect.setIncludedInSupertypeQuery(true);

        smartFilterAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        smartFilterAspect.setId("test:smartFilter");
        smartFilterAspect.setTitle("Smart filter");
        smartFilterAspect.setDescription("Smart Filter");
        smartFilterAspect.setParentId("mycompany:testAspect");
        smartFilterAspect.setModel(scanModel);
        smartFilterAspect.setIsContainer(false);
        smartFilterAspect.setIsArchive(true);
        smartFilterAspect.setIncludedInSupertypeQuery(true);

        whitePaperType = new org.alfresco.rest.api.tests.client.data.Type();
        whitePaperType.setId("mycompany:whitepaper");
        whitePaperType.setTitle("whitepaper");
        whitePaperType.setDescription("Whitepaper");
        whitePaperType.setParentId("mycompany:doc");
        whitePaperType.setModel(myCompanyModel);
        whitePaperType.setIsContainer(false);
        whitePaperType.setIsArchive(true);
        whitePaperType.setIncludedInSupertypeQuery(true);

        docType = new org.alfresco.rest.api.tests.client.data.Type();
        docType.setId("mycompany:doc");
        docType.setTitle("doc");
        docType.setDescription("Doc");
        docType.setParentId("cm:content");
        docType.setModel(myCompanyModel);
        docType.setIsContainer(false);
        docType.setIsArchive(true);
        docType.setIncludedInSupertypeQuery(true);

        publishableType = new org.alfresco.rest.api.tests.client.data.Type();
        publishableType.setId("test:publishable");
        publishableType.setParentId("mycompany:doc");
        publishableType.setIsContainer(false);
        publishableType.setIsArchive(true);
        publishableType.setIncludedInSupertypeQuery(true);

        Model testModel = new Model();
        testModel.setAuthor("Administrator");
        testModel.setId("api:apiModel");
        testModel.setNamespaceUri("http://www.api.t2/model/1.0");
        testModel.setNamespacePrefix("test2");

        Model apiModel = new Model();
        apiModel.setAuthor("Administrator");
        apiModel.setId("api:apiModel");
        apiModel.setNamespaceUri("http://www.api.t1/model/1.0");
        apiModel.setNamespacePrefix("api");

        AssociationSource testAllAspectSource =  new AssociationSource(null, "test2:aspect-all", true, true, null);
        AssociationSource testAllAspectTarget =  new AssociationSource(null, "api:referenceable", false, false, false);
        Association testAllAspectAssociation = new Association("api:assoc-all", null, null, null, false, testAllAspectSource, testAllAspectTarget);
        testAllAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        testAllAspect.setId("test2:aspect-all");
        testAllAspect.setTitle("Aspect derived from other namespace");
        testAllAspect.setIsArchive(false);
        testAllAspect.setIncludedInSupertypeQuery(false);
        testAllAspect.setIsContainer(false);
        testAllAspect.setModel(testModel);
        testAllAspect.setAssociations(Collections.singletonList(testAllAspectAssociation));
        testAllAspect.setMandatoryAspects(Arrays.asList("test2:aspect-three", "api:aspect-one", "api:aspect-two"));

        AssociationSource apiBaseSource =  new AssociationSource(null, "api:base", false, true, null);
        AssociationSource apiBaseTarget =  new AssociationSource(null, "api:base", true, false, false);
        Association apiBaseAssociation = new Association("api:assoc1", null, null, false, false, apiBaseSource, apiBaseTarget);

        AssociationSource apiChildSource =  new AssociationSource(null, "api:base", true, true, null);
        AssociationSource apiChildTarget =  new AssociationSource(null, "api:referenceable", false, false, false);
        Association apiChildAssociation = new Association("api:childassoc1", null, null, true, false, apiChildSource, apiChildTarget);

        AssociationSource apiBaseSource2 =  new AssociationSource(null, "api:base", true, true, null);
        AssociationSource apiBaseTarget2 =  new AssociationSource(null, "api:referenceable", false, false, false);
        Association apiBaseAssociation2 = new Association("api:assoc2", null, null, false, false, apiBaseSource2, apiBaseTarget2);

        AssociationSource apiChildPropagateSource =  new AssociationSource(null, "api:base", true, true, null);
        AssociationSource apiChildPropagateTarget =  new AssociationSource(null, "api:referenceable", false, false, false);
        Association apiChildPropagateAssociation = new Association("api:childassocPropagate", null, null, true, false, apiChildPropagateSource, apiChildPropagateTarget);

        apiBaseType = new org.alfresco.rest.api.tests.client.data.Type();
        apiBaseType.setId("api:base");
        apiBaseType.setTitle("Base");
        apiBaseType.setDescription("The Base Type");
        apiBaseType.setIncludedInSupertypeQuery(true);
        apiBaseType.setIsContainer(true);
        apiBaseType.setModel(apiModel);
        apiBaseType.setAssociations(Arrays.asList(apiBaseAssociation, apiChildAssociation, apiBaseAssociation2, apiChildPropagateAssociation));
        apiBaseType.setMandatoryAspects(Collections.singletonList("api:referenceable"));

        apiForcedType = new org.alfresco.rest.api.tests.client.data.Type();
        apiForcedType.setId("api:enforced");
        apiForcedType.setParentId("api:base");
        apiForcedType.setIncludedInSupertypeQuery(true);
        apiForcedType.setIsContainer(true);
        apiForcedType.setModel(apiModel);
        apiForcedType.setAssociations(Arrays.asList(apiBaseAssociation2, apiChildPropagateAssociation, apiBaseAssociation, apiChildAssociation));
        apiForcedType.setMandatoryAspects(Collections.singletonList("api:referenceable"));

        AssociationSource apiChildSource2 =  new AssociationSource(null, "api:file", false, true, null);
        AssociationSource apiChildTarget2 =  new AssociationSource(null, "api:referenceable", true, false, false);
        Association apiChildAssociation2 = new Association("api:childassoc2", null, null, true, false, apiChildSource2, apiChildTarget2);
        apiFileType = new org.alfresco.rest.api.tests.client.data.Type();
        apiFileType.setId("api:file");
        apiFileType.setParentId("api:base");
        apiFileType.setIsArchive(true);
        apiFileType.setIncludedInSupertypeQuery(true);
        apiFileType.setIsContainer(true);
        apiFileType.setModel(apiModel);
        apiFileType.setAssociations(Arrays.asList(apiBaseAssociation2, apiChildAssociation2, apiChildPropagateAssociation, apiBaseAssociation, apiChildAssociation));
        apiFileType.setMandatoryAspects(Collections.singletonList("api:referenceable"));

        apiFileDerivedType = new org.alfresco.rest.api.tests.client.data.Type();
        apiFileDerivedType.setId("api:file-derived");
        apiFileDerivedType.setParentId("api:file");
        apiFileDerivedType.setIsArchive(true);
        apiFileDerivedType.setIncludedInSupertypeQuery(true);
        apiFileDerivedType.setIsContainer(true);
        apiFileDerivedType.setModel(apiModel);
        apiFileDerivedType.setAssociations(Arrays.asList(apiBaseAssociation2, apiChildAssociation2, apiChildPropagateAssociation, apiBaseAssociation, apiChildAssociation));
        apiFileDerivedType.setMandatoryAspects(Collections.singletonList("api:referenceable"));

        apiFileDerivedNoArchiveType = new org.alfresco.rest.api.tests.client.data.Type();
        apiFileDerivedNoArchiveType.setId("api:file-derived-no-archive");
        apiFileDerivedNoArchiveType.setParentId("api:file");
        apiFileDerivedNoArchiveType.setIsArchive(false);
        apiFileDerivedNoArchiveType.setIncludedInSupertypeQuery(true);
        apiFileDerivedNoArchiveType.setIsContainer(true);
        apiFileDerivedNoArchiveType.setModel(apiModel);
        apiFileDerivedNoArchiveType.setAssociations(Arrays.asList(apiBaseAssociation2, apiChildAssociation2, apiChildPropagateAssociation, apiBaseAssociation, apiChildAssociation));
        apiFileDerivedNoArchiveType.setMandatoryAspects(Collections.singletonList("api:referenceable"));

        apiFolderType = new org.alfresco.rest.api.tests.client.data.Type();
        apiFolderType.setId("api:folder");
        apiFolderType.setParentId("api:base");
        apiFolderType.setIncludedInSupertypeQuery(true);
        apiFolderType.setIsContainer(true);
        apiFolderType.setModel(apiModel);
        apiFolderType.setAssociations(Arrays.asList(apiBaseAssociation2, apiChildPropagateAssociation, apiBaseAssociation, apiChildAssociation));
        apiFolderType.setMandatoryAspects(Collections.singletonList("api:referenceable"));

        apiOverrideType = new org.alfresco.rest.api.tests.client.data.Type();
        apiOverrideType.setId("api:overridetype1");
        apiOverrideType.setParentId("api:base");
        apiOverrideType.setIncludedInSupertypeQuery(true);
        apiOverrideType.setIsContainer(false);
        apiOverrideType.setModel(apiModel);
        apiOverrideType.setAssociations(Collections.emptyList());
        apiOverrideType.setMandatoryAspects(Collections.emptyList());

        apiOverride2Type = new org.alfresco.rest.api.tests.client.data.Type();
        apiOverride2Type.setId("api:overridetype2");
        apiOverride2Type.setParentId("api:overridetype1");
        apiOverride2Type.setIncludedInSupertypeQuery(true);
        apiOverride2Type.setIsContainer(false);
        apiOverride2Type.setModel(apiModel);
        apiOverride2Type.setAssociations(Collections.emptyList());
        apiOverride2Type.setMandatoryAspects(Collections.emptyList());

        apiOverride3Type = new org.alfresco.rest.api.tests.client.data.Type();
        apiOverride3Type.setId("api:overridetype3");
        apiOverride3Type.setParentId("api:overridetype2");
        apiOverride3Type.setIncludedInSupertypeQuery(true);
        apiOverride3Type.setIsContainer(false);
        apiOverride3Type.setModel(apiModel);
        apiOverride3Type.setAssociations(Collections.emptyList());
        apiOverride3Type.setMandatoryAspects(Collections.emptyList());

        apiNamedPropConstraintType = new org.alfresco.rest.api.tests.client.data.Type();
        apiNamedPropConstraintType.setId("api:typeWithNamedPropConstraint");
        apiNamedPropConstraintType.setTitle("Type with named property-defined constraint.");
        apiNamedPropConstraintType.setDescription("A type with a named constraint defined within one of its properties.");
        apiNamedPropConstraintType.setParentId("api:overridetype2");
        apiNamedPropConstraintType.setIncludedInSupertypeQuery(true);
        apiNamedPropConstraintType.setIsContainer(false);
        apiNamedPropConstraintType.setModel(apiModel);
        apiNamedPropConstraintType.setAssociations(Collections.emptyList());
        apiNamedPropConstraintType.setMandatoryAspects(Collections.emptyList());

        allTypes = ImmutableList.of(apiBaseType, apiForcedType, apiFileType, apiFileDerivedType,
                apiFileDerivedNoArchiveType, apiFolderType, apiOverrideType, apiOverride2Type,
                apiOverride3Type, apiNamedPropConstraintType);
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
