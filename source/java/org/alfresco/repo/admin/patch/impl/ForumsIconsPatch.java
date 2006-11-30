package org.alfresco.repo.admin.patch.impl;

import java.io.Serializable;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;

/**
 * Patch to remove the '_large' from the icon property for all the forums
 * based space types i.e. fm:forums, fm:forum and fm:topic.
 * 
 * @author gavinc
 */
public class ForumsIconsPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.forumsIcons.result";
    
    private ImporterBootstrap importerBootstrap;
   
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }
    
    @Override
    protected String applyInternal() throws Exception 
    {
        int iconsChanged = 0;
        
        // change all the fm:forums nodes
        iconsChanged += changeIcons(ForumModel.TYPE_FORUMS);
        
        // change all the fm:forum nodes
        iconsChanged += changeIcons(ForumModel.TYPE_FORUM);
        
        // change all the topic nodes
        iconsChanged += changeIcons(ForumModel.TYPE_TOPIC);
       
        // return success message
        return I18NUtil.getMessage(MSG_SUCCESS, new Object[] {iconsChanged});
    }
    
    /**
     * Removes the '_large' from the icon property for the nodes of the given type
     * 
     * @param typeName The qname of the type to change the icon property for
     * @return Returns the number of icons changed
     */
    private int changeIcons(QName typeName)
    {
        int changed = 0;
        String query = "TYPE:\"" + typeName.toString() + "\"";
        
        ResultSet results = null;
        try
        {
            results = this.searchService.query(this.importerBootstrap.getStoreRef(), 
                SearchService.LANGUAGE_LUCENE, query);
            
            // if there are any results iterate through nodes and update icon property
            if (results.length() > 0)
            {
               for (NodeRef node : results.getNodeRefs())
               {
                   if (this.nodeService.exists(node))
                   {
                       String icon = (String)this.nodeService.getProperty(node, ApplicationModel.PROP_ICON);
                       if (icon != null && icon.length() > 0)
                       {
                           int idx = icon.indexOf("_large");
                           if (idx != -1)
                           {
                               String newIcon = icon.substring(0, idx);
                               this.nodeService.setProperty(node, ApplicationModel.PROP_ICON, (Serializable)newIcon);
                               changed++;
                           }
                       }
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
        
        return changed;
    }
}
