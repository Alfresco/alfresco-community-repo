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
     * @param elementName QName
     * @param dictionary DictionaryService
     * @param importer Importer
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
     * @param elementName QName
     * @param parent NodeContext
     */
    public ParentContext(QName elementName, NodeContext parent)
    {
        super(elementName, parent.getDictionaryService(), parent.getImporter());
        parentRef = parent.getNodeRef();
    }
    
    /**
     * Construct 
     * 
     * @param elementName QName
     * @param parent NodeContext
     * @param assocDef AssociationDefinition
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
