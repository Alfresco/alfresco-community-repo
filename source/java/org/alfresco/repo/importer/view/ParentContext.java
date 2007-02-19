/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.importer.view;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.importer.ImportParent;
import org.alfresco.repo.importer.Importer;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.namespace.QName;


/**
 * Maintains state about the parent context of the node being imported.
 * 
 * @author David Caruana
 *
 */
public class ParentContext extends ElementContext
    implements ImportParent
{
    private NodeRef parentRef;
    private QName assocType;


    /**
     * Construct
     * 
     * @param dictionary
     * @param configuration
     * @param progress
     * @param elementName
     * @param parentRef
     * @param assocType
     */
    public ParentContext(QName elementName, DictionaryService dictionary, Importer importer)
    {
        super(elementName, dictionary, importer);
        parentRef = importer.getRootRef();
        assocType = importer.getRootAssocType();
    }
    
    /**
     * Construct (with unknown child association) 
     * 
     * @param elementName
     * @param parent
     */
    public ParentContext(QName elementName, NodeContext parent)
    {
        super(elementName, parent.getDictionaryService(), parent.getImporter());
        parentRef = parent.getNodeRef();
    }
    
    /**
     * Construct 
     * 
     * @param elementName
     * @param parent
     * @param childDef
     */
    public ParentContext(QName elementName, NodeContext parent, AssociationDefinition assocDef)
    {
        this(elementName, parent);
        
        TypeDefinition typeDef = parent.getTypeDefinition();
        if (typeDef != null)
        {
            //
            // Ensure association type is valid for node parent
            //
            
            // Build complete Type Definition
            Set<QName> allAspects = new HashSet<QName>();
            for (AspectDefinition typeAspect : parent.getTypeDefinition().getDefaultAspects())
            {
                allAspects.add(typeAspect.getName());
            }
            allAspects.addAll(parent.getNodeAspects());
            TypeDefinition anonymousType = getDictionaryService().getAnonymousType(parent.getTypeDefinition().getName(), allAspects);
            
            // Determine if Association is valid for Type Definition
            Map<QName, AssociationDefinition> nodeAssociations = anonymousType.getAssociations();
            if (nodeAssociations.containsKey(assocDef.getName()) == false)
            {
                throw new ImporterException("Association " + assocDef.getName() + " is not valid for node " + parent.getTypeDefinition().getName());
            }
        }
        
        parentRef = parent.getNodeRef();
        assocType = assocDef.getName();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportParent#getParentRef()
     */
    public NodeRef getParentRef()
    {
        return parentRef;
    }
    
    /**
     * Set Parent Reference
     * 
     * @param  parentRef  parent reference
     */
    public void setParentRef(NodeRef parentRef)
    {
        this.parentRef = parentRef;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportParent#getAssocType()
     */
    public QName getAssocType()
    {
        return assocType;
    }

    /**
     * Set Parent / Child Assoc Type
     *  
     * @param assocType  association type
     */
    public void setAssocType(QName assocType)
    {
        this.assocType = assocType;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ParentContext[parent=" + parentRef + ",assocType=" + getAssocType() + "]";
    }
    
}
