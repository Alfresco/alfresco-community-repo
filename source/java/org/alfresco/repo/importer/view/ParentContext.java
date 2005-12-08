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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.importer.ImportParent;
import org.alfresco.repo.importer.Importer;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
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
    public ParentContext(QName elementName, NodeContext parent, ChildAssociationDefinition childDef)
    {
        this(elementName, parent);
        
        // Ensure association is valid for node
        Set<QName> allAspects = new HashSet<QName>();
        for (AspectDefinition typeAspect : parent.getTypeDefinition().getDefaultAspects())
        {
            allAspects.add(typeAspect.getName());
        }
        allAspects.addAll(parent.getNodeAspects());
        TypeDefinition anonymousType = getDictionaryService().getAnonymousType(parent.getTypeDefinition().getName(), allAspects);
        Map<QName, ChildAssociationDefinition> nodeAssociations = anonymousType.getChildAssociations();
        if (nodeAssociations.containsKey(childDef.getName()) == false)
        {
            throw new ImporterException("Association " + childDef.getName() + " is not valid for node " + parent.getTypeDefinition().getName());
        }
        
        parentRef = parent.getNodeRef();
        assocType = childDef.getName();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportParent#getParentRef()
     */
    public NodeRef getParentRef()
    {
        return parentRef;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportParent#getAssocType()
     */
    public QName getAssocType()
    {
        return assocType;
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
