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
