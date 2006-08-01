package org.alfresco.web.templating.xforms.servlet;

import org.chiba.adapter.ChibaAdapter;
import org.chiba.xml.xforms.exception.XFormsException;
import org.apache.log4j.Category;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Returns a submission response exactly once.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: SubmissionResponseServlet.java,v 1.1 2005/12/21 22:59:27 unl Exp $
 */
public class SubmissionResponseServlet extends HttpServlet {
    private static Category LOGGER = Category.getInstance(SubmissionResponseServlet.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // lookup session
        HttpSession session = request.getSession(false);
        if (session != null) {
            // lookup attribute containing submission response map
            Map submissionResponse = (Map) session.getAttribute(ChibaServlet.CHIBA_SUBMISSION_RESPONSE);
            if (submissionResponse != null) {
                // shutdown form session
                ChibaAdapter adapter = (ChibaAdapter) session.getAttribute(ChibaServlet.CHIBA_ADAPTER);
                if (adapter != null) {
                    try {
                        adapter.shutdown();
                    }
                    catch (XFormsException e) {
                        LOGGER.error("xforms shutdown failed", e);
                    }
                }

                // remove session attributes
                session.removeAttribute(ChibaServlet.CHIBA_ADAPTER);
                session.removeAttribute(ChibaServlet.CHIBA_SUBMISSION_RESPONSE);

                // copy header fields
                Map headerMap = (Map) submissionResponse.get("header");
                Iterator iterator = headerMap.keySet().iterator();
                while (iterator.hasNext()) {
                    final String name = (String) iterator.next();
                    if (name.equalsIgnoreCase("Transfer-Encoding")) {
                        // Some servers (e.g. WebSphere) may set a "Transfer-Encoding"
                        // with the value "chunked". This may confuse the client since
                        // ChibaServlet output is not encoded as "chunked", so this
                        // header is ignored.
                        continue;
                    }

                    final String value = (String) headerMap.get(name);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("added header: " + name + "=" + value);
                    }

                    response.setHeader(name, value);
                }

                // copy body stream
                InputStream bodyStream = (InputStream) submissionResponse.get("body");
                OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
                for (int b = bodyStream.read(); b > -1; b = bodyStream.read()) {
                    outputStream.write(b);
                }

                // close streams
                bodyStream.close();
                outputStream.close();
            }
        }
	//        response.sendError(HttpServletResponse.SC_FORBIDDEN, "no submission response available");
    }
}
