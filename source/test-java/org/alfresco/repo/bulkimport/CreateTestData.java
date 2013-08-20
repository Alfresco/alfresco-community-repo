package org.alfresco.repo.bulkimport;

import java.io.File;
import java.io.IOException;

import org.alfresco.util.GUID;
import org.apache.commons.io.FileUtils;

public class CreateTestData
{

	private void createFile(File sourceFile, File targetFolder) throws IOException
	{
		File f = new File(targetFolder, GUID.generate() + ".bin");
		FileUtils.copyFile(sourceFile, f);
	}

	private void createDirectory(File sourceFolder, File targetFolder) throws IOException
	{
		String s = sourceFolder.getName();
		if(s != null && !s.equals(""))
		{
//		String path = sourceFolder.getPath();
//		int i = path.lastIndexOf(File.separatorChar);
//		if(i != -1)
//		{
//			String s = path.substring(i);
			File d = new File(targetFolder, s);
			if(d.exists() || d.mkdir())
			{
				createTestData(sourceFolder, d);
			}
			else
			{
				System.err.println("Unable to create directory " + d.getAbsolutePath());
			}
		}
	}

	public void createTestData(File sourceFolder, File targetFolder) throws IOException
	{
		File[] files = sourceFolder.listFiles();
		for(File file : files)
		{
			if(file.getName().startsWith("."))
			{
				continue;
			}
			if(file.isDirectory())
			{
				createDirectory(file, targetFolder);
//				String path = file.getAbsolutePath();
//				int i = path.lastIndexOf(File.separatorChar);
//				if(i != -1)
//				{
//					String s = path.substring(i);
//					File d = new File(targetFolder, s);
//					if(d.mkdir())
//					{
//						createTestData(file, d);
//					}
//					else
//					{
//						System.err.println("Unable to create directory " + d.getAbsolutePath());
//					}
//				}
			}
			else
			{
				createFile(file, targetFolder);
			}
		}
	}
	
	public void execute(String sourceFolderPath, String targetFolderPath) throws IOException
	{
		File targetFolder = new File(targetFolderPath);
		File sourceFolder = new File(sourceFolderPath);
		
		if(!sourceFolder.isDirectory())
		{
			throw new IllegalArgumentException("source is not a folder");
		}
		
		if(!targetFolder.isDirectory())
		{
			throw new IllegalArgumentException("target is not a folder");
		}

		createDirectory(sourceFolder, targetFolder);
//		int i = sourceFolderPath.lastIndexOf(File.separatorChar);
//		if(i != -1)
//		{
//			String s = sourceFolderPath.substring(i);
//			File d = new File(targetFolder, s);
//			if(d.mkdir())
//			{
//				createTestData(sourceFolder, d);
//			}
//		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			new CreateTestData().execute(args[0], args[1]);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
}
