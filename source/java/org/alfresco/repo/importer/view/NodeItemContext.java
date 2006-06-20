/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.importer.view;

import org.alfresco.service.namespace.QName;


/**
 * Represents Property Context
 * 
 * @author David Caruana
 *
 */
public class NodeItemContext extends ElementContext
{
    private NodeContext nodeContext;
    
    /**
     * Construct
     * 
     * @param elementName
     * @param dictionary
     * @param importer
     */
    public NodeItemContext(QName elementName, NodeContext nodeContext)
    {
        super(elementName, nodeContext.getDictionaryService(), nodeContext.getImporter());
        this.nodeContext = nodeContext;
    }
    
    /**
     * Gets the Node Context
     */
    public NodeContext getNodeContext()
    {
        return nodeContext;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "NodeItemContext[nodeContext=" + nodeContext.toString() + "]";
    }
 

}
