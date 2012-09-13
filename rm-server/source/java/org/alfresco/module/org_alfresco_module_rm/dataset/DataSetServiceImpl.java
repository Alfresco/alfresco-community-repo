package org.alfresco.module.org_alfresco_module_rm.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementSearchBehaviour;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.Role;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataSetServiceImpl implements DataSetService, RecordsManagementModel
{

   /** Logger */
   private static Log logger = LogFactory.getLog(DataSetServiceImpl.class);

   /** Registered data set implementations */
   private Map<String, DataSet> dataSets = new HashMap<String, DataSet>();

   /** Spaces store */
   private static final StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE,
            "SpacesStore");

   /** Importer service */
   private ImporterService importerService;

   /** Search service */
   private SearchService searchService;

   /** Node service */
   private NodeService nodeService;

   /** Records management service */
   private RecordsManagementService recordsManagementService;

   /** Records management action service */
   private RecordsManagementActionService recordsManagementActionService;

   /** Permission service */
   private PermissionService permissionService;

   /** Authority service */
   private AuthorityService authorityService;

   /** Records management security service */
   private RecordsManagementSecurityService recordsManagementSecurityService;

   /** Records management search behaviour */
   private RecordsManagementSearchBehaviour recordsManagementSearchBehaviour;

   /** Disposition service */
   private DispositionService dispositionService;

   /**
    * Set importer service
    * 
    * @param importerService the importer service
    */
   public void setImporterService(ImporterService importerService)
   {
      this.importerService = importerService;
   }

   /**
    * Set search service
    * 
    * @param searchService the search service
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }

   /**
    * Set node service
    * 
    * @param nodeService the node service
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * Set records management service
    * 
    * @param recordsManagementService the records management service
    */
   public void setRecordsManagementService(RecordsManagementService recordsManagementService)
   {
      this.recordsManagementService = recordsManagementService;
   }

   /**
    * Set records management action service
    * 
    * @param recordsManagementActionService the records management action
    *           service
    */
   public void setRecordsManagementActionService(
            RecordsManagementActionService recordsManagementActionService)
   {
      this.recordsManagementActionService = recordsManagementActionService;
   }

   /**
    * Set permission service
    * 
    * @param permissionService the permission service
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }

   /**
    * Set authority service
    * 
    * @param authorityService the authority service
    */
   public void setAuthorityService(AuthorityService authorityService)
   {
      this.authorityService = authorityService;
   }

   /**
    * Set records management security service
    * 
    * @param recordsManagementSecurityService the records management security
    *           service
    */
   public void setRecordsManagementSecurityService(
            RecordsManagementSecurityService recordsManagementSecurityService)
   {
      this.recordsManagementSecurityService = recordsManagementSecurityService;
   }

   /**
    * Set records management search behaviour
    * 
    * @param recordsManagementSearchBehaviour the records management search
    *           behaviour
    */
   public void setRecordsManagementSearchBehaviour(
            RecordsManagementSearchBehaviour recordsManagementSearchBehaviour)
   {
      this.recordsManagementSearchBehaviour = recordsManagementSearchBehaviour;
   }

   /**
    * Set disposition service
    * 
    * @param dispositionService the disposition service
    */
   public void setDispositionService(DispositionService dispositionService)
   {
      this.dispositionService = dispositionService;
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService#register(org.alfresco.module.org_alfresco_module_rm.dataset.DataSet)
    */
   @Override
   public void register(DataSet dataSet)
   {
      ParameterCheck.mandatory("dataSet", dataSet);

      this.dataSets.put(dataSet.getId(), dataSet);
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService#getDataSets()
    */
   @Override
   public Map<String, DataSet> getDataSets()
   {
      return this.dataSets;
   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService#loadDataSet(java.lang.String,
    *      org.alfresco.service.cmr.repository.NodeRef)
    */
   @Override
   public void loadDataSet(String dataSetId, NodeRef filePlan)
   {
      ParameterCheck.mandatoryString("dataSetId", dataSetId);
      ParameterCheck.mandatory("filePlan", filePlan);

      // Get the data set
      DataSet dataSet = getDataSets().get(dataSetId);

      // Import the RM test data ACP into the the provided file plan node reference
      InputStream is = null;
      try
      {
         is = getClass().getClassLoader().getResourceAsStream(dataSet.getPath());
         if (is == null)
         {
            throw new AlfrescoRuntimeException("The '" + dataSet.getLabel()
                  + "' import file could not be found!");
         }
   
         // Import view
         Reader viewReader = new InputStreamReader(is);
         Location location = new Location(filePlan);
         importerService.importView(viewReader, location, null, null);
   
         // Patch data
         patchLoadedData();
      }
      catch (Exception ex)
      {
         throw new RuntimeException("Unexpected exception thrown", ex);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
               is = null;
            }
            catch (IOException ex)
            {
               throw new RuntimeException("Failed to close the input stream!", ex);
            }
         }
      }
   }

   /**
    * Temp method to patch AMP'ed data
    * 
    * @param searchService
    * @param nodeService
    * @param recordsManagementService
    * @param recordsManagementActionService
    */
   private void patchLoadedData()
   {
      AuthenticationUtil.RunAsWork<Object> runAsWork = new AuthenticationUtil.RunAsWork<Object>()
      {
         public Object doWork() throws Exception
         {
            java.util.List<NodeRef> rmRoots = recordsManagementService.getFilePlans();
            logger.info("Bootstraping " + rmRoots.size() + " rm roots ...");
            for (NodeRef rmRoot : rmRoots)
            {
               if (permissionService.getInheritParentPermissions(rmRoot) == true)
               {
                  logger.info("Updating permissions for rm root: " + rmRoot);
                  permissionService.setInheritParentPermissions(rmRoot, false);
               }

               String allRoleShortName = "AllRoles" + rmRoot.getId();
               String allRoleGroupName = authorityService.getName(AuthorityType.GROUP,
                        allRoleShortName);

               if (authorityService.authorityExists(allRoleGroupName) == false)
               {
                  logger.info("Creating all roles group for root node: " + rmRoot.toString());

                  // Create "all" role group for root node
                  String allRoles = authorityService.createAuthority(AuthorityType.GROUP,
                           allRoleShortName, "All Roles", null);

                  // Put all the role groups in it
                  Set<Role> roles = recordsManagementSecurityService.getRoles(rmRoot);
                  for (Role role : roles)
                  {
                     logger.info("   - adding role group " + role.getRoleGroupName()
                              + " to all roles group");
                     authorityService.addAuthority(allRoles, role.getRoleGroupName());
                  }

                  // Set the permissions
                  permissionService.setPermission(rmRoot, allRoles, RMPermissionModel.READ_RECORDS,
                           true);
               }
            }

            // Make sure all the containers do not inherit permissions
            ResultSet rs = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE,
                     "TYPE:\"rma:recordsManagementContainer\"");
            try
            {
               logger.info("Bootstraping " + rs.length() + " record containers ...");

               for (NodeRef container : rs.getNodeRefs())
               {
                  String containerName = (String) nodeService.getProperty(container,
                           ContentModel.PROP_NAME);

                  // Set permissions
                  if (permissionService.getInheritParentPermissions(container) == true)
                  {
                     logger.info("Updating permissions for record container: " + containerName);
                     permissionService.setInheritParentPermissions(container, false);
                  }
               }
            }
            finally
            {
               rs.close();
            }

            // fix up the test dataset to fire initial events for disposition
            // schedules
            rs = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE,
                     "TYPE:\"rma:recordFolder\"");
            try
            {
               logger.info("Bootstraping " + rs.length() + " record folders ...");

               for (NodeRef recordFolder : rs.getNodeRefs())
               {
                  String folderName = (String) nodeService.getProperty(recordFolder,
                           ContentModel.PROP_NAME);

                  // Set permissions
                  if (permissionService.getInheritParentPermissions(recordFolder) == true)
                  {
                     logger.info("Updating permissions for record folder: " + folderName);
                     permissionService.setInheritParentPermissions(recordFolder, false);
                  }

                  if (nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE) == false)
                  {
                     // See if the folder has a disposition schedule that needs
                     // to be applied
                     DispositionSchedule ds = dispositionService
                              .getDispositionSchedule(recordFolder);
                     if (ds != null)
                     {
                        // Fire action to "set-up" the folder correctly
                        logger.info("Setting up bootstraped record folder: " + folderName);
                        recordsManagementActionService.executeRecordsManagementAction(recordFolder,
                                 "setupRecordFolder");
                     }
                  }

                  // fixup the search behaviour aspect for the record folder
                  logger.info("Setting up search aspect for record folder: " + folderName);
                  recordsManagementSearchBehaviour.fixupSearchAspect(recordFolder);
               }
            }
            finally
            {
               rs.close();
            }

            return null;
         }
      };

      AuthenticationUtil.runAs(runAsWork, AuthenticationUtil.getAdminUserName());

   }

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService#existsDataSet(java.lang.String)
    */
   @Override
   public boolean existsDataSet(String dataSetId)
   {
      ParameterCheck.mandatoryString("dataSetId", dataSetId);

      return getDataSets().containsKey(dataSetId);
   }

}
