package org.alfresco.repo.web.scripts.transfer;

import java.io.IOException;
import java.io.StringWriter;

import org.alfresco.service.cmr.transfer.TransferException;
import org.springframework.extensions.webscripts.json.JSONWriter;

public class TransferProcessorUtil
{
    public static String writeError(TransferException ex) throws IOException
    {
        StringWriter stringWriter = new StringWriter(300);
        JSONWriter jsonWriter = new JSONWriter(stringWriter);
        jsonWriter.startObject();
        jsonWriter.writeValue("errorId", ex.getMsgId());
        jsonWriter.startValue("errorParams");
        jsonWriter.startArray();
        stringWriter.write(writeErrorParams(ex.getMsgParams()));
        jsonWriter.endArray();
        jsonWriter.endObject();
        return stringWriter.toString();
    }

    /**
     * @param stringWriter
     * @param msgParams
     */
    public static String writeErrorParams(Object[] msgParams)
    {
        if (msgParams == null) return "";
        StringWriter writer = new StringWriter(300);
        boolean first = true;
        for (Object param : msgParams) {
            if (!first) {
                writer.write(",");
            }
            if (param != null) {
                writer.write("\"");
                writer.write(JSONWriter.encodeJSONString(param.toString()));
                writer.write("\"");
            } else {
                writer.write("null");
            }
            first = false;
        }
        return writer.toString();
    }


}
