package org.alfresco.web.templating.xforms.flux;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chiba.adapter.AbstractChibaAdapter;
import org.chiba.adapter.ChibaEvent;
import org.alfresco.web.templating.xforms.servlet.HttpRequestHandler;
import org.alfresco.web.templating.xforms.servlet.ServletAdapter;
import org.alfresco.web.templating.xforms.servlet.ChibaServlet;
import org.chiba.xml.xforms.events.XFormsEvent;
import org.chiba.xml.xforms.events.XFormsEventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.ui.Repeat;
import org.chiba.xml.util.DOMUtil;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for processing DWR calls and building appropriate responses. This
 * class is not exposed through DWR. Instead a Facadeclass 'FluxFacade' will be
 * exposed that only allows to use the dispatch method. All other methods will
 * be hidden for security.
 *
 * @author Joern Turner
 * @version $Id: FluxAdapter.java,v 1.15 2005/12/21 22:59:27 unl Exp $
 */
public class FluxAdapter extends AbstractChibaAdapter implements EventListener {
    private static final Log LOGGER = LogFactory.getLog(FluxAdapter.class); 

    private final HttpSession session;
    private EventLog eventLog;
    private EventTarget root;


    public FluxAdapter(HttpSession session) {
        this.chibaBean = createXFormsProcessor();
        this.context = new HashMap();
        chibaBean.setContext(this.context);
        this.eventLog = new EventLog();
	this.session = session;
    }

    /**
     * initialize the Adapter. This is necessary cause often the using
     * application will need to configure the Adapter before actually using it.
     *
     * @throws org.chiba.xml.xforms.exception.XFormsException
     */
    public void init() throws XFormsException {
        try {
            // get docuent root as event target in order to capture all events
            this.root = (EventTarget) this.chibaBean.getXMLContainer().getDocumentElement();

            // interaction events my occur during init so we have to register before
            this.root.addEventListener(XFormsEventFactory.CHIBA_LOAD_URI, this, true);
            this.root.addEventListener(XFormsEventFactory.CHIBA_RENDER_MESSAGE, this, true);
            this.root.addEventListener(XFormsEventFactory.CHIBA_REPLACE_ALL, this, true);

            // init processor
            this.chibaBean.init();

            // todo: add getter for event log
            setContextParam("EVENT-LOG", this.eventLog);

            // register for notification events
            this.root.addEventListener(XFormsEventFactory.CHIBA_STATE_CHANGED, this, true);
            this.root.addEventListener(XFormsEventFactory.CHIBA_PROTOTYPE_CLONED, this, true);
            this.root.addEventListener(XFormsEventFactory.CHIBA_ID_GENERATED, this, true);
            this.root.addEventListener(XFormsEventFactory.CHIBA_ITEM_INSERTED, this, true);
            this.root.addEventListener(XFormsEventFactory.CHIBA_ITEM_DELETED, this, true);
            this.root.addEventListener(XFormsEventFactory.CHIBA_INDEX_CHANGED, this, true);
            this.root.addEventListener(XFormsEventFactory.CHIBA_SWITCH_TOGGLED, this, true);
        }
        catch (Exception e) {
            throw new XFormsException(e);
        }
    }

    /**
     * Dispatch a ChibaEvent to trigger some XForms processing such as updating
     * of values or execution of triggers.
     *
     * @param event an application specific event
     * @throws org.chiba.xml.xforms.exception.XFormsException
     * @see org.chiba.adapter.DefaultChibaEventImpl
     */
    public void dispatch(ChibaEvent event) throws XFormsException {
	LOGGER.debug("dispatching " + event);
        this.eventLog.flush();
        String targetId = event.getId();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Event " + event.getEventName() + " dispatched");
            LOGGER.debug("Event target: " + targetId);
//            try 
//	    {
//                DOMUtil.prettyPrintDOM(this.chibaBean.getXMLContainer(), System.out);
//            } catch (TransformerException e) {
//                throw new XFormsException(e);
//            }
        }

