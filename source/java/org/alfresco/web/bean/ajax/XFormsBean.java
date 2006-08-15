/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean.ajax;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.web.templating.*;
import org.alfresco.web.templating.xforms.*;
import org.alfresco.web.templating.xforms.schemabuilder.FormBuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.alfresco.web.app.servlet.FacesHelper;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.events.XFormsEventFactory;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.chiba.xml.xforms.connector.http.AbstractHTTPConnector;

/**
 */
public class XFormsBean
    implements EventListener
{
    private static final Log LOGGER = LogFactory.getLog(XFormsBean.class);

    private TemplateType tt;
    private InstanceData instanceData = null;
    private ChibaBean chibaBean;

    public TemplateType getTemplateType()
    {
	return this.tt;
    }

    public void setTemplateType(final TemplateType tt)
    {
	this.tt = tt;
    }

    public void setInstanceData(final InstanceData instanceData)
    {
	this.instanceData = instanceData;
    }

    public void init()
	throws XFormsException
    {
	this.chibaBean = new ChibaBean();
	final FacesContext facesContext = FacesContext.getCurrentInstance();
	final ExternalContext externalContext = facesContext.getExternalContext();
	final HttpServletRequest request = (HttpServletRequest)
	    externalContext.getRequest();
	XFormsBean.storeCookies(request.getCookies(), this.chibaBean);
	try
        {
	    LOGGER.debug("initializing " + this + 
			 " with tt " + tt.getName());
	    final XFormsInputMethod tim = (XFormsInputMethod)
		tt.getInputMethods().get(0);
	    final Document form = tim.getXForm(instanceData.getContent(), tt);
	    this.chibaBean.setXMLContainer(form);
	    this.chibaBean.init();
	    EventTarget et = (EventTarget)
		this.chibaBean.getXMLContainer().getDocumentElement();
	    et.addEventListener(XFormsEventFactory.SUBMIT_ERROR, this, true);
	}
	catch (FormBuilderException fbe)
        {
	    LOGGER.error(fbe);
	}
    }

    public void getXForm() 
	throws IOException,
	       XFormsException
    {
	LOGGER.debug(this + " building xform");
	final FacesContext context = FacesContext.getCurrentInstance();
	final ResponseWriter out = context.getResponseWriter();
	final Map requestParameters = context.getExternalContext().getRequestParameterMap();
	LOGGER.debug("building xform for " + this.tt.getName());
	final Node form = this.chibaBean.getXMLContainer();
	final TemplatingService ts = TemplatingService.getInstance();
	ts.writeXML(form, out);
    }

    /**
     * sets the value of a control in the processor.
     *
     * @param id the id of the control in the host document
     * @param value the new value
     * @return the list of events that may result through this action
     */
    public void setXFormsValue() 
	throws XFormsException, IOException
    {
	final FacesContext context = FacesContext.getCurrentInstance();
	final Map requestParameters = context.getExternalContext().getRequestParameterMap();
	final String id = (String)requestParameters.get("id");
	final String value = (String)requestParameters.get("value");

	LOGGER.debug(this + " setXFormsValue(" + id + ", " + value + ")");
	this.chibaBean.updateControlValue(id, value);
	final ResponseWriter out = context.getResponseWriter();
	out.write("<todo/>");
	out.close();
    }

    /**
     * sets the value of a control in the processor.
     *
     * @param id the id of the control in the host document
     * @param value the new value
     * @return the list of events that may result through this action
     */
    public void fireAction() 
	throws XFormsException, IOException
    {
	final FacesContext context = FacesContext.getCurrentInstance();
	final Map requestParameters = context.getExternalContext().getRequestParameterMap();
	final String id = (String)requestParameters.get("id");

	LOGGER.debug(this + " fireAction(" + id + ")");
	this.chibaBean.dispatch(id, XFormsEventFactory.DOM_ACTIVATE);
	final ResponseWriter out = context.getResponseWriter();
	out.write("<todo/>");
	out.close();
    }

    /**
     * sets the value of a control in the processor.
     *
     * @param id the id of the control in the host document
     * @param value the new value
     * @return the list of events that may result through this action
     */
    public void handleAction() 
	throws Exception
    {
	LOGGER.debug(this + " handleAction");
	final FacesContext context = FacesContext.getCurrentInstance();
	final HttpServletRequest request = (HttpServletRequest)
	    context.getExternalContext().getRequest();
	final TemplatingService ts = TemplatingService.getInstance();
	final Document result = ts.parseXML(request.getInputStream());
	this.instanceData.setContent(result);
	final ResponseWriter out = context.getResponseWriter();
	ts.writeXML(result, out);
	out.close();
    }

    public void handleEvent(Event e)
    {
	LOGGER.debug("handleEvent " + e);
    }

    /**
     * stores cookies that may exist in request and passes them on to processor for usage in
     * HTTPConnectors. Instance loading and submission then uses these cookies. Important for
     * applications using auth.
     *
     * @param request the servlet request
     * @param adapter the Chiba adapter instance
     */
    private static void storeCookies(final javax.servlet.http.Cookie[] cookiesIn,
				     final ChibaBean chibaBean){
	if (cookiesIn != null) {
	    org.apache.commons.httpclient.Cookie[] commonsCookies = 
		new org.apache.commons.httpclient.Cookie[cookiesIn.length];
	    for (int i = 0; i < cookiesIn.length; i += 1) {
		commonsCookies[i] =
		    new org.apache.commons.httpclient.Cookie(cookiesIn[i].getDomain(),
							     cookiesIn[i].getName(),
							     cookiesIn[i].getValue(),
							     cookiesIn[i].getPath(),
							     cookiesIn[i].getMaxAge(),
							     cookiesIn[i].getSecure());
	    }
	    chibaBean.getContext().put(AbstractHTTPConnector.REQUEST_COOKIE,
				       commonsCookies);
	}
    }
}
