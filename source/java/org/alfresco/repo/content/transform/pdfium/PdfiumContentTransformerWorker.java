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
package org.alfresco.repo.content.transform.pdfium;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformerHelper;
import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.PagedSourceOptions;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class PdfiumContentTransformerWorker extends ContentTransformerHelper
		implements ContentTransformerWorker, InitializingBean {

	/** options variable name */
	private static final String KEY_OPTIONS = "options";
	/** source variable name */
	private static final String VAR_SOURCE = "source";
	/** target variable name */
	private static final String VAR_TARGET = "target";

	private static final Log logger = LogFactory.getLog(PdfiumContentTransformerWorker.class);

	/** the system command executer */
	private RuntimeExec executer;

	/** the output from the check command */
	private String versionString;

	private boolean available;

	public PdfiumContentTransformerWorker() {
		this.available = false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (executer == null) {
			throw new AlfrescoRuntimeException("System runtime executer not set");
		}
		setAvailable(true);
	}
	
	 /**
     * Set the runtime command executer that must be executed in order to run
     * <b>ImageMagick</b>.  Whether or not this is the full path to the convertCommand
     * or just the convertCommand itself depends the environment setup.
     * <p>
     * The command must contain the variables <code>${source}</code> and
     * <code>${target}</code>, which will be replaced by the names of the file to
     * be transformed and the name of the output file respectively.
     * <pre>
     *    convert ${source} ${target}
     * </pre>
     *  
     * @param executer the system command executer
     */
    public void setExecuter(RuntimeExec executer)
    {
        executer.setProcessProperty(
                "PDFIUM_TMPDIR", TempFileProvider.getTempDir().getAbsolutePath());
        this.executer = executer;
    }
    
	/**
	 * @return Returns true if the transformer is functioning otherwise false
	 */
	@Override
	public boolean isAvailable() {
		return available;
	}

	/**
	 * Make the transformer available
	 * 
	 * @param available
	 *            boolean
	 */
	protected void setAvailable(boolean available) {
		this.available = available;
	}

	@Override
	public String getVersionString() {
		return this.versionString;
	}

	@Override
	public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
		if (!available) {
			return false;
		}

		// Add limited support (so lots of other transforms are not supported)
		// for PDF to PNG.
		if ((MimetypeMap.MIMETYPE_PDF.equals(sourceMimetype)
				|| MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR.equals(sourceMimetype))
				&& MimetypeMap.MIMETYPE_IMAGE_PNG.equals(targetMimetype)) {
			return true;
		}

		return false;
	}

	@Override
	public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception {
		// get mimetypes
		String sourceMimetype = getMimetype(reader);
		String targetMimetype = getMimetype(writer);

		// get the extensions to use
		MimetypeService mimetypeService = getMimetypeService();
		String sourceExtension = mimetypeService.getExtension(sourceMimetype);
		String targetExtension = mimetypeService.getExtension(targetMimetype);
		if (sourceExtension == null || targetExtension == null) {
			throw new AlfrescoRuntimeException("Unknown extensions for mimetypes: \n" + "   source mimetype: "
					+ sourceMimetype + "\n" + "   source extension: " + sourceExtension + "\n" + "   target mimetype: "
					+ targetMimetype + "\n" + "   target extension: " + targetExtension);
		}

		// create required temp files
		File sourceFile = TempFileProvider.createTempFile(getClass().getSimpleName() + "_source_",
				"." + sourceExtension);
		File targetFile = TempFileProvider.createTempFile(getClass().getSimpleName() + "_target_",
				"." + targetExtension);

		// pull reader file into source temp file
		reader.getContent(sourceFile);

		transformInternal(sourceFile, sourceMimetype, targetFile, targetMimetype, options);

		// check that the file was created
		if (!targetFile.exists() || targetFile.length() == 0) {
			throw new ContentIOException("Pdfium transformation failed to write output file");
		}
		// upload the output image
		writer.putContent(targetFile);
		// done
		if (logger.isDebugEnabled()) {
			logger.debug("Transformation completed: \n" + "   source: " + reader + "\n" + "   target: " + writer + "\n"
					+ "   options: " + options);
		}

	}

	/**
	 * Transform the pdf content from the source file to the target file
	 */

	private void transformInternal(File sourceFile, String sourceMimetype, File targetFile, String targetMimetype,
			TransformationOptions options) throws Exception {
		Map<String, String> properties = new HashMap<String, String>(5);
		// set properties
		if (options instanceof ImageTransformationOptions) {
			ImageTransformationOptions imageOptions = (ImageTransformationOptions)options;
            ImageResizeOptions resizeOptions = imageOptions.getResizeOptions();
            String commandOptions = imageOptions.getCommandOptions();
            if (commandOptions == null)
            {
                commandOptions = "";
            }
            
            if(resizeOptions.getHeight() > -1){
            	commandOptions += " --height=" + resizeOptions.getHeight();
            }
            if(resizeOptions.getWidth() > -1){
            	commandOptions += " --width=" + resizeOptions.getHeight();
            }
            if(resizeOptions.getAllowEnlargement()){
            	commandOptions += " --allow-enlargement";
            }
            if(resizeOptions.isMaintainAspectRatio()){
            	commandOptions += " --maintain-aspect-ratio";
            }
            commandOptions += " --page="+ getSourcePageRange(imageOptions, sourceMimetype, targetMimetype);
            
            properties.put(KEY_OPTIONS, commandOptions);
		}

		properties.put(VAR_SOURCE,
				sourceFile.getAbsolutePath());
		properties.put(VAR_TARGET, targetFile.getAbsolutePath());

		// execute the statement
		long timeoutMs = options.getTimeoutMs();
		RuntimeExec.ExecutionResult result = executer.execute(properties, timeoutMs);
		if (result.getExitValue() != 0 && result.getStdErr() != null && result.getStdErr().length() > 0) {
			throw new ContentIOException("Failed to perform Pdfium transformation: \n" + result);
		}
		// success
		if (logger.isDebugEnabled()) {
			logger.debug("Pdfium executed successfully: \n" + executer);
		}
	}

	@Override
	public String getComments(boolean available) {
		StringBuilder sb = new StringBuilder();
		sb.append("# Supports transformations between mimetypes");
		sb.append("# pdf or ai to png.\n");
		return sb.toString();
	}
	

	/**
	 * Determines whether or not a single page range is required for the given
	 * source and target mimetypes.
	 * 
	 * @param sourceMimetype
	 * @param targetMimetype
	 * @return whether or not a page range must be specified for the transformer
	 *         to read the target files
	 */
	private boolean isSingleSourcePageRangeRequired(String sourceMimetype, String targetMimetype) {
		return true;
	}

	/**
	 * Gets the page range from the source to use in the command line.
	 * 
	 * @param options
	 *            the transformation options
	 * @param sourceMimetype
	 *            the source mimetype
	 * @param targetMimetype
	 *            the target mimetype
	 * @return the source page range for the command line
	 */
	private String getSourcePageRange(TransformationOptions options, String sourceMimetype, String targetMimetype) {
		if (options instanceof ImageTransformationOptions) {
			ImageTransformationOptions imageOptions = (ImageTransformationOptions) options;
			PagedSourceOptions pagedSourceOptions = imageOptions.getSourceOptions(PagedSourceOptions.class);
			if (pagedSourceOptions != null) {
				if (pagedSourceOptions.getStartPageNumber() != null && pagedSourceOptions.getEndPageNumber() != null) {
					if (pagedSourceOptions.getStartPageNumber().equals(pagedSourceOptions.getEndPageNumber())) {
						return   "" + (pagedSourceOptions.getStartPageNumber() - 1) ;
					} else {
						if (isSingleSourcePageRangeRequired(sourceMimetype, targetMimetype)) {
							throw new AlfrescoRuntimeException(
									"A single page is required for targets of type " + targetMimetype);
						}
						return "" + (pagedSourceOptions.getStartPageNumber() - 1) + "-"
								+ (pagedSourceOptions.getEndPageNumber() - 1);
					}
				} else {
					// TODO specified start to end of doc and start of doc to
					// specified end not yet supported
					// Just grab a single page specified by either start or end
					if (pagedSourceOptions.getStartPageNumber() != null)
						return "" + (pagedSourceOptions.getStartPageNumber() - 1) ;
					if (pagedSourceOptions.getEndPageNumber() != null)
						return "" + (pagedSourceOptions.getEndPageNumber() - 1) ;
				}
			}
		}
		if (options.getPageLimit() == 1 || isSingleSourcePageRangeRequired(sourceMimetype, targetMimetype)) {
			return "0";
		} else {
			return "";
		}
	}

}
