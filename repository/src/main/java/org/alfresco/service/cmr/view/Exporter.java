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
package org.alfresco.service.cmr.view;

import java.io.InputStream;
import java.util.Locale;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.namespace.QName;

/**
 * Contract for an exporter. An exporter is responsible for actually exporting the content of the Repository to a destination point e.g. file system.
 * 
 * @author David Caruana
 */
public interface Exporter
{
    /**
     * Start of Export
     */
    public void start(ExporterContext context);

    /**
     * Start export of namespace
     * 
     * @param prefix
     *            namespace prefix
     * @param uri
     *            namespace uri
     */
    public void startNamespace(String prefix, String uri);

    /**
     * End export of namespace
     * 
     * @param prefix
     *            namespace prefix
     */
    public void endNamespace(String prefix);

    /**
     * Start export of node
     * 
     * @param nodeRef
     *            the node reference
     */
    public void startNode(NodeRef nodeRef);

    /**
     * End export of node
     * 
     * @param nodeRef
     *            the node reference
     */
    public void endNode(NodeRef nodeRef);

    /**
     * Start export of node reference
     * 
     * @param nodeRef
     *            the node reference
     */
    public void startReference(NodeRef nodeRef, QName childName);

    /**
     * End export of node reference
     * 
     * @param nodeRef
     *            the node reference
     */
    public void endReference(NodeRef nodeRef);

    /**
     * Start export of aspects
     * 
     * @param nodeRef
     *            NodeRef
     */
    public void startAspects(NodeRef nodeRef);

    /**
     * Start export of aspect
     * 
     * @param nodeRef
     *            the node reference
     * @param aspect
     *            the aspect
     */
    public void startAspect(NodeRef nodeRef, QName aspect);

    /**
     * End export of aspect
     * 
     * @param nodeRef
     *            the node reference
     * @param aspect
     *            the aspect
     */
    public void endAspect(NodeRef nodeRef, QName aspect);

    /**
     * End export of aspects
     * 
     * @param nodeRef
     *            NodeRef
     */
    public void endAspects(NodeRef nodeRef);

    /**
     * Start export of ACL
     * 
     * @param nodeRef
     *            for node reference
     */
    public void startACL(NodeRef nodeRef);

    /**
     * Export permission
     * 
     * @param nodeRef
     *            for node reference
     * @param permission
     *            the permission
     */
    public void permission(NodeRef nodeRef, AccessPermission permission);

    /**
     * End export of ACL
     * 
     * @param nodeRef
     *            for node reference
     */
    public void endACL(NodeRef nodeRef);

    /**
     * Start export of properties
     * 
     * @param nodeRef
     *            the node reference
     */
    public void startProperties(NodeRef nodeRef);

    /**
     * Start export of property
     * 
     * @param nodeRef
     *            the node reference
     * @param property
     *            the property name
     */
    public void startProperty(NodeRef nodeRef, QName property);

    /**
     * End export of property
     * 
     * @param nodeRef
     *            the node reference
     * @param property
     *            the property name
     */
    public void endProperty(NodeRef nodeRef, QName property);

    /**
     * End export of properties
     * 
     * @param nodeRef
     *            the node reference
     */
    public void endProperties(NodeRef nodeRef);

    /**
     * Export start of value collection
     * 
     * @param nodeRef
     *            the node reference
     * @param property
     *            the property name
     */
    public void startValueCollection(NodeRef nodeRef, QName property);

    /**
     * Start export MLText
     * 
     * @param nodeRef
     *            the node reference
     * @param locale
     *            Locale
     * @param isNull
     *            boolean
     */
    public void startValueMLText(NodeRef nodeRef, Locale locale, boolean isNull);

    /**
     * End export MLText
     * 
     * @param nodeRef
     *            NodeRef
     */
    public void endValueMLText(NodeRef nodeRef);

    /**
     * Export property value
     * 
     * @param nodeRef
     *            the node reference
     * @param property
     *            the property name
     * @param value
     *            the value
     * @param index
     *            value index (or -1, if not part of multi-valued collection)
     */
    public void value(NodeRef nodeRef, QName property, Object value, int index);

    /**
     * Export content stream property value
     * 
     * @param nodeRef
     *            the node reference
     * @param property
     *            the property name
     * @param content
     *            the content stream
     * @param contentData
     *            content descriptor
     * @param index
     *            value index (or -1, if not part of multi-valued collection)
     */
    public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index);

    /**
     * Export end of value collection
     * 
     * @param nodeRef
     *            the node reference
     * @param property
     *            the property name
     */
    public void endValueCollection(NodeRef nodeRef, QName property);

    /**
     * Start export of associations
     * 
     * @param nodeRef
     *            NodeRef
     */
    public void startAssocs(NodeRef nodeRef);

    /**
     * Start export of association
     * 
     * @param nodeRef
     *            the node reference
     * @param assoc
     *            the association name
     */
    public void startAssoc(NodeRef nodeRef, QName assoc);

    /**
     * End export of association
     * 
     * @param nodeRef
     *            the node reference
     * @param assoc
     *            the association name
     */
    public void endAssoc(NodeRef nodeRef, QName assoc);

    /**
     * End export of associations
     * 
     * @param nodeRef
     *            NodeRef
     */
    public void endAssocs(NodeRef nodeRef);

    /**
     * Export warning
     * 
     * @param warning
     *            the warning message
     */
    public void warning(String warning);

    /**
     * End export
     */
    public void end();

}
