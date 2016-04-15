/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.opencmis;


public class PersistedContentStream {
//	implements ContentStream
//}
//{
//    private File tempFile = null;
//	private ContentStream stream;
//
//	public PersistedContentStream(ContentStream stream)
//	{
//		this.stream = stream;
//		copyToTempFile();
//	}
//
//	@Override
//	public List<CmisExtensionElement> getExtensions()
//	{
//		return stream.getExtensions();
//	}
//
//	@Override
//	public void setExtensions(List<CmisExtensionElement> extensions)
//	{
//		stream.setExtensions(extensions);
//	}
//
//	@Override
//	public long getLength()
//	{
//		return stream.getLength();
//	}
//
//	@Override
//	public BigInteger getBigLength()
//	{
//		return stream.getBigLength();
//	}
//
//	@Override
//	public String getMimeType()
//	{
//		return stream.getMimeType();
//	}
//
//	@Override
//	public String getFileName()
//	{
//		return stream.getFileName();
//	}
//	
//	@Override
//	public InputStream getStream()
//	{
//		try
//		{
//	        if(tempFile != null)
//	        {
//	        	InputStream stream = new BufferedInputStream(new FileInputStream(tempFile));
//				return stream;
//	        }
//	        else
//	        {
//	        	throw new CmisStorageException("Stream is null");
//	        }
//		}
//		catch (FileNotFoundException e)
//		{
//            throw new ContentIOException("Failed to copy content from input stream: \n" +
//                    "   writer: " + this,
//                    e);
//		}
//	}
//	
//    private void copyToTempFile()
//    {
//        int bufferSize = 40 * 1014;
//        long count = 0;
//
//        try
//        {
//            tempFile = TempFileProvider.createTempFile("cmis", "content");
//            if (stream.getStream() != null)
//            {
//                OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile), bufferSize);
//                InputStream in = new BufferedInputStream(stream.getStream(), bufferSize);
//
//                byte[] buffer = new byte[bufferSize];
//                int i;
//                while ((i = in.read(buffer)) > -1)
//                {
//                    out.write(buffer, 0, i);
//                    count += i;
//                }
//
//                in.close();
//                out.close();
//            }
//        }
//        catch (Exception e)
//        {
//        	cleanup();
//            throw new CmisStorageException("Unable to store content: " + e.getMessage(), e);
//        }
//
//        if (stream.getLength() > -1 && stream.getLength() != count)
//        {
//        	cleanup();
//            throw new CmisStorageException(
//                    "Expected " + stream.getLength() + " bytes but retrieved " + count + "bytes!");
//        }
//    }
//    
//    public void cleanup()
//    {
//        if (tempFile == null)
//        {
//            return;
//        }
//
//        try
//        {
//            tempFile.delete();
//        }
//        catch (Exception e)
//        {
//            // ignore - file will be removed by TempFileProvider
//        }
//    }
}
