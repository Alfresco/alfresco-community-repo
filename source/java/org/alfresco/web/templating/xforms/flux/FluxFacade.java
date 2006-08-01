package org.alfresco.web.templating.xforms.flux;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chiba.adapter.ChibaAdapter;
import org.chiba.adapter.ChibaEvent;
import org.chiba.adapter.DefaultChibaEventImpl;
import org.alfresco.web.templating.xforms.servlet.ChibaServlet;
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;
import uk.ltd.getahead.dwr.ExecutionContext;

import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;

/**
 * AJAX Facade class to hide the full functionality from the web-client.
 *
 * @author Joern Turner
 * @version $Id: FluxFacade.java,v 1.9 2005/12/21 19:06:55 unl Exp $
 */
public class FluxFacade 
{

    //this is a custom event to activate a trigger in XForms.
    private static final Log LOGGER = LogFactory.getLog(FluxFacade.class); 
    public static final String FLUX_ACTIVATE_EVENT = "flux-action-event";
    ChibaAdapter adapter = null;
    private HttpSession session;


    /**
     * grabs the actual adapter from the session.
     */
    public FluxFacade() {
        session = ExecutionContext.get().getSession();
        adapter = (ChibaAdapter) session.getAttribute(ChibaServlet.CHIBA_ADAPTER);
    }

    /**
     * executes a trigger
     *
     * @param id the id of the trigger to execute
     * @return the list of events that may result through this action
     * @throws FluxException
     */
    public Element fireAction(String id) throws FluxException {
	LOGGER.debug("fireAction " + id);
        ChibaEvent chibaActivateEvent = new DefaultChibaEventImpl();
        chibaActivateEvent.initEvent(FLUX_ACTIVATE_EVENT, id, null);
        return dispatch(chibaActivateEvent);
    }

    /**
     * sets the value of a control in the processor.
     *
     * @param id the id of the control in the host document
     * @param value the new value
     * @return the list of events that may result through this action
     * @throws FluxException
     */
    public Element setXFormsValue(String id, String value) throws FluxException {
	LOGGER.debug("setXFormsValue(" + id + ", " + value + ")");
        ChibaEvent event = new DefaultChibaEventImpl();
        event.initEvent("SETVALUE", id, value);
        return dispatch(event);
    }

    public Element setRepeatIndex(String id, String position) throws FluxException {
	LOGGER.debug("setRepeatPosition(" + id + ", " + position + ")");
        ChibaEvent event = new DefaultChibaEventImpl();
        event.initEvent("SETINDEX", id, position);
        return dispatch(event);
    }

    /**
     * fetches the progress of a running upload.
     *
     * @param id id of the upload control in use
     * @param filename filename for uploaded data
     * @return a array containing two elements for evaluation in browser. First
     *         param is the upload control id and second will be the current
     *         progress of the upload.
     */
    public Element fetchProgress(String id, String filename) {
        String progress = "0";

        if (session.getAttribute(filename) != null) {
            progress = ((Integer) session.getAttribute(filename)).toString();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Percent completed: " + progress);
            }
        }

        EventLog eventLog = (EventLog) adapter.getContextParam("EVENT-LOG");
        if (eventLog == null) {
            eventLog = new EventLog();
        }
        Element eventlogElement = eventLog.getLog();
        eventLog.flush();

        Element progressEvent = eventLog.add("upload-progress-event", id, "upload");
        eventLog.addProperty(progressEvent, "progress", progress);
        return eventlogElement;
    }

    private Element dispatch(ChibaEvent event) throws FluxException {
	LOGGER.debug("dispatching " + event);
        if (adapter != null) {
            try {
                adapter.dispatch(event);
            }
            catch (XFormsException e) {
                throw new FluxException(e);
            }
        }
        else {
            //session expired or cookie got lost
            throw new FluxException("Session expired. Please start again.");
        }
        EventLog eventLog = (EventLog) adapter.getContextParam("EVENT-LOG");
        Element eventlogElement = eventLog.getLog();

        if (LOGGER.isDebugEnabled()) {
            try 
	    {
                DOMUtil.prettyPrintDOM(eventlogElement, System.out);
            }
            catch (TransformerException e) 
            {
                e.printStackTrace();
            }
        }
        return eventlogElement;
    }

    public String getInfo() 
    {
        return "FluxFacade using " + adapter.toString();
    }
}

// end of class
