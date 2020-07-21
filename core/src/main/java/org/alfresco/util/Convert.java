/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.encoding.CharactersetFinder;
import org.alfresco.encoding.GuessEncodingCharsetFinder;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;

/**
 * Utility to convert text files.
 * <p/>
 * Check the usage options with the <b>--help</b> option.
 * <p/>
 * Here are some examples of how to use the <code>main</code> method:
 * <ul>
 *    <li>
 *    <b>--help</b><br/>
 *    Produce the help output.
 *    </li>
 *    <li>
 *    <b>--dry-run --encoding=UTF-8 --line-ending=WINDOWS --match="(.java|.xml|.jsp|.properties)$" --ignore="(.svn|classes)" "w:\"</b><br/>
 *    Find all source (.java, .xml, .jsp and .properties) files in directory "w:\".<br/>
 *    List files and show which would change when converting to CR-LF (Windows) line endings.<br/>
 *    Where auto-detection of the file is ambiguous, assume UTF-8.
 *    </li>
 *    <li>
 *    <b>--encoding=UTF-8 --line-ending=WINDOWS --match="(.java|.xml|.jsp|.properties)$" --ignore="(.svn|classes)" "w:\"</b><br/>
 *    Find all source (.java, .xml, .jsp and .properties) files in directory "w:\".  Recurse into subdirectories.<br/>
 *    Convert files, where necessary, to have CR-LF (Windows) line endings.<br/>
 *    Where auto-detection of the file encoding is ambiguous, assume UTF-8.<br/>
 *    Backups (.bak) files will be created.
 *    </li>
 *    <li>
 *    <b>--svn-update --no-backup --encoding=UTF-8 --line-ending=WINDOWS --match="(.java|.xml|.jsp|.properties)$" "w:\"</b><br/>
 *    Issue a 'svn status' command on directory "w:\" and match the regular expressions given to find files.<br/>
 *    Convert files, where necessary, to have CR-LF (Windows) line endings.<br/>
 *    Where auto-detection of the file encoding is ambiguous, assume UTF-8.  Write out as UTF-8.<br/>
 *    No backups files will be created.
 *    </li>
 * </ul>
 * 
 * @author Derek Hulley
 */
public class Convert
{
    private static final String OPTION_HELP = "--help";
    private static final String OPTION_SVN_STATUS = "--svn-status";
    private static final String OPTION_MATCH = "--match=";
    private static final String OPTION_IGNORE = "--ignore=";
    private static final String OPTION_ENCODING= "--encoding=";
    private static final String OPTION_LINE_ENDING = "--line-ending=";
    private static final String OPTION_REPLACE_TABS= "--replace-tabs=";
    private static final String OPTION_NO_RECURSE = "--no-recurse";
    private static final String OPTION_NO_BACKUP = "--no-backup";
    private static final String OPTION_DRY_RUN = "--dry-run";
    private static final String OPTION_VERBOSE = "--verbose";
    private static final String OPTION_QUIET = "--quiet";
    
    private static final Set<String> OPTIONS = new HashSet<String>(13);
    
    static
    {
        OPTIONS.add(OPTION_HELP);
        OPTIONS.add(OPTION_SVN_STATUS);
        OPTIONS.add(OPTION_MATCH);
        OPTIONS.add(OPTION_IGNORE);
        OPTIONS.add(OPTION_ENCODING);
        OPTIONS.add(OPTION_LINE_ENDING);
        OPTIONS.add(OPTION_REPLACE_TABS);
        OPTIONS.add(OPTION_NO_RECURSE);
        OPTIONS.add(OPTION_NO_BACKUP);
        OPTIONS.add(OPTION_DRY_RUN);
        OPTIONS.add(OPTION_VERBOSE);
        OPTIONS.add(OPTION_QUIET);
    }
    
    /**
     * @see GuessEncodingCharsetFinder
     */
    private static final CharactersetFinder CHARACTER_ENCODING_FINDER = new GuessEncodingCharsetFinder();

    private File startDir = null;
    
