/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.publishing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.cmr.publishing.NodeSnapshot;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingPackageSerializerTest extends AbstractPublishingIntegrationTest
{
    @Resource(name="publishingPackageSerializer")
    private StandardNodeSnapshotSerializer serializer;

    private TransferManifestNormalNode normalNode1;

    /**
     * @throws java.lang.Exception
     */
    @Before
    @Override
    public void onSetUp() throws Exception
    {
        super.onSetUp();
        serializer = (StandardNodeSnapshotSerializer) getApplicationContext().getBean("publishingPackageSerializer");
        normalNode1 = new TransferManifestNormalNode();
        normalNode1.setAccessControl(null);

        Set<QName> aspects = new HashSet<QName>();
        aspects.add(ContentModel.ASPECT_AUDITABLE);
        aspects.add(ContentModel.ASPECT_TITLED);
        normalNode1.setAspects(aspects);

        List<ChildAssociationRef> childAssocs = new ArrayList<ChildAssociationRef>();
        normalNode1.setChildAssocs(childAssocs);

        String guid = GUID.generate();
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, guid);
        normalNode1.setNodeRef(nodeRef);

        ChildAssociationRef primaryParentAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, new NodeRef(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "MY_PARENT_NODEREF"), QName.createQName(
                        NamespaceService.CONTENT_MODEL_1_0_URI, "localname"), nodeRef, true, -1);
        List<ChildAssociationRef> parentAssocs = new ArrayList<ChildAssociationRef>();
        parentAssocs.add(primaryParentAssoc);
        normalNode1.setParentAssocs(parentAssocs);
        
        Path path = new Path();
        path.append(new Path.ChildAssocElement(primaryParentAssoc));
        normalNode1.setParentPath(path);
        
        normalNode1.setPrimaryParentAssoc(primaryParentAssoc);
        
        Map<QName,Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, guid);
        normalNode1.setProperties(props);
        
        List<AssociationRef> sourceAssocs = new ArrayList<AssociationRef>();
        sourceAssocs.add(new AssociationRef(nodeRef, ContentModel.ASSOC_ATTACHMENTS, nodeRef));
        sourceAssocs.add(new AssociationRef(nodeRef, ContentModel.ASSOC_REFERENCES, nodeRef));
        normalNode1.setSourceAssocs(sourceAssocs);

        List<AssociationRef> targetAssocs = new ArrayList<AssociationRef>();
        targetAssocs.add(new AssociationRef(nodeRef, ContentModel.ASSOC_ATTACHMENTS, nodeRef));
        targetAssocs.add(new AssociationRef(nodeRef, ContentModel.ASSOC_REFERENCES, nodeRef));
        normalNode1.setTargetAssocs(targetAssocs);
        
        normalNode1.setType(ContentModel.TYPE_CONTENT);
        normalNode1.setAncestorType(ContentModel.TYPE_CONTENT);
        normalNode1.setUuid(guid);
    }

    @Test
    public void testSerializer() throws Exception
    {
        NodeSnapshotTransferImpl transferSnapshot = new NodeSnapshotTransferImpl(normalNode1);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        serializer.serialize(Collections.<NodeSnapshot>singleton(transferSnapshot), os);
        os.close();
        
        byte[] output = os.toByteArray();
        
        ByteArrayInputStream is = new ByteArrayInputStream(output);
        List<NodeSnapshot> snapshots = serializer.deserialize(is);
        assertEquals(1, snapshots.size());
        NodeSnapshot snapshot = snapshots.get(0);
        assertEquals(normalNode1.getNodeRef(), snapshot.getNodeRef());
        assertEquals(normalNode1.getType(), snapshot.getType());
        assertEquals(normalNode1.getAspects(), snapshot.getAspects());
        assertEquals(normalNode1.getProperties(), snapshot.getProperties());
    }
}
