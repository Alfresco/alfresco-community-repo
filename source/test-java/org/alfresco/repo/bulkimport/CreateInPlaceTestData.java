/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.bulkimport;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.alfresco.util.GUID;
import org.apache.commons.io.FileUtils;

// 2051 6 6 6 6 6 100 /Users/steveglover/dev/mac/projects/HEAD/code/root/projects/repository/source/test-resources/quick /Users/steveglover/dev/mac/projects/HEAD/data/contentstore
/**
 * Creates content files in an existing content store.
 * 
 * Note: use with caution.
 * 
 * @since 4.0
 */
public class CreateInPlaceTestData
{
	private List<File> sourceFiles = new ArrayList<File>(50);
	private File targetFolder;
    private Random rand = new Random();
    
    private int startYear;
    private int maxYears;
    private int maxMonths;
    private int maxDays;
    private int maxHours;
    private int maxMinutes;
    private int maxFilesPerMinute;

	public CreateInPlaceTestData(int startYear, int maxYears, int maxMonths, int maxDays,
			int maxHours, int maxMinutes, int maxFilesPerMinute, String sourceFolderPath, String contentStore)
	{
		super();
		this.startYear = startYear;
		this.maxYears = maxYears;
		this.maxMonths = maxMonths;
		this.maxDays = maxDays;
		this.maxHours = maxHours;
		this.maxMinutes = maxMinutes;
		this.maxFilesPerMinute = maxFilesPerMinute;

		File sourceFolder = new File(sourceFolderPath);
		targetFolder = new File(contentStore);
		
		if(!sourceFolder.isDirectory())
		{
			throw new IllegalArgumentException("source is not a folder");
		}
		
		if(!targetFolder.isDirectory())
		{
			throw new IllegalArgumentException("target is not a folder");
		}

		initSourceFiles(sourceFolder);
	}

	private File getSourceFile()
	{
        int idx = rand.nextInt(sourceFiles.size());
        File f = sourceFiles.get(idx);
        return f;
	}

	private void createFile(File targetFolder) throws IOException
	{
		File f = new File(targetFolder, GUID.generate() + ".bin");
		FileUtils.copyFile(getSourceFile(), f);
	}
	
	private void createFiles(File targetFolder) throws IOException
	{
		for(int i = 0; i < maxFilesPerMinute; i++)
		{
			createFile(targetFolder);
		}
	}

	private void createDirectoryTree(File contentStore) throws IOException
	{
		for(int y = startYear; y < startYear + rand.nextInt(maxYears)+1; y++)
		{
			File year = new File(contentStore, String.valueOf(y));
			if(year.exists() || year.mkdir())
			{
				
				for(int m = 1; m <= rand.nextInt(maxMonths)+1; m++)
				{
					File month = new File(year, String.valueOf(m));
					if(month.exists() || month.mkdir())
					{

						
						for(int d = 1; d <= rand.nextInt(maxDays)+1; d++)
						{
							File day = new File(month, String.valueOf(d));
							if(day.exists() || day.mkdir())
							{

								
								
								for(int h = 1; h <= rand.nextInt(maxHours)+1; h++)
								{
									File hour = new File(day, String.valueOf(h));
									if(hour.exists() || hour.mkdir())
									{

										
										
										
										
										for(int mi = 1; mi <= rand.nextInt(maxMinutes)+1; mi++)
										{
											File minute = new File(hour, String.valueOf(mi));
											if(minute.exists() || minute.mkdir())
											{

												
												
												createFiles(minute);
												
												
												
											}
											else
											{
												System.err.println("Unable to create directory " + minute.getAbsolutePath());
											}
										}
										
										
										
										
									}
									else
									{
										System.err.println("Unable to create directory " + hour.getAbsolutePath());
									}
								}
								
								
								
								
								
							}
							else
							{
								System.err.println("Unable to create directory " + day.getAbsolutePath());
							}
						}
						
						
						
					}
					else
					{
						System.err.println("Unable to create directory " + month.getAbsolutePath());
					}
				}
				
			}
			else
			{
				System.err.println("Unable to create directory " + year.getAbsolutePath());
			}
		}
	}

	private void initSourceFiles(File sourceFolder)
	{
		for(File f : sourceFolder.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File file)
			{
				return !file.getName().startsWith(".");
			}
			
		}))
		{
			sourceFiles.add(f);
		}
	}
	
	public void execute() throws IOException
	{
		createDirectoryTree(targetFolder);
	}
	
	public static void main(String[] args)
	{
		try
		{
			new CreateInPlaceTestData(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]),
					Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), args[7], args[8]).execute();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
}
