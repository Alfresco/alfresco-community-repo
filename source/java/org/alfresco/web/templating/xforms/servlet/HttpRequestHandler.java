package org.alfresco.web.templating.xforms.servlet;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.log4j.Category;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.events.XFormsEventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.ui.Repeat;
import org.chiba.adapter.ChibaEvent;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Default implementation for handling http servlet requests.
 *
 * @author joern turner
 * @version $Id: HttpRequestHandler.java,v 1.7 2005/10/27 23:10:31 joernt Exp $
 */
public class HttpRequestHandler {
    private static final Category LOGGER = Category.getInstance(HttpRequestHandler.class);
    public static final String DATA_PREFIX_PROPERTY = "chiba.web.dataPrefix";
    public static final String TRIGGER_PREFIX_PROPERTY = "chiba.web.triggerPrefix";
    public static final String SELECTOR_PREFIX_PROPERTY = "chiba.web.selectorPrefix";
    public static final String REMOVE_UPLOAD_PREFIX_PROPERTY = "chiba.web.removeUploadPrefix";
    public static final String DATA_PREFIX_DEFAULT = "d_";
    public static final String TRIGGER_PREFIX_DEFAULT = "t_";
    public static final String SELECTOR_PREFIX_DEFAULT = "s_";
    public static final String REMOVE_UPLOAD_PREFIX_DEFAULT = "ru_";

    private ChibaBean chibaBean;

    private String dataPrefix;
    private String selectorPrefix;
    private String triggerPrefix;
    private String removeUploadPrefix;
    private String uploadRoot;


    public HttpRequestHandler(ChibaBean chibaBean) {
        this.chibaBean = chibaBean;
    }

    /**
     * executes this handler.
     *
     * @throws XFormsException
     */
    public void execute(ChibaEvent event) throws XFormsException {
        //HttpServletRequest request = (HttpServletRequest) this.chibaBean.getContext().get(ServletAdapter.HTTP_SERVLET_REQUEST);
        HttpServletRequest request= (HttpServletRequest) event.getContextInfo();
        
        String contextRoot = request.getSession().getServletContext().getRealPath("");
        if (contextRoot == null) {
            contextRoot = request.getSession().getServletContext().getRealPath(".");
        }

        String uploadDir = (String) this.chibaBean.getContext().get(ServletAdapter.HTTP_UPLOAD_DIR);
        this.uploadRoot = new File(contextRoot, uploadDir).getAbsolutePath();

        handleRequest(request);
    }

    /**
     * checks whether we have multipart or urlencoded request and processes it accordingly. After updating
     * the data, a reacalculate, revalidate refresh sequence is fired and the found trigger is executed.
     *
     * @param request Servlet request
     * @throws org.chiba.xml.xforms.exception.XFormsException
     *          todo: implement action block behaviour
     */
    protected void handleRequest(HttpServletRequest request) throws XFormsException {
        String trigger = null;

        // Check that we have a file upload request
        boolean isMultipart = FileUpload.isMultipartContent(request);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("request isMultipart: " + isMultipart);
            LOGGER.debug("base URI: " + this.chibaBean.getBaseURI());
            LOGGER.debug("user agent: " + request.getHeader("User-Agent"));
        }

        if (isMultipart) {
            trigger = processMultiPartRequest(request, trigger);
        } else {
            trigger = processUrlencodedRequest(request, trigger);
        }

        // finally activate trigger if any
        if (trigger != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("trigger '" + trigger + "'");
            }

