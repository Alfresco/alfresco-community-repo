/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.behaviour.RecordsManagementSearchBehaviour;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.RMAuthority;
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
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataSetServiceImpl implements DataSetService, RecordsManagementModel
{

    /** Logger */
    private static Log logger = LogFactory.getLog(DataSetServiceImpl.class);

    /** Registered data set implementations */
    private Map<String, DataSet> dataSets = new HashMap<>();

    /** Spaces store */
    private static final StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

    /** Charset name */
    private static final String CHARSET_NAME = "UTF-8";

    /** Importer service */
    private ImporterService importerService;

    /** Search service */
    private SearchService searchService;

    /** Node service */
    private NodeService nodeService;

    /** File plan service service */
    private FilePlanService filePlanService;

    /** Permission service */
    private PermissionService permissionService;

    /** Authority service */
    private AuthorityService authorityService;

    /** File plan role service */
    private FilePlanRoleService filePlanRoleService;

    /** Records management search behaviour */
    private RecordsManagementSearchBehaviour recordsManagementSearchBehaviour;

    /** Disposition service */
    private DispositionService dispositionService;

    /** Record folder service */
    private RecordFolderService recordFolderService;

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
     * Set file plan service
     *
     * @param filePlanService the file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
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
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    /**
     * Set records management search behaviour
     *
     * @param recordsManagementSearchBehaviour the records management search
     *            behaviour
     */
    public void setRecordsManagementSearchBehaviour(RecordsManagementSearchBehaviour recordsManagementSearchBehaviour)
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
     * Set record folder service
     *
     * @param recordFolderService the record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
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
     * @see org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService#getDataSets(NodeRef,
     *      boolean)
     */
    @Override
    public Map<String, DataSet> getDataSets(NodeRef filePlan, boolean excludeLoaded)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        ParameterCheck.mandatory("excludeLoaded", excludeLoaded);

        // Get the list of all available data sets
        Map<String, DataSet> dataSets = new HashMap<>(getDataSets());

        // Should the list of unloaded data sets be retrieved
        if (excludeLoaded)
        {
            dataSets.keySet().removeAll(getLoadedDataSets(filePlan).keySet());
        }

        // Return the (filtered) list of data sets
        return dataSets;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService#loadDataSet(
     *org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public void loadDataSet(NodeRef filePlan, String dataSetId)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        ParameterCheck.mandatoryString("dataSetId", dataSetId);

        // Get the data set
        DataSet dataSet = getDataSets().get(dataSetId);

        // Import the RM test data ACP into the the provided file plan node
        // reference
        InputStream is = null;
        try
        {
            is = getClass().getClassLoader().getResourceAsStream(dataSet.getPath());
            if (is == null) { throw new AlfrescoRuntimeException("The '" + dataSet.getLabel()
                    + "' import file could not be found!"); }

            // Import view
            Reader viewReader = new InputStreamReader(is, CHARSET_NAME);
            Location location = new Location(filePlan);
            importerService.importView(viewReader, location, null, null);

            // Patch data
            patchLoadedData();

            // Set the data set id into the file plan's custom aspect
            setDataSetIdIntoFilePlan(dataSetId, filePlan);
        }
        catch (Exception ex)
        {
            throw new AlfrescoRuntimeException("Unexpected exception thrown. Please refer to the log files for details.", ex);
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
                    throw new AlfrescoRuntimeException("Failed to close the input stream!", ex);
                }
            }
        }
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

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService#getLoadedDataSets(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Map<String, DataSet> getLoadedDataSets(NodeRef filePlan)
    {
        ParameterCheck.mandatory("filePlan", filePlan);

        // Get the list of available data sets
        Map<String, DataSet> availableDataSets = new HashMap<>(getDataSets());

        // Get the property value of the aspect
        Serializable dataSetIds = nodeService.getProperty(filePlan, PROP_LOADED_DATA_SET_IDS);
        // Check if any data has been loaded before
        if (dataSetIds != null)
        {
            // Filter the data sets which have already been loaded
            @SuppressWarnings("unchecked")
            ArrayList<String> loadedDataSetIds = (ArrayList<String>) dataSetIds;
            Iterator<Map.Entry<String, DataSet>> iterator = availableDataSets.entrySet().iterator();
            while (iterator.hasNext())
            {
                Entry<String, DataSet> entry = iterator.next();
                String key = entry.getKey();
                if (!loadedDataSetIds.contains(key))
                {
                    iterator.remove();
                }
            }
            return availableDataSets;
        }

        return new HashMap<>();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService#isLoadedDataSet(org.alfresco.service.cmr.repository.NodeRef,
     *      java.lang.String)
     */
    @Override
    public boolean isLoadedDataSet(NodeRef filePlan, String dataSetId)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        ParameterCheck.mandatory("dataSetId", dataSetId);

        return getLoadedDataSets(filePlan).containsKey(dataSetId);
    }

    /**
     * Temp method to patch AMP'ed data
     */
    private void patchLoadedData()
    {
        AuthenticationUtil.RunAsWork<Object> runAsWork = new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork()
            {
                Set<NodeRef> rmRoots = filePlanService.getFilePlans();
                logger.info("Bootstraping " + rmRoots.size() + " rm roots ...");
                for (NodeRef rmRoot : rmRoots)
                {
                    if (permissionService.getInheritParentPermissions(rmRoot))
                    {
                        logger.info("Updating permissions for rm root: " + rmRoot);
                        permissionService.setInheritParentPermissions(rmRoot, false);
                    }

                    String allRoleShortName = RMAuthority.ALL_ROLES_PREFIX + rmRoot.getId();
                    String allRoleGroupName = authorityService.getName(AuthorityType.GROUP, allRoleShortName);

                    if (!authorityService.authorityExists(allRoleGroupName))
                    {
                        logger.info("Creating all roles group for root node: " + rmRoot.toString());

                        // Create "all" role group for root node
                        String allRoles = authorityService.createAuthority(AuthorityType.GROUP, allRoleShortName,
                                RMAuthority.ALL_ROLES_DISPLAY_NAME, new HashSet<>(Arrays.asList(RMAuthority.ZONE_APP_RM)));

                        // Put all the role groups in it
                        Set<Role> roles = filePlanRoleService.getRoles(rmRoot);
                        for (Role role : roles)
                        {
                            logger.info("   - adding role group " + role.getRoleGroupName() + " to all roles group");
                            authorityService.addAuthority(allRoles, role.getRoleGroupName());
                        }

                        // Set the permissions
                        permissionService.setPermission(rmRoot, allRoles, RMPermissionModel.READ_RECORDS, true);
                    }
                }

                // Make sure all the containers do not inherit permissions
                ResultSet rs = searchService.query(SPACES_STORE, SearchService.LANGUAGE_FTS_ALFRESCO,
                        "TYPE:\"rma:recordsManagementContainer\"");
                try
                {
                    logger.info("Bootstraping " + rs.length() + " record containers ...");

                    for (NodeRef container : rs.getNodeRefs())
                    {
                        String containerName = (String) nodeService.getProperty(container, ContentModel.PROP_NAME);

                        // Set permissions
                        if (permissionService.getInheritParentPermissions(container))
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

                // fix up the test dataset to fire initial events for
                // disposition
                // schedules
                rs = searchService.query(SPACES_STORE, SearchService.LANGUAGE_FTS_ALFRESCO, "TYPE:\"rma:recordFolder\"");
                try
                {
                    logger.info("Bootstraping " + rs.length() + " record folders ...");

                    for (NodeRef recordFolder : rs.getNodeRefs())
                    {
                        String folderName = (String) nodeService.getProperty(recordFolder, ContentModel.PROP_NAME);

                        // Set permissions
                        if (permissionService.getInheritParentPermissions(recordFolder))
                        {
                            logger.info("Updating permissions for record folder: " + folderName);
                            permissionService.setInheritParentPermissions(recordFolder, false);
                        }

                        if (!nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE))
                        {
                            // See if the folder has a disposition schedule that
                            // needs
                            // to be applied
                            DispositionSchedule ds = dispositionService.getDispositionSchedule(recordFolder);
                            if (ds != null)
                            {
                                // Fire action to "set-up" the folder correctly
                                logger.info("Setting up bootstraped record folder: " + folderName);
                                recordFolderService.setupRecordFolder(recordFolder);
                            }
                        }

                        // fixup the search behaviour aspect for the record
                        // folder
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
     * Helper method for setting the id of the imported data set into the file
     * plan's aspect
     *
     * @param dataSetId The id of the imported data set
     * @param filePlan The file plan into which the data set has been imported
     */
    @SuppressWarnings("unchecked")
    private void setDataSetIdIntoFilePlan(String dataSetId, NodeRef filePlan)
    {
        ArrayList<String> loadedDataSetIds;
        Serializable dataSetIds = nodeService.getProperty(filePlan, PROP_LOADED_DATA_SET_IDS);

        // Check if any data set has been imported
        if (dataSetIds == null)
        {
            Map<QName, Serializable> aspectProperties = new HashMap<>(1);
            aspectProperties.put(PROP_LOADED_DATA_SET_IDS, (Serializable) new ArrayList<String>());
            nodeService.addAspect(filePlan, ASPECT_LOADED_DATA_SET_ID, aspectProperties);
            loadedDataSetIds = (ArrayList<String>) nodeService.getProperty(filePlan, PROP_LOADED_DATA_SET_IDS);
        }
        else
        {
            loadedDataSetIds = (ArrayList<String>) dataSetIds;
        }

        // Add the new loaded data set id
        loadedDataSetIds.add(dataSetId);
        Map<QName, Serializable> aspectProperties = new HashMap<>(1);
        aspectProperties.put(PROP_LOADED_DATA_SET_IDS, (Serializable) loadedDataSetIds);
        nodeService.addAspect(filePlan, ASPECT_LOADED_DATA_SET_ID, aspectProperties);
    }

}
