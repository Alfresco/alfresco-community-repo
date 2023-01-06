/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.model.builder;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestNodeBodyModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.RepoTestModel;

/**
 * Builder for creating nested nodes using REST v1
 * No assertion is made in order to use this builder also for negative testing
 * 
 * @author Paul Brodner
 */
public class NodesBuilder
{
    private List<NodeDetail> nodes = new LinkedList<>();
    private RestWrapper restWrapper;
    private RepoTestModel lastNode;

    public NodeDetail getNode(String prefix)
    {
        for(NodeDetail node : nodes)
        {
            if(node.getPrefix().equals(prefix))
                return node;
        }
        return null;
    }
    /**
     * How you can use to create nested nodes:
     * will create in Admin's Repository:
     * - F1-P-randomname>
     * - F1-P-randomname
     * - F3-P-randomname
     * ------file1-randomname
     * ------file2-randomname
     * ------F3-randomname
     * <code>
     *       restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI()
                            .usingNode(ContentModel.my())
                                    .createHierarcy()
                                        .folder("F1-P")
                                        .folder("F2-P")
                                        .folder("F3-P")
                                            .file("file1")
                                            .file("file2")
                                            .folder("F4")                                            
     * </code>
     * 
     * @param restWrapper
     * @param repoModel
     */
    public NodesBuilder(RestWrapper restWrapper, RepoTestModel repoModel)
    {
        this.restWrapper = restWrapper;
        this.lastNode = repoModel;
    }

    public NodeDetail folder(String prefix)
    {
        NodeDetail n = new NodeDetail(prefix, lastNode.getNodeRef(), "cm:folder");
        nodes.add(n);
        return n;
    }

    public class NodeDetail
    {
        private RestNodeModel parentNodeModel;
        private String id;
        private String prefix;
        private String name;
        
        public ContentModel toContentModel()
        {            
            ContentModel cm = new ContentModel();
            cm.setNodeRef(getId());
            return cm;            
        }
        public NodeDetail(String prefix, String parentId, String nodeType)
        {
            this.prefix = prefix;
            this.name = RandomData.getRandomName(prefix);
            
            RestNodeBodyModel model = new RestNodeBodyModel();            
            model.setName(name);
            model.setNodeType(nodeType);

            // define the parent
            ContentModel parent = new ContentModel();
            parent.setNodeRef(parentId);

            RestNodeModel newModel = restWrapper.withCoreAPI().usingNode(parent).createNode(model);
            this.id = newModel.getId();
            
            if (nodeType.equals("cm:content"))
            {
                RestNodeModel tmp = new RestNodeModel();
                tmp.setId(parentId);
                parentNodeModel = tmp;
            }
            else
                parentNodeModel = newModel;

        }

        public NodeDetail folder(String prefix)
        {
            NodeDetail n = new NodeDetail(prefix, parentNodeModel.getId(), "cm:folder");
            nodes.add(n);
            return n;
        }

        public NodeDetail file(String prefix)
        {
            NodeDetail n = new NodeDetail(prefix, parentNodeModel.getId(), "cm:content");
            nodes.add(n);
            return n;
        }

        public String getId()
        {
            return id;
        }
        
        public String getName()
        {
            return name;
        }
        
        public String getPrefix()
        {
            return prefix;
        }
    }
}
