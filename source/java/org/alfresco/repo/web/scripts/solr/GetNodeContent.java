package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.web.scripts.content.StreamContent;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * 
 * A web service to return the text content (transformed if required) of a node's
 * content property.
 * 
 * @since 4.0
 *
 */
public class GetNodeContent extends StreamContent
{
    private static final Log logger = LogFactory.getLog(GetNodeContent.class);

    /**
     * format definied by RFC 822, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3 
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

    private NodeDAO nodeDAO;
    private NodeService nodeService;
    private ContentService contentService;

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        ContentReader textReader = null;
        Exception transformException = null;
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

        String nodeIDString = templateVars.get("nodeId");
        if(nodeIDString == null)
        {
            throw new WebScriptException("nodeID parameter is required for GetNodeContent");
        }
        long nodeId = Long.valueOf(nodeIDString).longValue();

        String propertyQName = templateVars.get("propertyQName");
        QName propertyName = null;
        if(propertyQName == null)
        {
            propertyName = ContentModel.PROP_CONTENT;
        }
        else
        {
            propertyName = QName.createQName(propertyQName);
        }
        Pair<Long, NodeRef> pair = nodeDAO.getNodePair(nodeId);
        if(pair == null)
        {
            throw new WebScriptException("Node id does not exist");
        }
        NodeRef nodeRef = pair.getSecond();

        // check If-Modified-Since header and set Last-Modified header as appropriate
        Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        long modifiedSince = -1;
        String modifiedSinceStr = req.getHeader("If-Modified-Since");
        if(modifiedSinceStr != null)
        {
            try
            {
                modifiedSince = dateFormat.parse(modifiedSinceStr).getTime();
            }
            catch (Throwable e)
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("Browser sent badly-formatted If-Modified-Since header: " + modifiedSinceStr);
                }
            }
            
            if (modifiedSince > 0L)
            {
                // round the date to the ignore millisecond value which is not supplied by header
                long modDate = (modified.getTime() / 1000L) * 1000L;
                if (modDate <= modifiedSince)
                {
                    res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
            }
        }

        ContentReader reader = contentService.getReader(nodeRef, propertyName);
        if(reader == null)
        {
            res.setStatus(HttpStatus.SC_NO_CONTENT);
            return;            
        }

        // optimisation - already text so just return the content directly
        // TODO - better way of doing this?
        if(reader.getMimetype().startsWith("text/"))
        {
            textReader = reader;
        }
        else
        {
            ContentWriter writer = contentService.getTempWriter();
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
    
            TransformationOptions options = new TransformationOptions();
            ContentTransformer transformer = contentService.getTransformer(reader.getMimetype(), MimetypeMap.MIMETYPE_TEXT_PLAIN);
            if(transformer == null)
            {
                res.setStatus(HttpStatus.SC_NO_CONTENT);
                return;
            }


            try
            {
                // TODO how to ensure UTF-8?
                transformer.transform(reader, writer);
            }
            catch (ContentIOException e)
            {
                transformException = e;
            }

            if(transformException == null)
            {
                // point the reader to the new-written content
                textReader = writer.getReader();
                // Check that the reader is a view onto something concrete
                if (textReader == null || !textReader.exists())
                {
                    transformException = new ContentIOException("The transformation did not write any content, yet: \n"
                            + "   transformer:     " + transformer + "\n" + "   temp writer:     " + writer);
                }
            }
        }

        if(transformException != null)
        {
            res.setHeader("XAlfresco-transformException", transformException.getMessage());
            res.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        else
        {
            streamContentImpl(req, res, textReader, false, modified, String.valueOf(modified.getTime()), null);            
        }
        
        /*        writer.addListener(new ContentStreamListener()
        {

            @Override
            public void contentStreamClosed() throws ContentIOException
            {
                // TODO Auto-generated method stub

            }

        });*/
    }
}
