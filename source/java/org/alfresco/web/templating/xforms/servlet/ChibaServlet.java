package org.alfresco.web.templating.xforms.servlet;

import org.alfresco.web.app.Application;
import org.alfresco.web.templating.xforms.DojoGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.Cookie;
import org.chiba.adapter.ChibaAdapter;
import org.alfresco.web.templating.xforms.flux.FluxAdapter;
import org.alfresco.web.templating.TemplatingService;
import org.chiba.tools.xslt.StylesheetLoader;
import org.chiba.tools.xslt.UIGenerator;
import org.chiba.tools.xslt.XSLTGenerator;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.connector.http.AbstractHTTPConnector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The ChibaServlet handles all interactions between client and
 * form-processor (ChibaBean) for the whole lifetime of a form-filling session.
 * <br>
 * The Processor will be started through a Get-request from the client
 * pointing to the desired form-container. The Processor instance will
 * be stored in a Session-object.<br>
 * <br>
 * All further interaction will be handled through Post-requests.
 * Incoming request params will be mapped to data and action-handlers.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @author William Boyd
 * @version $Id: ChibaServlet.java,v 1.14 2005/12/21 22:59:27 unl Exp $
 */
public class ChibaServlet extends HttpServlet {
    //init-params
    private static Log logger = LogFactory.getLog(ChibaServlet.class); 

    private static final String FORM_PARAM_NAME = "form";
    private static final String XSL_PARAM_NAME = "xslt";
    private static final String CSS_PARAM_NAME = "css";
    private static final String ACTIONURL_PARAM_NAME = "action_url";

    public static final String CHIBA_ADAPTER = "chiba.adapter";
    public static final String CHIBA_UI_GENERATOR = "chiba.ui.generator";
    public static final String CHIBA_SUBMISSION_RESPONSE = "chiba.submission.response";

    /*
     * It is not thread safe to modify these variables once the
     * init(ServletConfig) method has been called
     */
    // the absolute path to the Chiba config-file
    protected String configPath = null;

    // the rootdir of this app; forms + documents fill be searched under this root
    protected String contextRoot = null;

    // where uploaded files are stored
    protected String uploadDir = null;

    protected String stylesPath = null;

    protected String agent;


    /**
     * Returns a short description of the servlet.
     *
     * @return - Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Servlet Controller for Chiba XForms Processor";
    }

    /**
     * Destroys the servlet.
     */
    public void destroy() {
    }

