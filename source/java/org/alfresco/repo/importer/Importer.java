/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
     * Resolve path within context of root reference
     * 
     * @param path  the path to resolve
     * @return  node reference
     */
    public NodeRef resolvePath(String path);
    
    /**
     * Is excluded Content Model Class?
     * 
     * @param  QName  the class name to test
     * @return  true => the provided class is excluded from import
     */
    public boolean isExcludedClass(QName className);
    
    /**
     * Signal completion of node import
     * 
     * @param nodeRef  the node ref of the imported node
     */
    public void childrenImported(NodeRef nodeRef);
}
