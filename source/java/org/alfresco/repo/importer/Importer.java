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
package org.alfresco.repo.importer;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * The Importer interface encapusulates the strategy for importing 
 * a node into the Repository. 
 * 
 * @author David Caruana
 */
public interface Importer
{
    /**
     * @return  the root node to import into
     */
    public NodeRef getRootRef();
    
    /**
     * @return  the root child association type to import under
     */
    public QName getRootAssocType();

    /**
     * Signal start of import
     */
    public void start();

    /**
     * Signal end of import
     */
    public void end();

    /**
     * Signal import error
     */
    public void error(Throwable e);
    
    /**
     * Import meta-data
     */
    public void importMetaData(Map<QName, String> properties);
    
    /**
     * Import a node
     * 
     * @param node  the node description
     * @return  the node ref of the imported node
     */
    public NodeRef importNode(ImportNode node);

    /**
     * Signal completion of node import
     * 
     * @param nodeRef  the node ref of the imported node
     */
    public void childrenImported(NodeRef nodeRef);
}
