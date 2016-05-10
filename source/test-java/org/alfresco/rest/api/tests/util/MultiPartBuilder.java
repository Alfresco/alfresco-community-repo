/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *  This file is part of Alfresco
 *
 *   Alfresco is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Alfresco is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.rest.api.tests.util;


import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * <i><b>multipart/form-data</b></i> builder.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class MultiPartBuilder
{
    private FileData fileData;
    private String siteId;
    private String containerId;
    private String destination;
    private String uploadDirectory;
    private String updateNodeRef;
    private String description;
    private String contentTypeQNameStr;
    private List<String> aspects = Collections.emptyList();
    private Boolean majorVersion;
    private Boolean overwrite;
    private Boolean autoRename;
    private String nodeType;
    private Map<String, String> properties = Collections.emptyMap();

    private MultiPartBuilder()
    {
    }

    private MultiPartBuilder(MultiPartBuilder that)
    {
        this.fileData = that.fileData;
        this.siteId = that.siteId;
        this.containerId = that.containerId;
        this.destination = that.destination;
        this.uploadDirectory = that.uploadDirectory;
        this.updateNodeRef = that.updateNodeRef;
        this.description = that.description;
        this.contentTypeQNameStr = that.contentTypeQNameStr;
        this.aspects = new ArrayList<>(that.aspects);
        this.majorVersion = that.majorVersion;
        this.overwrite = that.overwrite;
        this.autoRename = that.autoRename;
        this.nodeType = that.nodeType;
        this.properties = new HashMap<>(that.properties);
    }

    public static MultiPartBuilder create()
    {
        return new MultiPartBuilder();
    }

    public static MultiPartBuilder copy(MultiPartBuilder copy)
    {
        return new MultiPartBuilder(copy);
    }

    public MultiPartBuilder setFileData(FileData fileData)
    {
        this.fileData = fileData;
        return this;
    }

    public MultiPartBuilder setSiteId(String siteId)
    {
        this.siteId = siteId;
        return this;
    }

    public MultiPartBuilder setContainerId(String containerId)
    {
        this.containerId = containerId;
        return this;
    }

    public MultiPartBuilder setDestination(String destination)
    {
        this.destination = destination;
        return this;
    }

    public MultiPartBuilder setUploadDirectory(String uploadDirectory)
    {
        this.uploadDirectory = uploadDirectory;
        return this;
    }

    public MultiPartBuilder setUpdateNoderef(String updateNodeRef)
    {
        this.updateNodeRef = updateNodeRef;
        return this;
    }

    public MultiPartBuilder setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public MultiPartBuilder setContentTypeQNameStr(String contentTypeQNameStr)
    {
        this.contentTypeQNameStr = contentTypeQNameStr;
        return this;
    }

    public MultiPartBuilder setAspects(List<String> aspects)
    {
        this.aspects = aspects;
        return this;
    }

    public MultiPartBuilder setMajorVersion(boolean majorVersion)
    {
        this.majorVersion = Boolean.valueOf(majorVersion);
        return this;
    }

    public MultiPartBuilder setOverwrite(boolean overwrite)
    {
        this.overwrite = Boolean.valueOf(overwrite);
        return this;
    }

    public MultiPartBuilder setAutoRename(boolean autoRename)
    {
        this.autoRename = Boolean.valueOf(autoRename);
        return this;
    }

    public MultiPartBuilder setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
        return this;
    }

    public MultiPartBuilder setProperties(Map<String, String> properties)
    {
        this.properties = properties;
        return this;
    }

    private String getAspects(List<String> aspects)
    {
        if (!aspects.isEmpty())
        {
            StringBuilder sb = new StringBuilder(aspects.size() * 2);
            for (String str : aspects)
            {
                sb.append(str).append(',');
            }

            sb.deleteCharAt(sb.length() - 1); // remove leading separator

            return sb.toString();

        }
        return null;
    }

    public static class FileData
    {
        private final String fileName;
        private final File file;
        private final String mimetype;
        private final String encoding;

        public FileData(String fileName, File file, String mimetype)
        {
            this(fileName, file, mimetype, null);
        }

        public FileData(String fileName, File file, String mimetype, String encoding)
        {
            this.fileName = fileName;
            this.file = file;
            this.mimetype = mimetype;
            this.encoding = encoding;
        }

        public String getFileName()
        {
            return fileName;
        }

        public File getFile()
        {
            return file;
        }

        public String getMimetype()
        {
            return mimetype;
        }

        public String getEncoding()
        {
            return encoding;
        }
    }

    public static class MultiPartRequest
    {
        private final byte[] body;
        private final String contentType;
        private final long contentLength;

        public MultiPartRequest(byte[] body, String contentType, long contentLength)
        {
            this.body = body;
            this.contentType = contentType;
            this.contentLength = contentLength;
        }

        public byte[] getBody()
        {
            return body;
        }

        public String getContentType()
        {
            return contentType;
        }

        public long getContentLength()
        {
            return contentLength;
        }
    }

    public MultiPartRequest build() throws IOException
    {
        List<Part> parts = new ArrayList<>();

        if (fileData != null)
        {
            FilePart fp = new FilePart("filedata", fileData.getFileName(), fileData.getFile(), fileData.getMimetype(), null);
            // Get rid of the default values added upon FilePart instantiation
            fp.setCharSet(fileData.getEncoding());
            fp.setContentType(fileData.getMimetype());
            parts.add(fp);
            addPartIfNotNull(parts, "filename", fileData.getFileName());
        }
        addPartIfNotNull(parts, "siteid", siteId);
        addPartIfNotNull(parts, "containerid", containerId);
        addPartIfNotNull(parts, "destination", destination);
        addPartIfNotNull(parts, "uploaddirectory", uploadDirectory);
        addPartIfNotNull(parts, "updatenoderef", updateNodeRef);
        addPartIfNotNull(parts, "description", description);
        addPartIfNotNull(parts, "contenttype", contentTypeQNameStr);
        addPartIfNotNull(parts, "aspects", getAspects(aspects));
        addPartIfNotNull(parts, "majorversion", majorVersion);
        addPartIfNotNull(parts, "overwrite", overwrite);
        addPartIfNotNull(parts, "autoRename", autoRename);
        addPartIfNotNull(parts, "nodeType", nodeType);

        if (!properties.isEmpty())
        {
            for (Entry<String, String> prop : properties.entrySet())
            {
                parts.add(new StringPart(prop.getKey(), prop.getValue()));
            }
        }

        MultipartRequestEntity req = new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), new HttpMethodParams());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        req.writeRequest(os);

        return new MultiPartRequest(os.toByteArray(), req.getContentType(), req.getContentLength());
    }

    private void addPartIfNotNull(List<Part> list, String partName, Object partValue)
    {
        if (partValue != null)
        {
            list.add(new StringPart(partName, partValue.toString()));
        }
    }
}