    private boolean svnStatus = false;
    private boolean dryRun = false;
    private Pattern matchPattern = null;
    private Pattern ignorePattern = null;
    private Charset charset = null;
    private String lineEnding = null;
    private Integer replaceTabs = null;
    private boolean noRecurse = false;
    private boolean noBackup = false;
    private boolean verbose = false;
    private boolean quiet = false;
    
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            printUsage();
        }
        // Convert args to a list
        List<String> argList = new ArrayList<String>(args.length);
        List<String> argListFixed = Arrays.asList(args);
        argList.addAll(argListFixed);
        // Extract all the options
        Map<String, String> optionValues = extractOptions(argList);
        
        // Check for help request
        if (optionValues.containsKey(OPTION_HELP))
        {
            printUsage();
            System.exit(0);
        }
        
        // Check
        if (argList.size() != 1)
        {
            printUsage();
            System.exit(1);
        }
        
        // Get the directory to start in
        File startDir = new File(argList.get(0));
        if (!startDir.exists() || !startDir.isDirectory())
        {
            System.err.println("Convert: ");
            System.err.println("   Unable to find directory: " + startDir);
            System.err.flush();
            printUsage();
            System.exit(1);
        }
        
        Convert convert = new Convert(optionValues, startDir);
        convert.convert();
    }

    /**
     * Private constructor for use by the main method.
     */
    private Convert(Map<String, String> optionValues, File startDir)
    {
        this.startDir = startDir;
        
        svnStatus = optionValues.containsKey(OPTION_SVN_STATUS);
        dryRun = optionValues.containsKey(OPTION_DRY_RUN);
        String match = optionValues.get(OPTION_MATCH);
        String ignore = optionValues.get(OPTION_IGNORE);
        String encoding = optionValues.get(OPTION_ENCODING);
        lineEnding = optionValues.get(OPTION_LINE_ENDING);
        noRecurse = optionValues.containsKey(OPTION_NO_RECURSE);
        noBackup = optionValues.containsKey(OPTION_NO_BACKUP);
        verbose = optionValues.containsKey(OPTION_VERBOSE);
        quiet = optionValues.containsKey(OPTION_QUIET);
        
        // Check that the tab replacement count is correct
        String replaceTabsStr = optionValues.get(OPTION_REPLACE_TABS);
        if (replaceTabsStr != null)
        {
            try
            {
                replaceTabs = Integer.parseInt(replaceTabsStr);
            }
            catch (NumberFormatException e)
            {
                System.err.println("Convert: ");
                System.err.println("   Unable to determine how many spaces to replace tabs with: " + replaceTabsStr);
                System.err.flush();
                printUsage();
                System.exit(1);
            }
        }

        // Check the match regex expressions
        if (match == null)
        {
            match = ".*";
        }
        try
        {
            matchPattern = Pattern.compile(match);
        }
        catch (Throwable e)
        {
            System.err.println("Convert: ");
            System.err.println("   Unable to parse regular expression: " + match);
            System.err.flush();
            printUsage();
            System.exit(1);
        }
        // Check the match regex expressions
        if (ignore != null)
        {
            try
            {
                ignorePattern = Pattern.compile(ignore);
            }
            catch (Throwable e)
            {
                System.err.println("Convert: ");
                System.err.println("   Unable to parse regular expression: " + ignore);
                System.err.flush();
                printUsage();
                System.exit(1);
            }
        }
        // Check the encoding
        if (encoding != null)
        {
            try
            {
                charset = Charset.forName(encoding);
            }
            catch (Throwable e)
            {
                System.err.println("Convert: ");
                System.err.println("   Unknown encoding: " + encoding);
                System.err.flush();
                printUsage();
                System.exit(1);
            }
        }
        
        // Check line ending
        if (lineEnding != null && !lineEnding.equals("WINDOWS") && !lineEnding.equals("UNIX"))
        {
            System.err.println("Convert: ");
            System.err.println("   Line endings can be either WINDOWS or UNIX: " + lineEnding);
            System.err.flush();
            printUsage();
            System.exit(1);
        }
        
        // Check quiet/verbose match
        if (verbose && quiet)
        {
            System.err.println("Convert: ");
            System.err.println("   Cannot output in verbose and quiet mode.");
            System.err.flush();
            printUsage();
            System.exit(1);
        }
    }
    
    private void convert()
    {
        try
        {
            if (!quiet)
            {
                System.out.print("Converting files matching " + matchPattern);
                System.out.print(ignorePattern == null ? "" : " but not " + ignorePattern);
                System.out.println(dryRun ? " [DRY RUN]" : "");
            }
            if (!svnStatus)
            {
                // Do a recursive pattern match
                convertDir(startDir);
            }
            else
            {
                // Use SVN
                convertSvn(startDir);
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.err.flush();
            printUsage();
            System.exit(1);
        }
        finally
        {
            System.out.flush();
        }
    }
    
    private void convertSvn(File currentDir) throws Throwable
    {
        RuntimeExec exec = new RuntimeExec();
        exec.setCommand(new String[] {"svn", "status", currentDir.toString()});
        ExecutionResult result = exec.execute();
        if (!result.getSuccess())
        {
            System.out.println("svn status command failed:" + exec);
        }
        // Get the output
        String dump = result.getStdOut();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new StringReader(dump));
            while (true)
            {
                String line = reader.readLine();
                if (line == null)
                {
                    break;
                }
                // Only lines that start with "A" or "M"
                if (!line.startsWith("A") && !line.startsWith("M"))
                {
                    continue;
                }
                String filename = line.substring(7).trim();
                if (filename.length() < 1)
                {
                    continue;
                }
                File file = new File(filename);
                if (!file.exists())
                {
                    continue;
                }
                // We found one
                convertFile(file);
            }
        }
        finally
        {
            if (reader != null)
            {
                try { reader.close(); } catch (Throwable e) {}
            }
        }
    }
    
    /**
     * Recursive method to do the conversion work.
     */
    private void convertDir(File currentDir) throws Throwable
    {
        // Get all children of the folder
        File[] childFiles = currentDir.listFiles();
        for (File childFile : childFiles)
        {
            if (childFile.isDirectory())
            {
                if (noRecurse)
                {
                    // Don't enter the directory
                    continue;
                }
                // Recurse
                convertDir(childFile);
            }
            else
            {
                convertFile(childFile);
            }
        }
    }
    
    private void convertFile(File file) throws Throwable
    {
        // We have a file, but does the pattern match
        String filePath = file.getAbsolutePath();
        if (matchPattern.matcher(filePath).find())
        {
            // It matches, but must we ignore it?
            if (ignorePattern != null && ignorePattern.matcher(filePath).find())
            {
                // It is ignorable
                return;
            }
        }
        else
        {
            // It missed the primary positive match
            return;
        }
        
        // Ignore folders
        if (file.isDirectory())
        {
            return;
        }
        
        if (file.length() > (1024 * 1024))              // 1MB.  TODO: Make an option
        {
            System.out.println(" (Too big)");
        }
        File backupFile = null;
        try
        {
            // Read the source file into memory
            byte[] fileBytes = readFileIntoMemory(file);
            // Calculate the MD5 for the file
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(fileBytes);
            byte[] fileMd5 = md5.digest();
            // Guess the charset now
            Charset fileCharset = guessCharset(fileBytes, charset);
            
            byte[] convertedBytes = fileBytes;
            byte[] sourceBytes = fileBytes;
            byte[] convertedMd5 = fileMd5;

            // Convert the tabs
            if (replaceTabs != null)
            {
                sourceBytes = convertTabs(sourceBytes, fileCharset, replaceTabs);
            }
            // Convert the charset
            if (charset != null)
            {
                // TODO
                // sourceBytes = convert ...
            }
            // Convert the line endings
            if (lineEnding != null)
            {
                convertedBytes = convertLineEndings(sourceBytes, fileCharset, lineEnding);
            }
            boolean changed = false;
            if (convertedBytes == fileBytes)
            {
                // Nothing done
            }
            else
            {
                // Recalculate the converted MD5
                md5 = MessageDigest.getInstance("MD5");
                md5.update(convertedBytes);
                convertedMd5 = md5.digest();
                // Now compare
                changed = !Arrays.equals(fileMd5, convertedMd5);
            }
            // Make a backup of the file if it changed
            if (changed)
            {
                if (!noBackup && !dryRun)
                {
                    String backupFilename = file.getAbsolutePath() + ".bak";
                    File backupFilePre = new File(backupFilename);
                    // Write the original file contents to the backup file
                    writeMemoryIntoFile(fileBytes, backupFilePre);
                    // That being successful, we can now reference it
                    backupFile = backupFilePre;
                }
                if (!quiet)
                {
                    System.out.println("   " + file + " <Modified>");
                }
                // Only write to the file if this is not a dry run
                if (!dryRun)
                {
                    // Now write the converted buffer to the original file
                    writeMemoryIntoFile(convertedBytes, file);
                }
            }
            else
            {
                if (verbose)
                {
                    System.out.println("   " + file + " <No change>");
                }
            }
        }
        catch (Throwable e)
        {
            if (backupFile != null)
            {
                try
                {
                    file.delete();
                    backupFile.renameTo(file);
                }
                catch (Throwable ee)
                {
                    System.err.println("Failed to restore backup file: " + backupFile);
                    ee.printStackTrace();
                }
            }
            throw e;
        }
        finally
        {
            if (!quiet || verbose)
            {
                System.out.flush();
            }
        }
    }
    
    /**
     * Brute force guessing by doing charset conversions.<br/>
     */
    private static Charset guessCharset(byte[] bytes, Charset charset) throws Exception
    {
        Charset guessedCharset = CHARACTER_ENCODING_FINDER.detectCharset(bytes);
        if (guessedCharset == null)
        {
            return charset;
        }
        else
        {
            return guessedCharset;
        }
    }
    
    private static byte[] convertTabs(byte[] bytes, Charset charset, int replaceTabs) throws Exception
    {
        // The tab character
        char tab = '\t';
        char space = ' ';

        // The output
        StringBuilder sb = new StringBuilder(bytes.length);
        
        String charsetName = charset.name();
        // Using the charset, convert to a string
        String str = new String(bytes, charsetName);
        char[] chars = str.toCharArray();
        for (char c : chars)
        {
            if (c == tab)
            {
                // Replace the tab
                for (int i = 0; i < replaceTabs; i++)
                {
                    sb.append(space);
                }
            }
            else
            {
                sb.append(c);
            }
        }
        // Done
        return sb.toString().getBytes(charsetName);
    }

    private static final String EOF_CHECK = "--EOF-CHECK--";
    private static byte[] convertLineEndings(byte[] bytes, Charset charset, String lineEnding) throws Exception
    {
        String charsetName = charset.name();
        // Using the charset, convert to a string
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder(bytes.length);
        try
        {
            String str = new String(bytes, charsetName);
            str = str + EOF_CHECK;
            reader = new BufferedReader(new StringReader(str));
            String line = reader.readLine();
            while (line != null)
            {
                // Ignore the newline check
                boolean addLine = true;
                if (line.equals(EOF_CHECK))
                {
                    break;
                }
                else if (line.endsWith(EOF_CHECK))
                {
                    int index = line.indexOf(EOF_CHECK);
                    line = line.substring(0, index);
                    addLine = false;
                }
                // Write the line back out
                sb.append(line);
                if (!addLine)
                {
                    // No newline
                }
                else if (lineEnding.equalsIgnoreCase("UNIX"))
                {
                    sb.append("\n");
                }
                else
                {
                    sb.append("\r\n");
                }
                line = reader.readLine();
            }
        }
        finally
        {
            if (reader != null)
            {
                try { reader.close(); } catch (Throwable e) {}
            }
        }
        // Done
        return sb.toString().getBytes(charsetName);
    }
    
    private static byte[] readFileIntoMemory(File file) throws Exception
    {
        InputStream is = null;
        OutputStream os = null;
        try
        {
            is = new BufferedInputStream(new FileInputStream(file));
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            os = new BufferedOutputStream(baos);
            byte[] buffer = new byte[1024];
            while (true)
            {
                int count = is.read(buffer);
                if (count < 0)
                {
                    break;
                }
                os.write(buffer, 0, count);
            }
            os.flush();
            byte[] memory = baos.toByteArray();
            return memory;
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Throwable e) {}
            }
            if (os != null)
            {
                try { os.close(); } catch (Throwable e) {}
            }
        }
    }
    
    private static void writeMemoryIntoFile(byte[] bytes, File file) throws Exception
    {
        InputStream is = null;
        OutputStream os = null;
        try
        {
            is = new ByteArrayInputStream(bytes);
            os = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[1024];
            while (true)
            {
                int count = is.read(buffer);
                if (count < 0)
                {
                    break;
                }
                os.write(buffer, 0, count);
            }
            os.flush();
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Throwable e) {}
            }
            if (os != null)
            {
                try { os.close(); } catch (Throwable e) {}
            }
        }
    }
    
    /**
     * Extract all the options from the list of arguments.
     * @param args      the program arguments.  This list will be modified.
     * @return          Returns a map of arguments and their values.  Where the arguments have
     *                  no values, an empty string is returned.
     */
    private static Map<String, String> extractOptions(List<String> args)
    {
        Map<String, String> optionValues = new HashMap<String, String>(13);
        // Iterate until we find a non-option
        Iterator<String> iterator = args.iterator();
        while (iterator.hasNext())
        {
            String arg = iterator.next();
            boolean foundOption = false;
            for (String option : OPTIONS)
            {
                if (!arg.startsWith(option))
                {
                    // It is a non-option
                    continue;
                }
                foundOption = true;
                // We can remove the argument
                iterator.remove();
                // Check if the option needs a value
                if (option.endsWith("="))
                {
                    // Extract the option value
                    int index = arg.indexOf("=");
                    if (index == arg.length() - 1)
                    {
                        // There is nothing there, so we don't keep a value
                    }
                    else
                    {
                        String value = arg.substring(index + 1);
                        optionValues.put(option, value);
                    }
                }
                else
                {
                    // Add the value to the map
                    String value = "";
                    optionValues.put(option, value);
                }
            }
            if (!foundOption)
            {
                // It is not an option
                break;
            }
        }
        // Done
        return optionValues;
    }
    
    public static void printUsage()
    {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("Usage: \n")
          .append("   Convert [options] directory \n")
          .append("   \n")
          .append("      options: \n")
          .append("         --help \n")
          .append("            Print this help. \n")
          .append("         --svn-status \n")
          .append("            Execute a 'svn status' command against the directory and use the output for the file list. \n")
          .append("         --match=?: \n")
          .append("            A regular expression that all filenames must match. \n")
          .append("            This argument can be escaped with double quotes, ie.g \"[a-zA-z0-9 ]\". \n")
          .append("            The regular expression will be applied to the full path of the file. \n")
          .append("            Name seperators will be '/' on Unix and ''\\'' on Windows systems. \n")
          .append("            The default is \"--match=.*\", or match all files. \n")
          .append("         --ignore=?: \n")
          .append("            A regular expression that all filenames must not match. \n")
          .append("            This argument can be escaped with double quotes, ie.g \"[a-zA-z0-9 ]\". \n")
          .append("            The regular expression will be applied to the full path of the file. \n")
          .append("            Name seperators will be '/' on Unix and ''\\'' on Windows systems. \n")
          .append("            This option is not present by default. \n")
          .append("         --encoding=? \n")
          .append("            If not specified, the encoding of the files is left unchanged. \n")
          .append("            Typical values would be UTF-8, UTF-16 or any java-recognized encoding string. \n")
          .append("         --line-ending=? \n")
          .append("            This can either be WINDOWS or UNIX. \n")
          .append("            If not set, the line ending style is left unchanged. \n")
          .append("         --replace-tabs=? \n")
          .append("            Specify the number of spaces to insert in place of a tab. \n")
          .append("         --no-recurse \n")
          .append("            Do not recurse into subdirectories. \n")
          .append("         --no-backup \n")
          .append("            The default is to make a backup of all files prior to modification. \n")
          .append("            With this option, no backups are made. \n")
          .append("         --dry-run \n")
          .append("            Do not modify or backup any files. \n")
          .append("            No filesystem modifications are made. \n")
          .append("         --verbose \n")
          .append("            Dump all files checked to std.out. \n")
          .append("         --quiet \n")
          .append("            Don't dump anything to std.out. \n")
          .append("       directory: \n")
          .append("          The directory to start searching in. \n")
          .append("          If the directory has spaces in it, then escape it with double quotes, e.g. \"C:\\Program Files\" \n")
          .append("   \n")
          .append("Details of the modifications being made are written to std.out. \n")
          .append("Errors are written to std.err. \n")
          .append("When used without any options, this program will behave like a FIND. \n");
        System.out.println(sb);
        System.out.flush();
    }
}
