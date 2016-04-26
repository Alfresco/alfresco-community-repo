package org.alfresco.email.server.impl.subetha;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.Part;

import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.email.EmailMessagePart;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.util.remote.RemotableInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @since 2.2
 */
public class SubethaEmailMessagePart implements EmailMessagePart
{
    private static final String ERR_UNSUPPORTED_ENCODING = "email.server.err.usupported_encoding";
    private static final String ERR_FAILED_TO_READ_CONTENT_STREAM = "email.server.err.failed_to_read_content_stream";
    private static final String ERR_INCORRECT_MESSAGE_PART = "email.server.err.incorrect_message_part";
    
    private static final long serialVersionUID = -8530238872199733096L;

    static final Log log = LogFactory.getLog(SubethaEmailMessagePart.class);

    private static final Pattern encodingExtractor = Pattern.compile("charset\\s*=[\\s\"]*([^\";\\s]*)");

    private String encoding;
    private String fileName;
    private int fileSize = -1;
    private String contentType;
    private InputStream contentInputStream;
    
    private String rmiRegistryHost;
    private int rmiRegistryPort;
    
    protected SubethaEmailMessagePart()
    {
        super();
    }

    /**
     * Object can be built on existing message part only.
     * 
     * @param messagePart Message part.
     */
    public SubethaEmailMessagePart(Part messagePart)
    {
        ParameterCheck.mandatory("messagePart", messagePart);

        try
        {
            fileSize = messagePart.getSize();
            fileName = messagePart.getFileName();
            contentType = messagePart.getContentType();

            Matcher matcher = encodingExtractor.matcher(contentType);
            if (matcher.find())
            {
                encoding = matcher.group(1);
                if (!Charset.isSupported(encoding))
                {
                    throw new EmailMessageException(ERR_UNSUPPORTED_ENCODING, encoding);
                }
            }

            try
            {
                contentInputStream = messagePart.getInputStream(); 
            }
            catch (Exception ex)
            {
                throw new EmailMessageException(ERR_FAILED_TO_READ_CONTENT_STREAM, ex.getMessage());
            }
        }
        catch (MessagingException e)
        {
            throw new EmailMessageException(ERR_INCORRECT_MESSAGE_PART, e.getMessage());
        }
    }

    public SubethaEmailMessagePart(Part messagePart, String fileName)
    {
        this(messagePart);
        this.fileName = fileName;
    }


    public InputStream getContent()
    {
        return contentInputStream;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public String getFileName()
    {
        return fileName;
    }

    public int getSize()
    {
        return fileSize;
    }


    public void setRmiRegistry(String rmiRegistryHost, int rmiRegistryPort)
    {
        this.rmiRegistryHost = rmiRegistryHost;
        this.rmiRegistryPort = rmiRegistryPort;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        contentInputStream = new RemotableInputStream(rmiRegistryHost, rmiRegistryPort, contentInputStream);
        out.defaultWriteObject();
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
    }
}
