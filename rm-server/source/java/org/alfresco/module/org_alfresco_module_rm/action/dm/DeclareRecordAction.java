package org.alfresco.module.org_alfresco_module_rm.action.dm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

public class DeclareRecordAction extends ActionExecuterAbstractBase implements RecordsManagementModel
{
   /** I18N */
   private static final String MSG_UNDECLARED_ONLY_RECORDS = "rm.action.undeclared-only-records";
   private static final String MSG_NO_DECLARE_MAND_PROP = "rm.action.no-declare-mand-prop";

   /** Logger */
   private static Log logger = LogFactory.getLog(DeclareRecordAction.class);

   /** Record service */
   private RecordService recordService;

   /** Record management service */
   private RecordsManagementService recordsManagementService;

   /** Node service */
   private NodeService nodeService;

   /** Ownable service **/
   private OwnableService ownableService;

   /** Dictionary service */
   private DictionaryService dictionaryService;

   /**
    * @param recordService record service
    */
   public void setRecordService(RecordService recordService)
   {
      this.recordService = recordService;
   }

   /**
    * @param recordsManagementService records management service
    */
   public void setRecordsManagementService(
         RecordsManagementService recordsManagementService)
   {
      this.recordsManagementService = recordsManagementService;
   }

   /**
    * @param nodeService node service
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param ownableSerice ownable serice 
    */
   public void setOwnableService(OwnableService ownableService)
   {
      this.ownableService = ownableService;
   }

   /**
    * @param dictionaryService dictionary service
    */
   public void setDictionaryService(DictionaryService dictionaryService)
   {
      this.dictionaryService = dictionaryService;
   }

   @Override
   protected void executeImpl(Action action, final NodeRef actionedUponNodeRef)
   {
      if (recordsManagementService.isRecord(actionedUponNodeRef) == true)
      {
         if (recordService.isDeclared(actionedUponNodeRef) == false)
         {
            List<String> missingProperties = new ArrayList<String>(5);
            // Aspect not already defined - check mandatory properties then add
            if (mandatoryPropertiesSet(actionedUponNodeRef, missingProperties) == true)
            {
               // Add the declared aspect
               Map<QName, Serializable> declaredProps = new HashMap<QName, Serializable>(2);
               declaredProps.put(PROP_DECLARED_AT, new Date());
               declaredProps.put(PROP_DECLARED_BY, AuthenticationUtil.getRunAsUser());
               nodeService.addAspect(actionedUponNodeRef, ASPECT_DECLARED_RECORD, declaredProps);

               // remove all owner related rights 
               ownableService.setOwner(actionedUponNodeRef, OwnableService.NO_OWNER);
            }
            else
            {
               throw new AlfrescoRuntimeException(buildMissingPropertiesErrorString(missingProperties));
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
    * @param nodeRef
    *            node reference
    * @return boolean true if all mandatory properties are set, false otherwise
    */
   private boolean mandatoryPropertiesSet(NodeRef nodeRef, List<String> missingProperties)
   {
       boolean result = true;

       Map<QName, Serializable> nodeRefProps = nodeService.getProperties(nodeRef);

       QName nodeRefType = nodeService.getType(nodeRef);

       TypeDefinition typeDef = dictionaryService.getType(nodeRefType);
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
               AspectDefinition aspectDef = dictionaryService.getAspect(aspect);
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

   @Override
   protected void addParameterDefinitions(List<ParameterDefinition> paramList)
   {
      // No parameters
   }

}