    /**
     * Initializes the servlet.
     *
     * @param config - the ServletConfig object
     * @throws javax.servlet.ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        logger.info("--------------- initing ChibaServlet... ---------------");
        //read some params from web-inf
        contextRoot = getServletConfig().getServletContext().getRealPath("");
        if (contextRoot == null)
            contextRoot = getServletConfig().getServletContext().getRealPath(".");

        //get the relative path to the chiba config-file
        String path = getServletConfig().getInitParameter("chiba.config");

        //get the real path for the config-file
        if (path != null) {
            configPath = getServletConfig().getServletContext().getRealPath(path);
        }

        //get the path for the stylesheets
        path = getServletConfig().getServletContext().getInitParameter("chiba.xforms.stylesPath");

        //get the real path for the stylesheets and configure a new StylesheetLoader with it
        if (path != null) {
            stylesPath = getServletConfig().getServletContext().getRealPath(path);
            logger.info("stylesPath: " + stylesPath);
        }

        //uploadDir = contextRoot	+ "/" + getServletConfig().getServletContext().getInitParameter("chiba.upload");
        uploadDir = getServletConfig().getServletContext().getInitParameter("chiba.upload");

        //Security constraint
        if (uploadDir != null) {
            if (uploadDir.toUpperCase().indexOf("WEB-INF") >= 0) {
                throw new ServletException("Chiba security constraint: uploadDir '" + uploadDir + "' not allowed");
            }
        }

        //user-agent mappings
	agent = getServletConfig().getServletContext().getInitParameter("chiba.useragent.ajax.path");
    }

    /**
     * Starts a new form-editing session.<br>
     * <p/>
     * The default value of a number of settings can be overridden as follows:
     * <p/>
     * 1. The uru of the xform to be displayed can be specified by using a param name of 'form' and a param value
     * of the location of the xform file as follows, which will attempt to load the current xforms file.
     * <p/>
     * http://localhost:8080/chiba-0.9.3/XFormsServlet?form=/forms/hello.xhtml
     * <p/>
     * 2. The uru of the CSS file used to style the form can be specified using a param name of 'css' as follows:
     * <p/>
     * http://localhost:8080/chiba-0.9.3/XFormsServlet?form=/forms/hello.xhtml&css=/chiba/my.css
     * <p/>
     * 3. The uri of the XSLT file used to generate the form can be specified using a param name of 'xslt' as follows:
     * <p/>
     * http://localhost:8080/chiba-0.9.3/XFormsServlet?form=/forms/hello.xhtml&xslt=/chiba/my.xslt
     * <p/>
     * 4. Besides these special params arbitrary other params can be passed via the GET-string and will be available
     * in the context map of ChibaBean. This means they can be used as instance data (with the help of ContextResolver)
     * or to set params for URI resolution.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     * @see org.chiba.xml.xforms.connector.context.ContextResolver
     * @see org.chiba.xml.xforms.connector.ConnectorFactory
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
	throws ServletException, IOException {

        ChibaAdapter adapter = null;
        HttpSession session = request.getSession(true);
	try {
	    if (request.getParameter("xxx") != null)
	    {
		response.setContentType("text/xml");
		Writer out = response.getWriter();
		adapter = (ChibaAdapter)session.getAttribute(CHIBA_ADAPTER);
		TemplatingService.getInstance().writeXML(adapter.getXForms(), out);
		out.close();
	    }
	    else
	    {
		logger.info("--------------- new XForms session ---------------");
		//kill everything that may have lived before
		session.removeAttribute(CHIBA_ADAPTER);
		session.removeAttribute(CHIBA_UI_GENERATOR);

		// determine Form to load
		String formURI = /*getRequestURI(request) +*/ request.getParameter(FORM_PARAM_NAME);
		logger.info("formURI: " + formURI);
		String xslFile = request.getParameter(XSL_PARAM_NAME);
		String css = request.getParameter(CSS_PARAM_NAME);
		String actionURL = getActionURL(request, response,true);
		logger.info("setting up adapeter");

		//setup Adapter
		adapter = setupAdapter(new FluxAdapter(session), session, formURI);
		setContextParams(request, adapter);
		storeCookies(request, adapter);
		adapter.init();

		if (load(adapter, response)) return;
		if (replaceAll(adapter, response)) return;

		//            response.setContentType("text/html");
		//	    PrintWriter out = response.getWriter();

		logger.info("generating ui");

		UIGenerator uiGenerator = createUIGenerator(request, 
							    response, 
							    actionURL, 
							    xslFile, 
							    css);
		uiGenerator.setInputNode(adapter.getXForms());
		//            uiGenerator.setOutput(out);
		uiGenerator.generate();

		//store adapter in session
		session.setAttribute(CHIBA_ADAPTER, adapter);
		session.setAttribute(CHIBA_UI_GENERATOR,uiGenerator);

