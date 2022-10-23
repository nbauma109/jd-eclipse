/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.ide.eclipse.editors;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.core.SourceMapper;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.printer.LineNumberStringBuilderPrinter;
import org.jd.core.v1.util.StringConstants;
import org.jd.ide.eclipse.JavaDecompilerPlugin;
import org.jd.ide.eclipse.util.loader.DirectoryLoader;
import org.jd.ide.eclipse.util.loader.ZipLoader;

import jd.core.preferences.Preferences;


/**
 * JDSourceMapper
 * 
 * @project Java Decompiler Eclipse Plugin
 * @version 0.1.4
 * @see     org.eclipse.jdt.internal.core.SourceMapper
 */
public class JDSourceMapper extends SourceMapper {
	private static final String JAVA_SOURCE_SUFFIX        = ".java";
	private static final int    JAVA_SOURCE_SUFFIX_LENGTH = 5;

	private static final ClassFileToJavaSourceDecompiler DECOMPILER = new ClassFileToJavaSourceDecompiler();
	
	private File basePath;
	
	private LineNumberStringBuilderPrinter printer = new LineNumberStringBuilderPrinter();
	
	@SuppressWarnings("rawtypes")
	public JDSourceMapper(File basePath, IPath sourcePath, String sourceRootPath, Map options) {	
		super(sourcePath, sourceRootPath, options);
		this.basePath = basePath;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public char[] findSource(String javaSourcePath) {
		char[] source = null;
		
		// Search source file
		if (this.rootPaths == null) {
			source = super.findSource(javaSourcePath);
		} else {
			Iterator iterator = this.rootPaths.iterator();
			
			while (iterator.hasNext() && (source == null)) {
				String sourcesRootPath = (String)iterator.next();				
				source = super.findSource(sourcesRootPath + IPath.SEPARATOR + javaSourcePath);
			}
		}
		
		if ((source == null) && javaSourcePath.toLowerCase().endsWith(JAVA_SOURCE_SUFFIX)) {	
			String internalTypeName = javaSourcePath.substring(0, javaSourcePath.length()-JAVA_SOURCE_SUFFIX_LENGTH);
			
			// Decompile class file
			try {
				source = decompile(this.basePath.getAbsolutePath(), internalTypeName);
			} catch (Exception e) {
				JavaDecompilerPlugin.getDefault().getLog().log(new Status(
					IStatus.ERROR, JavaDecompilerPlugin.PLUGIN_ID, 
					0, e.getMessage(), e));
			}
		}

		return source;
	}
		
    /**
     * @param basePath          Path to the root of the classpath, either a 
     *                          path to a directory or a path to a jar file.
     * @param internalClassName internal name of the class.
     * @return Decompiled class text.
     */
	protected char[] decompile(String basePath, String internalTypeName) throws Exception {
		// Load preferences
		IPreferenceStore store = JavaDecompilerPlugin.getDefault().getPreferenceStore();
		
		boolean realignmentLineNumber = store.getBoolean(JavaDecompilerPlugin.PREF_REALIGN_LINE_NUMBERS);
		boolean unicodeEscape = store.getBoolean(JavaDecompilerPlugin.PREF_ESCAPE_UNICODE_CHARACTERS);
		boolean showLineNumbers = store.getBoolean(JavaDecompilerPlugin.PREF_SHOW_LINE_NUMBERS);
		boolean showMetaData = store.getBoolean(JavaDecompilerPlugin.PREF_SHOW_METADATA);
		
        Map<String, String> configuration = new HashMap<>();
        configuration.put(Preferences.REALIGN_LINE_NUMBERS, Boolean.toString(realignmentLineNumber));
        configuration.put(Preferences.ESCAPE_UNICODE_CHARACTERS, Boolean.toString(unicodeEscape));
        configuration.put(Preferences.WRITE_LINE_NUMBERS, Boolean.toString(showLineNumbers));
        configuration.put(Preferences.WRITE_METADATA, Boolean.toString(showMetaData));
        
        // Initialize loader
        Loader loader;
        File base = new File(basePath);
        
        if (base.isFile()) {
        	if (basePath.toLowerCase().endsWith(".jar") || basePath.toLowerCase().endsWith(".zip")) {
        		loader = new ZipLoader(base);
        	} else {
				JavaDecompilerPlugin.getDefault().getLog().log(new Status(
						IStatus.ERROR, JavaDecompilerPlugin.PLUGIN_ID, 
						"Unexpected container type file: " + basePath));
        		return null;
        	}
        } else {        
        	loader = new DirectoryLoader(base);
        }

        String entryPath = internalTypeName + StringConstants.CLASS_FILE_SUFFIX;
		String decompiledOutput = printer.buildDecompiledOutput(configuration, loader, entryPath, DECOMPILER);
		return decompiledOutput .toCharArray();
	}
}
