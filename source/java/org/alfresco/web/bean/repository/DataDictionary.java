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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.bean.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Lighweight client side representation of the repository data dictionary. 
 * This allows service calls to be kept to a minimum and for bean access, thus enabling JSF
 * value binding expressions.
 * 
 * @author gavinc
 */
public final class DataDictionary
{
   private DictionaryService dictionaryService;
   private Map<QName, TypeDefinition> types = new HashMap<QName, TypeDefinition>(11, 1.0f);

   /**
    * Constructor
    * 
    * @param dictionaryService The dictionary service to use to retrieve the data 
    */
   public DataDictionary(DictionaryService dictionaryService)
   {
      this.dictionaryService = dictionaryService;
   }
   
   /**
    * Returns the type definition for the type represented by the given qname
    * 
    * @param type The qname of the type to lookup the definition for
    * @return The type definition for the requested type
    */
   public TypeDefinition getTypeDef(QName type)
   {
      TypeDefinition typeDef = types.get(type);
      
      if (typeDef == null)
      {
         typeDef = this.dictionaryService.getType(type);
         
         if (typeDef != null)
         {
            types.put(type, typeDef);
         }
      }
      
      return typeDef;
   }
   
   /**
    * Returns the type definition for the type represented by the given qname
    * and for all the given aspects
    * 
    * @param type The type to retrieve the definition for 
    * @param optionalAspects A list of aspects to retrieve the definition for 
    * @return A unified type definition of the given type and aspects 
    */
   public TypeDefinition getTypeDef(QName type, Collection<QName> optionalAspects)
   {
      return this.dictionaryService.getAnonymousType(type, optionalAspects);
   }
   
   /**
    * Returns the property definition for the given property on the given node 
    * 
    * @param node The node from which to get the property
    * @param property The property to find the definition for
    * @return The property definition or null if the property is not known
    */
   public PropertyDefinition getPropertyDefinition(Node node, String property)
   {
      PropertyDefinition propDef = null;
      
      TypeDefinition typeDef = getTypeDef(node.getType(), node.getAspects());
      
      if (typeDef != null)
      {
         Map<QName, PropertyDefinition> properties = typeDef.getProperties();
         propDef = properties.get(Repository.resolveToQName(property));
      }
      
      return propDef;
   }
   
   /**
    * Returns the association definition for the given association on the given node
    * 
    * @param node The node from which to get the association
    * @param association The association to find the definition for
    * @return The association definition or null if the association is not known
    */
   public AssociationDefinition getAssociationDefinition(Node node, String association)
   {
      AssociationDefinition assocDef = null;
      
      TypeDefinition typeDef = getTypeDef(node.getType(), node.getAspects());
      
      if (typeDef != null)
      {
         Map<QName, AssociationDefinition> assocs = typeDef.getAssociations();
         assocDef = assocs.get(Repository.resolveToQName(association));
      }
      
      return assocDef;
   }
}
