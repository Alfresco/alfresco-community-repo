package org.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.sandbox.SandboxConstants;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Patch that migrates deployment data to the new deployment model.
 * 
 * @author gavinc
 */
public class DeploymentMigrationPatch extends AbstractPatch
{
    protected ImporterBootstrap importerBootstrap;
    protected IndexerAndSearcher indexerAndSearcher;
    protected AVMService avmService;
    
    private static final String MSG_SUCCESS = "patch.deploymentMigration.result";
    private static final String MSG_WEBPROJECT = "patch.deploymentMigration.webProjectName";
    private static final String MSG_SERVER_MIGRATED = "patch.deploymentMigration.serverMigrated";
    private static final String MSG_REPORT_MIGRATED = "patch.deploymentMigration.reportMigrated";
    
    private static final String FILE_SERVER_PREFIX = "\\\\";
    
    private static final Log logger = LogFactory.getLog(DeploymentMigrationPatch.class);

    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }
   
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }
    
    public void setAvmService(AVMService avmService)
    {
       this.avmService = avmService;
    }
    
    @Override
    protected String applyInternal() throws Exception 
    {
        String query = "TYPE:\"wca:webfolder\"";
        
        ResultSet results = null;
        try
        {
            results = this.searchService.query(this.importerBootstrap.getStoreRef(), 
                SearchService.LANGUAGE_LUCENE, query);
            
            // iterate through the web projects and migrate the deployment data
            if (results.length() > 0)
            {
                for (NodeRef node : results.getNodeRefs())
                {
                    if (this.nodeService.exists(node))
                    {
                        migrate(node);
                    }
                }
            }
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }
       
        // return success message
        return I18NUtil.getMessage(MSG_SUCCESS);
    }
    
    @SuppressWarnings("unchecked")
    protected void migrate(NodeRef webProject)
    {
        // output name of web project currently being migrated
        String projectName = (String)this.nodeService.getProperty(webProject, ContentModel.PROP_NAME);
        logger.info(I18NUtil.getMessage(MSG_WEBPROJECT, projectName));
       
        // see if the web project has any deployment servers configured
        List<String> deployTo = (List<String>)this.nodeService.getProperty(webProject, WCMAppModel.PROP_DEPLOYTO);
        if (deployTo != null && deployTo.size() > 0)
        {
            for (String server : deployTo)
            {
               if (server != null && server.length() > 0)
               {
                  migrateServer(server.trim(), webProject, projectName);
               }
            }
        }
       
        // migrate any deployment reports present
        List<ChildAssociationRef> deployReportRefs = nodeService.getChildAssocs(webProject, 
                WCMAppModel.ASSOC_DEPLOYMENTREPORT, RegexQNamePattern.MATCH_ALL);
        if (deployReportRefs.size() > 0)
        {
            // gather data required for deploymentattempt node
            String attemptId = GUID.generate();
            String store = (String)this.nodeService.getProperty(webProject, WCMAppModel.PROP_AVMSTORE);
            List<String> servers = (List<String>)this.nodeService.getProperty(webProject, 
                    WCMAppModel.PROP_SELECTEDDEPLOYTO);
            if (servers == null)
            {
               servers = new ArrayList<String>();
            }
            Integer version = (Integer)this.nodeService.getProperty(webProject, 
                    WCMAppModel.PROP_SELECTEDDEPLOYVERSION);
            Date time = (Date)this.nodeService.getProperty(
                    deployReportRefs.get(0).getChildRef(), WCMAppModel.PROP_DEPLOYSTARTTIME);
          
            // create a deploymentattempt node for the reports to move to
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(8, 1.0f);
            props.put(WCMAppModel.PROP_DEPLOYATTEMPTID, attemptId);
            props.put(WCMAppModel.PROP_DEPLOYATTEMPTTYPE, WCMAppModel.CONSTRAINT_LIVESERVER);
            props.put(WCMAppModel.PROP_DEPLOYATTEMPTSTORE, store);
            props.put(WCMAppModel.PROP_DEPLOYATTEMPTVERSION, version);
            props.put(WCMAppModel.PROP_DEPLOYATTEMPTSERVERS, (Serializable)servers);
            props.put(WCMAppModel.PROP_DEPLOYATTEMPTTIME, time);
            NodeRef attempt = this.nodeService.createNode(webProject, 
                  WCMAppModel.ASSOC_DEPLOYMENTATTEMPT, WCMAppModel.ASSOC_DEPLOYMENTATTEMPT, 
                  WCMAppModel.TYPE_DEPLOYMENTATTEMPT, props).getChildRef();
           
            // set the attempt id on the staging store
            this.avmService.setStoreProperty(store, SandboxConstants.PROP_LAST_DEPLOYMENT_ID, 
                     new PropertyValue(DataTypeDefinition.TEXT, attemptId));
            
            // migrate each report found
            for (ChildAssociationRef ref : deployReportRefs)
            {
                migrateReport(ref.getChildRef(), attempt, webProject, projectName);
            }
        }
        
        // remove all the deprecated properties in the web project
        this.nodeService.removeProperty(webProject, WCMAppModel.PROP_DEPLOYTO);
        this.nodeService.removeProperty(webProject, WCMAppModel.PROP_SELECTEDDEPLOYTO);
        this.nodeService.removeProperty(webProject, WCMAppModel.PROP_SELECTEDDEPLOYVERSION);
    }

    protected void migrateServer(String server, NodeRef webProject, String webProjectName)
    {
        // work out the host and port
        String host = server;
        int port = -1;
        int idx = server.indexOf(":");
        if (idx != -1)
        {
            host = server.substring(0, idx);
            String strPort = server.substring(idx+1);
            port = Integer.parseInt(strPort);
        }
        
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(4, 1.0f);
        if (server.startsWith(FILE_SERVER_PREFIX))
        {
            // server name starts with \\ so is therefore a file system deployment
            props.put(WCMAppModel.PROP_DEPLOYTYPE, WCMAppModel.CONSTRAINT_FILEDEPLOY);
            host = host.substring(FILE_SERVER_PREFIX.length());
        }
        else
        {
            // server name does not start with \\ so is therefore an Alfresco server deployment
            props.put(WCMAppModel.PROP_DEPLOYTYPE, WCMAppModel.CONSTRAINT_ALFDEPLOY);
        }
        
        // set the properties
        props.put(WCMAppModel.PROP_DEPLOYSERVERTYPE, WCMAppModel.CONSTRAINT_LIVESERVER);
        props.put(WCMAppModel.PROP_DEPLOYSERVERHOST, host);
        if (port != -1)
        {
           props.put(WCMAppModel.PROP_DEPLOYSERVERPORT, new Integer(port));
        }
        
        // create the deploymentserver node as a child of the webproject
        this.nodeService.createNode(webProject, WCMAppModel.ASSOC_DEPLOYMENTSERVER, 
              WCMAppModel.ASSOC_DEPLOYMENTSERVER, WCMAppModel.TYPE_DEPLOYMENTSERVER, 
              props).getChildRef();
         
        // inform of migration
        logger.info(I18NUtil.getMessage(MSG_SERVER_MIGRATED, server, webProjectName));
    }
    
    protected void migrateReport(NodeRef report, NodeRef attempt, NodeRef webProject, 
             String webProjectName)
    {
       String server = (String)this.nodeService.getProperty(report, WCMAppModel.PROP_DEPLOYSERVER);
          
       // make the deployment report node a child of the given deploymentattempt node
       this.nodeService.moveNode(report, attempt, WCMAppModel.ASSOC_DEPLOYMENTREPORTS,
                WCMAppModel.ASSOC_DEPLOYMENTREPORTS);
       
       // inform of migration
       logger.info(I18NUtil.getMessage(MSG_REPORT_MIGRATED, server, webProjectName));
    }
}
