/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for AVMCompare dialogs
 *
 * @author Dmitry Lazurkin
 */
public abstract class CompareSnapshotDialog extends BaseDialogBean
{
    private static final Log logger = LogFactory.getLog(CompareSnapshotDialog.class);

    private static final long serialVersionUID = 5483551383286687197L;

    private final static String MSG_CLOSE = "close";
    protected AVMBrowseBean avmBrowseBean;
    transient private SandboxService sandboxService;
    transient private AVMService avmService;
    transient private AVMSyncService avmSyncService;
    protected NodeRef websiteRef;
    protected String store;
    protected NodeRef webProjectRef;
    protected int version;
    protected String sandbox;
    protected String storeRoot;

    protected boolean finished = false;

    public abstract List<Map<String, String>> getComparedNodes();

    @Override
    public void init(Map<String, String> parameters)
    {
        super.init(parameters);

        // setup context for dialog
        this.sandbox = parameters.get("sandbox");

        String ver = parameters.get("version");
        if (ver != null && ver.length() > 0)
        {
            this.version = Integer.parseInt(ver);
        }
        else
        {
            this.version = -1;
        }

        // get the store
        this.store = parameters.get("store");
        this.storeRoot = AVMUtil.buildSandboxRootPath(this.sandbox);

        // get the web project noderef
        String webProject = parameters.get("webproject");
        if (webProject == null)
        {
            this.webProjectRef = this.avmBrowseBean.getWebsite().getNodeRef();
        }
        else
        {
            this.webProjectRef = new NodeRef(webProject);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Initialising dialog compare snapshot: " + this.websiteRef);
        }
    }

    @Override
    public String getContainerDescription()
    {
        int prev = WCMCompareUtils.getPrevVersionID(getSandboxService(), sandbox, version);
        return MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), getDescription()), version, prev);
    }

    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return outcome;
    }

    /**
     * Getter for avmBrowseBean
     *
     * @return avmBrowseBean
     */
    public AVMBrowseBean getAvmBrowseBean()
    {
        return avmBrowseBean;
    }

    /**
     * Setter for avmBrowseBean
     *
     * @param avmBrowseBean avm browse bean
     */
    public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
    {
        this.avmBrowseBean = avmBrowseBean;
    }

    /**
     * Getter for avmService service
     *
     * @return avmService
     */
    public AVMService getAvmService()
    {
        if (avmService == null)
        {
            avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
        }

        return avmService;
    }

    /**
     * Getter for sandboxService service
     * 
     * @return sandboxService
     */
    public SandboxService getSandboxService()
    {
        if (sandboxService == null)
        {
            sandboxService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getSandboxService();
        }
        return sandboxService;
    }

    /**
     * Getter for avmSyncService service
     *
     * @return avmSyncService
     */
    public AVMSyncService getAvmSyncService()
    {
        if (avmSyncService == null)
        {
            avmSyncService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMSyncService();
        }
        return avmSyncService;
    }

    /**
     * Returns description message id for AVMCompare dialog
     *
     * @return description message id for dialog
     */
    protected abstract String getDescription();

    /**
     * Returns sandbox name
     *
     * @return sandbox name
     */
    public String getSandbox()
    {
        return sandbox;
    }
}
