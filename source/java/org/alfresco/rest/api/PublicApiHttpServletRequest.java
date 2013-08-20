package org.alfresco.rest.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class PublicApiHttpServletRequest extends HttpServletRequestWrapper
{
	public PublicApiHttpServletRequest(HttpServletRequest request) throws IOException
	{
		super(getWrappedHttpServletRequest(request));
	}

	public void resetInputStream() throws IOException
	{
		ServletInputStream stream = getInputStream();
		stream.reset();
	}
	
	private static HttpServletRequest getWrappedHttpServletRequest(HttpServletRequest request) throws IOException
	{
		final PublicApiServletInputStream sis = new PublicApiServletInputStream(request.getInputStream());
		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request)
		{
			public ServletInputStream getInputStream() throws java.io.IOException
			{
				return sis;
			}
		};
		return wrapper;
	}
	
	private static class PublicApiServletInputStream extends ServletInputStream
	{
		private BufferedInputStream in;

		PublicApiServletInputStream(InputStream in)
		{
			this.in = new BufferedInputStream(in);
			this.in.mark(8096);
		}

		@Override
		public int read() throws IOException
		{
			return in.read();
		}

		@Override
		public void reset() throws IOException
		{
			in.reset();
		}
	}
}
