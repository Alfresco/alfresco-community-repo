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

/**
 */
public class XFormsBean
    implements EventListener
{
    private static final Log LOGGER = LogFactory.getLog(XFormsBean.class);

    private TemplateType tt;
    private Document instanceData = null;
    private ChibaBean chibaBean;
    private Map context = new HashMap();

    public TemplateType getTemplateType()
    {
	return this.tt;
    }

    public void setTemplateType(final TemplateType tt)
    {
	this.tt = tt;
    }

    public void setInstanceData(final Document instanceData)
    {
	this.instanceData = instanceData;
    }

    public void init()
	throws XFormsException
    {
	this.chibaBean = new ChibaBean();
	this.chibaBean.setContext(context);

	try
        {
	    LOGGER.debug("initializing " + this + 
			 " with tt " + tt.getName());
	    final XFormsInputMethod tim = (XFormsInputMethod)
		tt.getInputMethods().get(0);
	    final Document form = tim.getXForm(instanceData, tt);
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
	//	    final String templateTypeName = (String)requestParameters.get("template_type_name");
	
	LOGGER.debug("building xform for " + this.tt.getName()); 	    //templateTypeName);
	
	//	    final TemplatingService ts = TemplatingService.getInstance();
	//	    final TemplateType tt = ts.getTemplateType(templateTypeName);
	//	    final XFormsInputMethod tim = (XFormsInputMethod)
	//		tt.getInputMethods().get(0);
	//	    final Document form = tim.getXForm(tt);
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
	throws XFormsException
    {
	final FacesContext context = FacesContext.getCurrentInstance();
	final Map requestParameters = context.getExternalContext().getRequestParameterMap();
	final String id = (String)requestParameters.get("id");
	final String value = (String)requestParameters.get("value");

	LOGGER.debug(this + " setXFormsValue(" + id + ", " + value + ")");
	this.chibaBean.updateControlValue(id, value);
    }

    /**
     * sets the value of a control in the processor.
     *
     * @param id the id of the control in the host document
     * @param value the new value
     * @return the list of events that may result through this action
     */
    public void fireAction() 
	throws XFormsException
    {
	final FacesContext context = FacesContext.getCurrentInstance();
	final Map requestParameters = context.getExternalContext().getRequestParameterMap();
	final String id = (String)requestParameters.get("id");

	LOGGER.debug(this + " fireAction(" + id + ")");
	this.chibaBean.dispatch(id, XFormsEventFactory.DOM_ACTIVATE);
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
	final FacesContext context = FacesContext.getCurrentInstance();
	final HttpServletRequest request = (HttpServletRequest)
	    context.getExternalContext().getRequest();
	BufferedReader bufferedReader = request.getReader();
	StringBuffer sb = new StringBuffer();
	do
	{
	    String s = bufferedReader.readLine();
	    if (s == null)
		break;
	    sb.append(s).append('\n');
	}
	while (true);
	String xml = sb.toString();
	System.out.println("you submitting " + xml);
	final ResponseWriter out = context.getResponseWriter();
	out.write(xml);
	out.close();
    }

    public void handleEvent(Event e)
    {
	LOGGER.debug("handleEvent " + e);
    }
}
