package org.alfresco.web.templating.xforms.servlet;

import org.apache.log4j.Category;
import org.chiba.adapter.AbstractChibaAdapter;
import org.chiba.adapter.ChibaEvent;
import org.chiba.xml.xforms.events.XFormsEvent;
import org.chiba.xml.xforms.events.XFormsEventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import java.util.Map;

/**
 * integrates XForms Processor into Web-applications and handles request
 * processing. This is the default implementation of ChibaAdapter and besides
 * handling the interaction it also manages a UIGenerator to build the rendered
 * output for the browser.
 *
 * @author joern turner
 * @version $Id: ServletAdapter.java,v 1.8 2005/12/15 11:45:38 unl Exp $
 */
public class ServletAdapter extends AbstractChibaAdapter implements EventListener {

    private static final Category LOGGER = Category.getInstance(ServletAdapter.class);
    public static final String HTTP_SERVLET_REQUEST = "chiba.web.request";
    //public static final String HTTP_SESSION_OBJECT = "chiba.web.session";
    public static final String HTTP_UPLOAD_DIR = "chiba.web.uploadDir";

    //private ChibaBean chibaBean = null;
    //private String formURI = null;
    //private String actionUrl = null;
    //private String CSSFile = null;
    //private String stylesheet = null;
    //private UIGenerator generator = null;
    //private String stylesheetPath = null;
    //private HashMap context = null;
    public static final String USERAGENT = "chiba.useragent";
    private HttpRequestHandler handler;
    public static final Object XSLT_PATH = "xslt-path";

    /**
     * Creates a new ServletAdapter object.
     */
    public ServletAdapter() {
    }

    /**
     * place to put application-specific params or configurations before
     * actually starting off the XFormsProcessor. It's the responsibility of
     * this method to call chibaBean.init() to finish up the processor setup.
     *
     * @throws XFormsException If an error occurs
     */
    public void init() throws XFormsException {
        this.chibaBean.init();
        this.handler = getNewInteractionHandler();
    }


    /**
     * ServletAdapter knows and executes only one ChibaEvent: 'http-request'
     * which will contain the HttpServletRequest as contextInfo.
     *
     * @param event only events of type 'http-request' will be handled
     * @throws XFormsException
     */
    public void dispatch(ChibaEvent event) throws XFormsException {
        if (event.getEventName().equals("http-request")) {
            this.handler.execute(event);
        }
        else {
            LOGGER.warn("unknown event: '" + event.getEventName() + "' - ignoring");
        }

    }

    /**
     * terminates the XForms processing. right place to do cleanup of
     * resources.
     *
     * @throws org.chiba.xml.xforms.exception.XFormsException
     */
    public void shutdown() throws XFormsException {
        this.chibaBean.shutdown();
    }

    /**
     * Instructs the application environment to forward the given response.
     *
     * @param response a map containing at least a response stream and optional
     * header information.
     */
    public void forward(Map response) {
        this.chibaBean.getContext().put(SUBMISSION_RESPONSE, response);
    }

    // todo: should be set by servlet

    /**
     * return a new InteractionHandler.
     * <p/>
     * This method returns a new HttpRequestHandler.
     *
     * @return returns a new
     */
    protected HttpRequestHandler getNewInteractionHandler()
            throws XFormsException {
        return new HttpRequestHandler(this.chibaBean);
    }

    public void setUploadDestination(String uploadDir) {
        super.setUploadDestination(uploadDir);
        //HttpRequestHandler uses this
        // todo: should be a member of request handler and set directly
        setContextParam(HTTP_UPLOAD_DIR, uploadDir);
    }

    // event handling
    // todo: should be moved up to base class

    /**
     * This method is called whenever an event occurs of the type for which the
     * <code> EventListener</code> interface was registered.
     *
     * @param event The <code>Event</code> contains contextual information about
     * the event. It also contains the <code>stopPropagation</code> and
     * <code>preventDefault</code> methods which are used in determining the
     * event's flow and default action.
     */
    public void handleEvent(Event event) {
        String type = event.getType();
        String targetId = ((Element) event.getTarget()).getAttributeNS(null, "id");
        XFormsEvent xformsEvent = (XFormsEvent) event;

        if (XFormsEventFactory.CHIBA_LOAD_URI.equals(type)) {
            handleLoadURI(targetId, (String) xformsEvent.getContextInfo("uri"), (String) xformsEvent.getContextInfo("show"));
            return;
        }
        if (XFormsEventFactory.CHIBA_RENDER_MESSAGE.equals(type)) {
            handleMessage(targetId, (String) xformsEvent.getContextInfo("message"), (String) xformsEvent.getContextInfo("level"));
            return;
        }
        if (XFormsEventFactory.CHIBA_REPLACE_ALL.equals(type)) {
            handleReplaceAll(targetId, (Map) xformsEvent.getContextInfo("header"), xformsEvent.getContextInfo("body"));
            return;
        }

        // unknown event ignored
    }

    // todo: *either* move up these three methods as abstract template methods *or* use event log ?
    public void handleLoadURI(String targetId, String uri, String show) {
        // todo
    }

    public void handleMessage(String targetId, String message, String level) {
        // todo
    }

    public void handleReplaceAll(String targetId, Map header, Object body) {
        // todo
    }
}

// end of class
