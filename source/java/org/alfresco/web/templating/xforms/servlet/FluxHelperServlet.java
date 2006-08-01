package org.alfresco.web.templating.xforms.servlet;

import org.apache.commons.fileupload.FileUpload;
import org.apache.log4j.Category;
import org.chiba.adapter.ChibaAdapter;
import org.chiba.adapter.ChibaEvent;
import org.chiba.adapter.DefaultChibaEventImpl;
import org.chiba.xml.xforms.config.Config;
import org.chiba.tools.xslt.UIGenerator;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Provides extra functionality that's not easily handled with AJAX. This helper servlet will only be triggered in
 * in one of two situations:<br>
 * 1. an upload<br>
 * for an file upload to happen the browser has to submit the file as multipart-request. In this case the browser
 * form must be submitted cause there's no way to get the file content from javascript to send an AJAX request.<br><br>
 * 2. for a submission replace="all"<br>
 * This mode requires that the response will be directly streamed back to the client, replacing the existing viewport.
 * To maintain the correct location of that response in the location bar of the browser there seems no way but to
 * also let the browser do the request/response handling itself by the use of a normal form submit.
 *
 * @author Joern Turner
 * @version $Version: $
 */
public class FluxHelperServlet extends ChibaServlet {
    //init-params
    private static Category cat = Category.getInstance(FluxHelperServlet.class);


    /**
     * Returns a short description of the servlet.
     *
     * @return - Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Ajax Servlet Controller for Chiba XForms Processor";
    }

    /**
     * Destroys the servlet.
     */
    public void destroy() {
    }

    /**
     * handles all interaction with the user during a form-session.
     *
     * Note: this method is only triggered if the
     * browser has javascript turned off.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        ChibaAdapter chibaAdapter =null;

        response.setContentType("text/html");

        try {
            chibaAdapter = (ChibaAdapter) session.getAttribute(CHIBA_ADAPTER);
            if (chibaAdapter == null) {
                throw new ServletException(Config.getInstance().getErrorMessage("session-invalid"));
            }
            ChibaEvent chibaEvent = new DefaultChibaEventImpl();
            chibaEvent.initEvent("http-request",null,request);
            chibaAdapter.dispatch(chibaEvent);

            boolean isUpload = FileUpload.isMultipartContent(request);

            if(isUpload){
                ServletOutputStream out = response.getOutputStream();
                out.println("<html><head><title>status</title></head><body><div id='upload-status-ok' style='width:10px;height:10px;background:green'>&nbsp;</div></body></html>");
                out.close();
            }else{
                if(!replaceAll(chibaAdapter, response)){
                    UIGenerator uiGenerator = (UIGenerator) session.getAttribute(CHIBA_UI_GENERATOR);
                    uiGenerator.setInputNode(chibaAdapter.getXForms());
                    uiGenerator.setOutput(response.getWriter());
                    uiGenerator.generate();
                    response.getWriter().close();
                }
            }
        } catch (Exception e) {
            shutdown(chibaAdapter, session, e, response, request);
        }
    }
}

// end of class
