/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.importer;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;

/**
 * The Importer interface encapusulates the strategy for importing a node into the Repository.
 * 
 * @author David Caruana
 */
public interface Importer
{
    /**
     * @return the root node to import into
     */
    public NodeRef getRootRef();

    /**
     * @return the root child association type to import under
     */
    public QName getRootAssocType();

    /**
     * @return the location to import under
     */
    public Location getLocation();

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
     * @param node
     *            the node description
     * @return the node ref of the imported node
     */
    public NodeRef importNode(ImportNode node);

    /**
     * Resolve path within context of root reference
     * 
     * @param path
     *            the path to resolve
     * @return node reference
     */
    public NodeRef resolvePath(String path);

    /**
     * Is excluded Content Model Class?
     * 
     * @param className
     *            the class name to test
     * @return true => the provided class is excluded from import
     */
    public boolean isExcludedClass(QName className);

    /**
     * Signal completion of node import
     * 
     * @param nodeRef
     *            the node ref of the imported node
     */
    public void childrenImported(NodeRef nodeRef);
}
