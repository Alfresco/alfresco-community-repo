package org.alfresco.repo.content.transform;

import java.io.File;
import java.net.URLConnection;

import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;

/**
 * A version of {@link StringBean} which allows control of the
 *  encoding in the underlying HTML Parser.
 * Unfortunately, StringBean doesn't allow easy over-riding of
 *  this, so we have to duplicate some code to control this.
 * This allows us to correctly handle HTML files where the encoding
 *  is specified against the content property (rather than in the 
 *  HTML Head Meta), see ALF-10466 for details.
 */
class EncodingAwareStringBean extends StringBean
{
    private static final long serialVersionUID = -9033414360428669553L;

    /**
     * Sets the File to extract strings from, and the encoding
     *  it's in (if known to Alfresco)
     *   
     * @param file The File that text should be fetched from.
     * @param encoding The encoding of the input
     */
    public void setURL(File file, String encoding)
    {
        String previousURL = getURL();
        String newURL = file.getAbsolutePath();
        
        if ( (previousURL == null) || (!newURL.equals(previousURL)) )
        {
            try
            {
                URLConnection conn = getConnection();

                if (null == mParser)
                {
                    mParser = new Parser(newURL);
                }
                else
                {
                    mParser.setURL(newURL);
                }
                
                if (encoding != null)
                {
                    mParser.setEncoding(encoding);
                }
                
                mPropertySupport.firePropertyChange(PROP_URL_PROPERTY, previousURL, getURL());
                mPropertySupport.firePropertyChange(PROP_CONNECTION_PROPERTY, conn, mParser.getConnection());
                setStrings();
            }
            catch (ParserException pe)
            {
                updateStrings(pe.toString());
            }
        }
    }
    
    public String getEncoding(){
    	return mParser.getEncoding();
    }
}