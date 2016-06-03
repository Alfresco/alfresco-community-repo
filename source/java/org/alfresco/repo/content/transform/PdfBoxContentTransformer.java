package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.pdf.PDFParserConfig;

/**
 * Uses <a href="http://tika.apache.org/">Apache Tika</a> and
 *  <a href="@link http://pdfbox.apache.org/">Apache PDFBox</a> to perform
 *  conversions from PDF documents.
 * 
 * @author Nick Burch
 * @author Derek Hulley
 */
public class PdfBoxContentTransformer extends TikaPoweredContentTransformer
{
    protected PDFParserConfig pdfParserConfig;
    
    public PdfBoxContentTransformer() {
       super(new String[] {
             MimetypeMap.MIMETYPE_PDF
       });
    }

    @Override
    protected Parser getParser() {
       return new PDFParser();
    }
    
    /**
     * Sets the PDFParserConfig for inclusion in the ParseContext sent to the PDFBox parser,
     * useful for setting config like spacingTolerance.
     * 
     * @param pdfParserConfig
     */
    public void setPdfParserConfig(PDFParserConfig pdfParserConfig)
    {
        this.pdfParserConfig = pdfParserConfig;
    }

    @Override
    protected ParseContext buildParseContext(Metadata metadata, String targetMimeType, TransformationOptions options)
    {
        ParseContext context = super.buildParseContext(metadata, targetMimeType, options);
        if (pdfParserConfig != null)
        {
            context.set(PDFParserConfig.class, pdfParserConfig);
        }
        // TODO: Possibly extend TransformationOptions to allow for per-transform PDFParserConfig?
        return context;
    }
}
