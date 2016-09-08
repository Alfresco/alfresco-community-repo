/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Declare record action
 * 
 * @author Roy Wetherall
 */
public class DeclareRecordAction extends RMActionExecuterAbstractBase
{
   /** I18N */
   private static final String MSG_UNDECLARED_ONLY_RECORDS = "rm.action.undeclared-only-records";
   private static final String MSG_NO_DECLARE_MAND_PROP = "rm.action.no-declare-mand-prop";

   /** Logger */
   private static Log logger = LogFactory.getLog(DeclareRecordAction.class);

   /**
    * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
    */
   @Override
   protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
   {
      if (recordsManagementService.isRecord(actionedUponNodeRef) == true)
      {
         if (recordsManagementService.isRecordDeclared(actionedUponNodeRef) == false)
         {
            List<String> missingProperties = new ArrayList<String>(5);
            // Aspect not already defined - check mandatory properties then add
            if (mandatoryPropertiesSet(actionedUponNodeRef, missingProperties) == true)
            {
               // Add the declared aspect
               Map<QName, Serializable> declaredProps = new HashMap<QName, Serializable>(2);
               declaredProps.put(PROP_DECLARED_AT, new Date());
               declaredProps.put(PROP_DECLARED_BY, AuthenticationUtil.getRunAsUser());
               this.nodeService.addAspect(actionedUponNodeRef, ASPECT_DECLARED_RECORD, declaredProps);

               // remove all owner related rights 
               this.ownableService.setOwner(actionedUponNodeRef, OwnableService.NO_OWNER);
            }
            else
            {
               logger.debug(buildMissingPropertiesErrorString(missingProperties));
               action.setParameterValue(ActionExecuterAbstractBase.PARAM_RESULT, "missingProperties");
            }
         }
      }
      else
      {
         throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_UNDECLARED_ONLY_RECORDS, actionedUponNodeRef.toString()));
      }
   }

   private String buildMissingPropertiesErrorString(List<String> missingProperties)
   {
      StringBuilder builder = new StringBuilder(255);
      builder.append(I18NUtil.getMessage(MSG_NO_DECLARE_MAND_PROP));
      builder.append("  ");
      for (String missingProperty : missingProperties)            
      {
         builder.append(missingProperty)
            .append(", ");
      }
      return builder.toString();
    }

   /**
    * Helper method to check whether all the mandatory properties of the node have been set
    * 
    * @param nodeRef node reference
    * @return boolean true if all mandatory properties are set, false otherwise
    */
   private boolean mandatoryPropertiesSet(NodeRef nodeRef, List<String> missingProperties)
   {
      boolean result = true;

      Map<QName, Serializable> nodeRefProps = this.nodeService.getProperties(nodeRef);

      QName nodeRefType = this.nodeService.getType(nodeRef);

      TypeDefinition typeDef = this.dictionaryService.getType(nodeRefType);
      for (PropertyDefinition propDef : typeDef.getProperties().values())
      {
         if (propDef.isMandatory() == true)
         {
            if (nodeRefProps.get(propDef.getName()) == null)
            {
               logMissingProperty(propDef, missingProperties);

               result = false;
               break;
            }
         }
      }

      if (result != false)
      {
         Set<QName> aspects = this.nodeService.getAspects(nodeRef);
         for (QName aspect : aspects)
         {
            AspectDefinition aspectDef = this.dictionaryService.getAspect(aspect);
            for (PropertyDefinition propDef : aspectDef.getProperties().values())
            {
               if (propDef.isMandatory() == true)
               {
                  if (nodeRefProps.get(propDef.getName()) == null)
                  {
                     logMissingProperty(propDef, missingProperties);

                     result = false;
                     break;
                  }
               }
            }
         }
      }

      return result;
   }

   /**
    * Log information about missing properties.
    * 
    * @param propDef               property definition
    * @param missingProperties     missing properties
    */
   private void logMissingProperty(PropertyDefinition propDef, List<String> missingProperties)
   {
      if (logger.isWarnEnabled())
      {
         StringBuilder msg = new StringBuilder();
         msg.append("Mandatory property missing: ").append(propDef.getName());
         logger.warn(msg.toString());
      }
      missingProperties.add(propDef.getName().toString());
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#getProtectedAspects()
    */
   @Override
   public Set<QName> getProtectedAspects()
   {
      HashSet<QName> qnames = new HashSet<QName>();
      qnames.add(ASPECT_DECLARED_RECORD);
      return qnames;
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#isExecutableImpl(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, boolean)
    */
   @Override
   protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
   {
      if (recordsManagementService.isRecord(filePlanComponent) == true)
      {
         if (recordsManagementService.isRecordDeclared(filePlanComponent) == false)
         {
            // Aspect not already defined - check mandatory properties then add
            List<String> missingProperties = new ArrayList<String>(10);
            if (mandatoryPropertiesSet(filePlanComponent, missingProperties) == true)
            {
               return true;
            }
         }
         return false;
      }
      else
      {
         if (throwException)
         {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_UNDECLARED_ONLY_RECORDS, filePlanComponent.toString()));
         }
         else
         {
            return false;
         }
      }
   }

}