		//            out.close();
		logger.info("done!");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    shutdown(adapter, session, e, response, request);
	}
    }

    /**
     * configures the an Adapter for interacting with the XForms processor (ChibaBean). The Adapter itself
     * will create the XFormsProcessor (ChibaBean) and configure it for processing.
     *
     * If you'd like to use a different source of XForms documents e.g. DOM you should extend this class and
     * overwrite this method. Please take care to also set the baseURI of the processor to a reasonable value
     * cause this will be the fundament for all URI resolutions taking place.
     *
     * @param adapter  the ChibaAdapter implementation to setup
     * @param session   - the Servlet session
     * @param formPath  - the relative location where forms are stored
     * @return ServletAdapter
     */
    protected ChibaAdapter setupAdapter(ChibaAdapter adapter,
					HttpSession session,
					String formPath) 
	throws XFormsException, URISyntaxException {

	adapter.createXFormsProcessor();

	if ((configPath != null) && !(configPath.equals(""))) {
	    adapter.setConfigPath(configPath);
	}
	adapter.setXForms(new URI(formPath));
	adapter.setBaseURI(formPath);
	adapter.setUploadDestination(uploadDir);

	Map servletMap = new HashMap();
	servletMap.put(ChibaAdapter.SESSION_ID, session.getId());
	adapter.setContextParam(ChibaAdapter.SUBMISSION_RESPONSE, servletMap);

	return adapter;
    }

    /**
     * stores cookies that may exist in request and passes them on to processor for usage in
     * HTTPConnectors. Instance loading and submission then uses these cookies. Important for
     * applications using auth.
     *
     * @param request the servlet request
     * @param adapter the Chiba adapter instance
     */
    protected void storeCookies(HttpServletRequest request,ChibaAdapter adapter){
	javax.servlet.http.Cookie[] cookiesIn = request.getCookies();
	if (cookiesIn != null) {
	    Cookie[] commonsCookies = new org.apache.commons.httpclient.Cookie[cookiesIn.length];
	    for (int i = 0; i < cookiesIn.length; i += 1) {
		javax.servlet.http.Cookie c = cookiesIn[i];
		Cookie newCookie = new Cookie(c.getDomain(),
					      c.getName(),
					      c.getValue(),
					      c.getPath(),
					      c.getMaxAge(),
					      c.getSecure());
		commonsCookies[i] = newCookie;
	    }
	    adapter.setContextParam(AbstractHTTPConnector.REQUEST_COOKIE,commonsCookies);
	}
    }

    /**
     *
     * creates and configures the UI generating component.
     * @param request
     * @param actionURL
     * @param xslFile
     * @param css
     * @return
     * @throws XFormsException
     */
    protected UIGenerator createUIGenerator(HttpServletRequest request,
					    HttpServletResponse response,
					    String actionURL,
					    String xslFile,
					    String css) 
	throws XFormsException {
//        StylesheetLoader stylesheetLoader = new StylesheetLoader(stylesPath);
//        if (xslFile != null){
//            stylesheetLoader.setStylesheetFile(xslFile);
//        }
	UIGenerator uiGenerator = new DojoGenerator(request, response);

	//set parameters
	uiGenerator.setParameter("contextroot",request.getContextPath());
	uiGenerator.setParameter("action-url",actionURL);
	uiGenerator.setParameter("debug-enabled", String.valueOf(logger.isDebugEnabled()));
	String selectorPrefix = 
	    Config.getInstance().getProperty(HttpRequestHandler.SELECTOR_PREFIX_PROPERTY,
					     HttpRequestHandler.SELECTOR_PREFIX_DEFAULT);
	uiGenerator.setParameter("selector-prefix", selectorPrefix);
	String removeUploadPrefix = 
	    Config.getInstance().getProperty(HttpRequestHandler.REMOVE_UPLOAD_PREFIX_PROPERTY,
					     HttpRequestHandler.REMOVE_UPLOAD_PREFIX_DEFAULT);
	uiGenerator.setParameter("remove-upload-prefix", removeUploadPrefix);
	if (css != null)
	    uiGenerator.setParameter("css-file", css);
	String dataPrefix = Config.getInstance().getProperty("chiba.web.dataPrefix");
	uiGenerator.setParameter("data-prefix", dataPrefix);

	String triggerPrefix = Config.getInstance().getProperty("chiba.web.triggerPrefix");
	uiGenerator.setParameter("trigger-prefix", triggerPrefix);

	uiGenerator.setParameter("user-agent", request.getHeader("User-Agent"));

	//	uiGenerator.setParameter("scripted","true");

	return uiGenerator;
    }

    /**
     * this method is responsible for passing all context information needed by the Adapter and Processor from
     * ServletRequest to ChibaContext. Will be called only once when the form-session is inited (GET).
     *
     * @param request           the ServletRequest
     * @param chibaAdapter    the ChibaAdapter to use
     */
    protected void setContextParams(HttpServletRequest request, ChibaAdapter chibaAdapter) {

	//[1] pass user-agent to Adapter for UI-building
	chibaAdapter.setContextParam(ServletAdapter.USERAGENT, request.getHeader("User-Agent"));

	//[2] read any request params that are *not* Chiba params and pass them into the context map
	Enumeration params = request.getParameterNames();
	while (params.hasMoreElements()) {
	    String s = (String) params.nextElement();
	    //store all request-params we don't use in the context map of ChibaBean
	    if (!(s.equals(FORM_PARAM_NAME) ||
		  s.equals(XSL_PARAM_NAME) ||
		  s.equals(CSS_PARAM_NAME) ||
		  s.equals(ACTIONURL_PARAM_NAME))) {
		String value = request.getParameter(s);
		//servletAdapter.setContextProperty(s, value);
		chibaAdapter.setContextParam(s, value);
		if (logger.isDebugEnabled()) {
		    logger.debug("added request param '" + s + "' added to context");
		}
	    }
	}
    }

    /**
     * @deprecated should be re-implemented using chiba events on adapter
     */
    protected boolean load(ChibaAdapter adapter, HttpServletResponse response) throws XFormsException, IOException {
	if (adapter.getContextParam(ChibaAdapter.LOAD_URI) != null) {
	    String redirectTo = (String) adapter.removeContextParam(ChibaAdapter.LOAD_URI);
	    adapter.shutdown();
	    response.sendRedirect(response.encodeRedirectURL(redirectTo));
	    return true;
	}
	return false;
    }

    /**
     * @deprecated should be re-implemented using chiba events on adapter
     */
    protected boolean replaceAll(ChibaAdapter chibaAdapter, HttpServletResponse response)
	throws XFormsException, IOException {
	if (chibaAdapter.getContextParam(ChibaAdapter.SUBMISSION_RESPONSE) != null) {
	    Map forwardMap = (Map) chibaAdapter.removeContextParam(ChibaAdapter.SUBMISSION_RESPONSE);
	    if (forwardMap.containsKey(ChibaAdapter.SUBMISSION_RESPONSE_STREAM)) {
		forwardResponse(forwardMap, response);
		chibaAdapter.shutdown();
		return true;
	    }
	}
	return false;
    }

    private String getActionURL(HttpServletRequest request, HttpServletResponse response, boolean scripted) {
	String defaultActionURL = getRequestURI(request) + agent;
	String encodedDefaultActionURL = response.encodeURL(defaultActionURL);
	int sessIdx = encodedDefaultActionURL.indexOf(";jsession");
	String sessionId = null;
	if (sessIdx > -1) {
	    sessionId = encodedDefaultActionURL.substring(sessIdx);
	}
	String actionURL = request.getParameter(ACTIONURL_PARAM_NAME);
	if (null == actionURL) {
	    actionURL = encodedDefaultActionURL;
	} else if (null != sessionId) {
	    actionURL += sessionId;
	}

	logger.info("actionURL: " + actionURL);
	// encode the URL to allow for session id rewriting
	return response.encodeURL(actionURL);
    }

    private String getRequestURI(HttpServletRequest request){
	StringBuffer buffer = new StringBuffer(request.getScheme());
	buffer.append("://");
	buffer.append(request.getServerName());
	buffer.append(":");
	buffer.append(request.getServerPort()) ;
	buffer.append(request.getContextPath());
	return buffer.toString();
    }

    private void forwardResponse(Map forwardMap, HttpServletResponse response) throws IOException {
	// fetch response stream
	InputStream responseStream = (InputStream) forwardMap.remove(ChibaAdapter.SUBMISSION_RESPONSE_STREAM);

	// copy header information
	Iterator iterator = forwardMap.keySet().iterator();
	while (iterator.hasNext()) {
        	
	    String name = (String) iterator.next();
            
	    if ("Transfer-Encoding".equalsIgnoreCase(name)) {
		// Some servers (e.g. WebSphere) may set a "Transfer-Encoding"
		// with the value "chunked". This may confuse the client since
		// ChibaServlet output is not encoded as "chunked", so this
		// header is ignored.
		continue;
	    }
	    String value = (String) forwardMap.get(name);
	    response.setHeader(name, value);
	}

	// copy stream content
	OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
	for (int b = responseStream.read();
	     b > -1;
	     b = responseStream.read()) {
	    outputStream.write(b);
	}

	// close streams
	responseStream.close();
	outputStream.close();
    }

    protected void shutdown(ChibaAdapter chibaAdapter,
			    HttpSession session,
			    Exception e,
			    HttpServletResponse response,
			    HttpServletRequest request) 
	throws IOException,
	ServletException {
	// attempt to shutdown processor
	if (chibaAdapter != null) {
	    try {
		chibaAdapter.shutdown();
	    } catch (XFormsException xfe) {
		xfe.printStackTrace();
	    }
	}
	Application.handleServletError(this.getServletContext(),
				       request,
				       response,
				       e,
				       logger,
				       null);
    }
}

// end of class