            this.chibaBean.dispatch(trigger, XFormsEventFactory.DOM_ACTIVATE);
        }
    }

    /**
     * @param request Servlet request
     * @param trigger Trigger control
     * @return the calculated trigger
     * @throws XFormsException If an error occurs
     */
    protected String processMultiPartRequest(HttpServletRequest request, String trigger) throws XFormsException {
        DiskFileUpload upload = new DiskFileUpload();

        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }

        upload.setRepositoryPath(this.uploadRoot);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("root dir for uploads: " + this.uploadRoot);
        }

        List items;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException fue) {
            throw new XFormsException(fue);
        }

        Map formFields = new HashMap();
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            String itemName = item.getName();
            String fieldName = item.getFieldName();
            String id = fieldName.substring(Config.getInstance().getProperty("chiba.web.dataPrefix").length());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Multipart item name is: " + itemName
                        + " and fieldname is: " + fieldName
                        + " and id is: " + id);
                LOGGER.debug("Is formfield: " + item.isFormField());
            }

            if (item.isFormField()) {

                // check for upload-remove action
                if (fieldName.startsWith(getRemoveUploadPrefix())) {
                    id = fieldName.substring(getRemoveUploadPrefix().length());
                    // if data is null, file will be removed ...
                    // TODO: remove the file from the disk as well
                    chibaBean.updateControlValue(id, "", "", null);
                    continue;
                }

                // It's a field name, it means that we got a non-file
                // form field. Upload is not required. We must treat it as we
                // do in processUrlencodedRequest()
                processMultipartParam(formFields, fieldName, item, encoding);
            } else {

                String uniqueFilename = new File(getUniqueParameterName("file"),
                        new File(itemName).getName()).getPath();

                File savedFile = new File(this.uploadRoot, uniqueFilename);

                byte[] data = null;

                data = processMultiPartFile(item, id, savedFile, encoding, data);

                // if data is null, file will be removed ...
                // TODO: remove the file from the disk as well
                chibaBean.updateControlValue(id, item.getContentType(),
                        itemName, data);
            }
            
            // handle regular fields
            if (formFields.size() > 0) {

                Iterator it = formFields.keySet().iterator();
                while (it.hasNext()) {

                    fieldName = (String) it.next();
                    String[] values = (String[]) formFields.get(fieldName);
                    
                    // [1] handle data
                    handleData(fieldName, values);

                    // [2] handle selector
                    handleSelector(fieldName, values[0]);

                    // [3] handle trigger
                    trigger = handleTrigger(trigger, fieldName);
                }
            }
        }

        return trigger;
    }

    protected String processUrlencodedRequest(HttpServletRequest request, String trigger) throws XFormsException {
        // iterate request parameters
        Enumeration names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String paramName = names.nextElement().toString();
            String[] values = request.getParameterValues(paramName);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(this + " parameter-name: " + paramName);
                for (int i = 0; i < values.length; i++) {
                    LOGGER.debug(this + " value: " + values[i]);
                }
            }

            // [1] handle data
            handleData(paramName, values);

            // [2] handle selector
            handleSelector(paramName, values[0]);

            // [3] handle trigger
            trigger = handleTrigger(trigger, paramName);
        }
        return trigger;
    }

    /**
     * @param name
     * @throws XFormsException
     */
    protected void handleData(String name, String[] values)
            throws XFormsException {
        if (name.startsWith(getDataPrefix())) {
            String id = name.substring(getDataPrefix().length());

            // assemble new control value
            String newValue;

            if (values.length > 1) {
                StringBuffer buffer = new StringBuffer(values[0]);

                for (int i = 1; i < values.length; i++) {
                    buffer.append(" ").append(values[i]);
                }

                newValue = trim( buffer.toString() );
            } else {
                newValue = trim( values[0] );
            }

            this.chibaBean.updateControlValue(id, newValue);
        }
    }

    /**
     * patch to handle linefeed duplication in textareas with some browsers.
     *
     * @param value the value where linebreaks will be trimmed
     * @return returns a cleaned up version of the value
     */

    protected String trim(String value) {
        if (value != null && value.length() > 0) {
            value = value.replaceAll("\r\n", "\r");
            value = value.trim();
        }
        return value;
    }

    /**
     * @param name
     * @throws XFormsException
     */
    protected void handleSelector(String name, String value) throws XFormsException {
        if (name.startsWith(getSelectorPrefix())) {
            int separator = value.lastIndexOf(':');

            String id = value.substring(0, separator);
            int index = Integer.valueOf(value.substring(separator + 1)).intValue();

            Repeat repeat = (Repeat) this.chibaBean.lookup(id);
            repeat.setIndex(index);
        }
    }

    protected String handleTrigger(String trigger, String name) {
        if ((trigger == null) && name.startsWith(getTriggerPrefix())) {
            String parameter = name;
            int x = parameter.lastIndexOf(".x");
            int y = parameter.lastIndexOf(".y");

            if (x > -1) {
                parameter = parameter.substring(0, x);
            }

            if (y > -1) {
                parameter = parameter.substring(0, y);
            }

            // keep trigger id
            trigger = name.substring(getTriggerPrefix().length());
        }
        return trigger;
    }

    private byte[] processMultiPartFile(FileItem item, String id, File savedFile, String encoding, byte[] data)
            throws XFormsException {
        // some data uploaded ...
        if (item.getSize() > 0) {

            if (chibaBean.storesExternalData(id)) {

                // store data to file and create URI
                try {
                    savedFile.getParentFile().mkdir();
                    item.write(savedFile);
                } catch (Exception e) {
                    throw new XFormsException(e);
                }
                // content is URI in this case
                try {
                    data = savedFile.toURI().toString().getBytes(encoding);
                } catch (UnsupportedEncodingException e) {
                    throw new XFormsException(e);
                }

            } else {
                // content is the data
                data = item.get();
            }
        }
        return data;
    }

    private void processMultipartParam(Map formFields, String fieldName, FileItem item, String encoding) throws XFormsException {
        String values[] = (String[]) formFields.get(fieldName);
        String formFieldValue = null;
        try {
            formFieldValue = item.getString(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new XFormsException(e.getMessage(), e);
        }

        if (values == null) {
            formFields.put(fieldName, new String[]{formFieldValue});
        } else {
            // not very effective, but not many duplicate values
            // expected either ...
            String[] tmp = new String[values.length + 1];
            System.arraycopy(values, 0, tmp, 0, values.length);
            tmp[values.length] = formFieldValue;
            formFields.put(fieldName, tmp);
        }
    }


    /**
     * returns the prefix which is used to identify trigger parameters.
     *
     * @return the prefix which is used to identify trigger parameters
     */
    protected final String getTriggerPrefix() {
        if (this.triggerPrefix == null) {
            try {
                this.triggerPrefix =
                        Config.getInstance().getProperty(TRIGGER_PREFIX_PROPERTY, TRIGGER_PREFIX_DEFAULT);
            } catch (Exception e) {
                this.triggerPrefix = TRIGGER_PREFIX_DEFAULT;
            }
        }

        return this.triggerPrefix;
    }

    protected final String getDataPrefix() {
        if (this.dataPrefix == null) {
            try {
                this.dataPrefix = Config.getInstance().getProperty(DATA_PREFIX_PROPERTY, DATA_PREFIX_DEFAULT);
            } catch (Exception e) {
                this.dataPrefix = DATA_PREFIX_DEFAULT;
            }
        }

        return this.dataPrefix;
    }

    protected final String getRemoveUploadPrefix() {
        if (this.removeUploadPrefix == null) {
            try {
                this.removeUploadPrefix = Config.getInstance().getProperty(REMOVE_UPLOAD_PREFIX_PROPERTY, REMOVE_UPLOAD_PREFIX_DEFAULT);
            } catch (Exception e) {
                this.removeUploadPrefix = REMOVE_UPLOAD_PREFIX_DEFAULT;
            }
        }

        return this.removeUploadPrefix;
    }


    private String getUniqueParameterName(String prefix) {
        return prefix + Integer.toHexString((int) (Math.random() * 10000));
    }

    /**
     * returns the configured prefix which identifies 'selector' parameters. These are used to transport
     * the state of repeat indices via http.
     *
     * @return the prefix for selector parameters from the configuration
     */
    public final String getSelectorPrefix() {
        if (this.selectorPrefix == null) {
            try {
                this.selectorPrefix =
                        Config.getInstance().getProperty(SELECTOR_PREFIX_PROPERTY,
                                SELECTOR_PREFIX_DEFAULT);
            } catch (Exception e) {
                this.selectorPrefix = SELECTOR_PREFIX_DEFAULT;
            }
        }

        return this.selectorPrefix;
    }

    /**
     * Get the value of chibaBean.
     *
     * @return the value of chibaBean
     */
    public ChibaBean getChibaBean() {
        return this.chibaBean;
    }

}

// end of class