        if (event.getEventName().equals(FluxFacade.FLUX_ACTIVATE_EVENT)) 
            chibaBean.dispatch(targetId, XFormsEventFactory.DOM_ACTIVATE);
        else if (event.getEventName().equals("SETINDEX")) {
            int position = Integer.parseInt((String) event.getContextInfo());
            Repeat repeat = (Repeat) this.chibaBean.lookup(targetId);
            repeat.setIndex(position);
        }
        else if (event.getEventName().equals("SETVALUE")) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Event contextinfo: " + event.getContextInfo());
            this.chibaBean.updateControlValue(targetId, (String) event.getContextInfo());
        }
        else if (event.getEventName().equalsIgnoreCase("http-request")) {
            // todo: make request handler member of web adapter
            HttpRequestHandler httpRequestHandler = new HttpRequestHandler(this.chibaBean);
            httpRequestHandler.execute(event);
        }
        else {
            throw new XFormsException("Unknown or illegal event type");
        }
    }

    /**
     * listen to processor and add a DefaultChibaEventImpl object to the
     * EventQueue.
     *
     * @param event the handled DOMEvent
     */
    public void handleEvent(Event event) 
    {
	LOGGER.debug("handleEvent(" + event + ")");
        try {
            if (event instanceof XFormsEvent) 
	    {
                XFormsEvent xformsEvent = (XFormsEvent) event;
                String type = xformsEvent.getType();
                if (XFormsEventFactory.CHIBA_REPLACE_ALL.equals(type)) 
                {
                    // get event context and store it in session
                    Map submissionResponse = new HashMap();
                    submissionResponse.put("header", xformsEvent.getContextInfo("header"));
                    submissionResponse.put("body", xformsEvent.getContextInfo("body"));
                    this.session.setAttribute(ChibaServlet.CHIBA_SUBMISSION_RESPONSE, submissionResponse);

                    // get event properties
                    Element target = (Element) event.getTarget();
                    String targetId = target.getAttributeNS(null, "id");
                    String targetName = target.getLocalName();

                    // add event properties to log
                    this.eventLog.add(type, targetId, targetName);
                }
                else 
                {
                    // add event to log
                    this.eventLog.add(xformsEvent);
                }
            }
        }
        catch (Exception e) {
	    System.out.println("**** " + e.getMessage());
	    LOGGER.debug("error " + e.getMessage() + " while handling event " + event);
            try {
                this.chibaBean.getContainer().handleEventException(e);
            }
            catch (XFormsException xfe) {
                xfe.printStackTrace();
            }
        }
    }


    /**
     * terminates the XForms processing. right place to do cleanup of
     * resources.
     *
     * @throws org.chiba.xml.xforms.exception.XFormsException
     */
    public void shutdown() throws XFormsException {
        try {
            // deregister for notification events
            this.root.removeEventListener(XFormsEventFactory.CHIBA_STATE_CHANGED, this, true);
            this.root.removeEventListener(XFormsEventFactory.CHIBA_PROTOTYPE_CLONED, this, true);
            this.root.removeEventListener(XFormsEventFactory.CHIBA_ID_GENERATED, this, true);
            this.root.removeEventListener(XFormsEventFactory.CHIBA_ITEM_INSERTED, this, true);
            this.root.removeEventListener(XFormsEventFactory.CHIBA_ITEM_DELETED, this, true);
            this.root.removeEventListener(XFormsEventFactory.CHIBA_INDEX_CHANGED, this, true);
            this.root.removeEventListener(XFormsEventFactory.CHIBA_SWITCH_TOGGLED, this, true);

            // shutdown processor
            this.chibaBean.shutdown();
            this.chibaBean = null;

            // deregister for interaction events
            this.root.removeEventListener(XFormsEventFactory.CHIBA_LOAD_URI, this, true);
            this.root.removeEventListener(XFormsEventFactory.CHIBA_RENDER_MESSAGE, this, true);
            this.root.removeEventListener(XFormsEventFactory.CHIBA_REPLACE_ALL, this, true);

            this.root = null;

            System.gc();
        }
        catch (Exception e) {
            throw new XFormsException(e);
        }
    }

    /**
     * set the upload location. This string represents the destination
     * (data-sink) for uploads.
     *
     * @param destination a String representing the location where to store
     * uploaded files/data.
     */
    public void setUploadDestination(String destination) {
        this.uploadDestination = destination;
        //todo: this should be moved to parent class. it's duplicated in both Adapters
        setContextParam(ServletAdapter.HTTP_UPLOAD_DIR, this.uploadDestination);
    }

    protected String escape(String string) {
        if (string == null) {
            return string;
        }

        StringBuffer buffer = new StringBuffer(string.length());
        char c;
        for (int index = 0; index < string.length(); index++) {
            c = string.charAt(index);
            switch (c) {
                case '\n':
                    buffer.append('\\').append('n');
                    break;
                case '\r':
                    buffer.append('\\').append('r');
                    break;
                case '\t':
                    buffer.append('\\').append('t');
                    break;
                case '\'':
                    buffer.append('\\').append('\'');
                    break;
                case '\"':
                    buffer.append('\\').append('\"');
                    break;
                default:
                    buffer.append(c);
                    break;
            }
        }

        return buffer.toString();
    }

}
// end of class
