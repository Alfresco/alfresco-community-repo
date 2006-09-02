package org.alfresco.web.bean.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a transient node i.e. it is not and will not be present in the repository.
 * <p>
 * This type of node is typically used to drive the property sheet when data collection
 * is required for a type but the node does not need to be stored in the repository. An
 * example use is the workflow, transient nodes are used to collect workitem metadata.
 * </p>
 * 
 * @author gavinc
 */
public class TransientNode extends Node
{
   private static final long serialVersionUID = 2140554155948154106L;
   
   private static final Log logger = LogFactory.getLog(TransientNode.class);

   /**
    * Constructor.
    * <p>
    * NOTE: The name is NOT automatically added to the map of properties,
    * if you need the name of this node to be in the map then add it to
    * the map passed in to this constructor.
    * </p>
    * 
    * @param type The type this node will represent
    * @param name The name of the node
    * @param data The properties and associations this node will have
    */
   public TransientNode(QName type, String name, Map<QName, Serializable> data)
   {
      // create a dummy NodeRef to pass to the constructor
      super(new NodeRef(Repository.getStoreRef(), GUID.generate()));
      
      this.type = type;
      this.name = name;
      
      // initialise the node
      initNode(data);
   }

   /**
    * Initialises the node.
    *
    * @param data The properties and associations to initialise the node with
    */
   protected void initNode(Map<QName, Serializable> data)
   {
      // setup the transient node so that the super class methods work
      // and do not need to go back to the repository
      
      DictionaryService ddService = this.getServiceRegistry().getDictionaryService();
      
      // marshall the given properties and associations into the internal maps
      this.associations = new QNameNodeMap(getServiceRegistry().getNamespaceService(), this);
      this.childAssociations = new QNameNodeMap(getServiceRegistry().getNamespaceService(), this);

      if (data != null)
      {
         // go through all data items and allocate to the correct internal list
         for (QName item : data.keySet())
         {
            PropertyDefinition propDef = ddService.getProperty(item);
            if (propDef != null)
            {
               this.properties.put(item, data.get(item));
            }
            else
            {
               // see if the item is either type of association
               AssociationDefinition assocDef = ddService.getAssociation(item);
               if (assocDef != null)
               {
                  if (assocDef.isChild())
                  {
                     // TODO: handle lists of NodeRef's
                     NodeRef child = null;
                     Object obj = data.get(item);
                     if (obj instanceof String)
                     {
                        child = new NodeRef((String)obj);
                     }
                     else if (obj instanceof NodeRef)
                     {
                        child = (NodeRef)obj;
                     }
                     else if (obj instanceof List)
                     {
                        if (logger.isWarnEnabled())
                           logger.warn("0..* child associations are not supported yet");
                     }
                     
                     if (child != null)
                     {
                        // create a child association reference, add it to a list and add the list
                        // to the list of child associations for this node
                        List<ChildAssociationRef> assocs = new ArrayList<ChildAssociationRef>(1);
                        ChildAssociationRef childRef = new ChildAssociationRef(assocDef.getName(), this.nodeRef,
                              null, child);
                        assocs.add(childRef);
                        
                        this.childAssociations.put(item, assocs);
                     }
                  }
                  else
                  {
                     // TODO: handle lists of NodeRef's
                     NodeRef target = null;
                     Object obj = data.get(item);
                     if (obj instanceof String)
                     {
                        target = new NodeRef((String)obj);
                     }
                     else if (obj instanceof NodeRef)
                     {
                        target = (NodeRef)obj;
                     }
                     else if (obj instanceof List)
                     {
                        if (logger.isWarnEnabled())
                           logger.warn("0..* associations are not supported yet");
                     }
                     
                     if (target != null)
                     {
                        // create a association reference, add it to a list and add the list
                        // to the list of associations for this node
                        List<AssociationRef> assocs = new ArrayList<AssociationRef>(1);
                        AssociationRef assocRef = new AssociationRef(this.nodeRef, assocDef.getName(), target);
                        assocs.add(assocRef);
                        
                        this.associations.put(item, assocs);
                     }
                  }
               }
            }
         }
      }
  
      // show that the maps have been initialised
      this.propsRetrieved = true;
      this.assocsRetrieved = true;
      this.childAssocsRetrieved = true;
      
      // setup the list of aspects the node would have
      TypeDefinition typeDef = ddService.getType(this.type);
      if (typeDef == null)
      {
         throw new AlfrescoRuntimeException("Failed to find type definition for start task: " + this.type);
      }
      
      this.aspects = new HashSet<QName>();
      for (AspectDefinition aspectDef : typeDef.getDefaultAspects())
      {
         this.aspects.add(aspectDef.getName());
      }
      
      // setup remaining variables
      this.path = "";
      this.locked = Boolean.FALSE;
      this.workingCopyOwner = Boolean.FALSE;
   }
   
   @Override
   public boolean hasPermission(String permission)
   {
      return true;
   }

   @Override
   public void reset()
   {
      // don't reset anything otherwise we'll lose our data
      // with no way of getting it back!!
   }
   
   @Override
   public String toString()
   {
      return "Transient node of type: " + getType() + 
             "\nProperties: " + this.getProperties().toString();
   }
}
