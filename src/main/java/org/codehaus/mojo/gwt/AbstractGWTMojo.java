package org.codehaus.mojo.gwt;

/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Map;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Abstract superclass for all Google Web Toolkit mojos.
 * @author Bob Allison
 */
public abstract class AbstractGWTMojo
        extends AbstractMojo
{
    
    /**
     * Location of the source files.
     *
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private File sourceDirectory;
    
    /**
     * Location of the source files.
     *
     * @parameter expression="${project.build.testSourceDirectory}"
     * @required
     */
    private File testsourceDirectory;
    
    /**
     * Location to place the web content.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;
    
    /**
     * Location to find compiled classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File classDirectory;
    
    /**
     * Location to find compiled classes.
     *
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     * @readonly
     */
    private File testclassDirectory;
    
    /**
     * The name of the GWT module.
     *
     * @parameter
     * @required
     */
    private String moduleName;
    
    /**
     * The name of the home page.
     *
     * @parameter
     * @required
     */
    private String homePage;
    
    /**
     * The GWT installation directory.
     *
     * @parameter
     * @required
     */
    private File gwtDirectory;
    
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * System properties to set on the command line.
     *
     * @parameter
     */
    private Map properties;
    
    /**
     * Additional class path elements to add.
     *
     * @parameter
     */
    private String[] pathelements;
    
    
    // // //  Getters for subclasses
    
    
    /**
     * Get the name of the GWT module.
     * @return The name of the GWT module
     */
    protected String getModuleName()
    {
        return moduleName;
    }
    
    /**
     * Get the name of the home page.
     * @return The name of the home page
     */
    protected String getHomePage()
    {
        return homePage;
    }
    
    
    // // //  Consumers for tool output
    
    /**
     * Consumer to receive lines sent to stdout.  The lines are logged
     * as info.
     */
    private class StdoutConsumer  implements StreamConsumer
    {
        /** Logger to receive the lines. */
        private Log logger;
        
        /**
         * Constructor.
         * @param log The logger to receive the lines
         */
        public StdoutConsumer( Log log )
        {
            logger = log;
        }
        
        /**
         * Consume a line.
         * @param string The line to consume
         */
        public void consumeLine( String string )
        {
            logger.info( string );
        }
    }
    
    /**
     * Consumer to receive lines sent to stderr.  The lines are logged
     * as warnings.
     */
    private class StderrConsumer  implements StreamConsumer
    {
        /** Logger to receive the lines. */
        private Log logger;
        
        /**
         * Constructor.
         * @param log The logger to receive the lines
         */
        public StderrConsumer( Log log )
        {
            logger = log;
        }
        
        /**
         * Consume a line.
         * @param string The line to consume
         */
        public void consumeLine( String string )
        {
            logger.warn( string );
        }
    }
    
    // // //  Methods for mojos

    /**
     * Run the GWT tool.
     * @param classname The class to execute
     * @param target The target for GWT to operate on
     * @throws MojoExecutionException if a problem occurs running GWT
     */
    protected void runGWT(String classname, String target)
    throws MojoExecutionException
    {
	runGWT( classname, target, new String[0] );
    }

    /**
     * Run the GWT tool.
     * @param classname The class to execute
     * @param target The target for GWT to operate on
     * @param arg Extra arguments to pass
     * @throws MojoExecutionException if a problem occurs running GWT
     */
    protected void runGWT(String classname, String target, String arg)
    throws MojoExecutionException
    {
	runGWT( classname, target, new String[] { arg } );
    }
    
    /**
     * Run the GWT tool.
     * @param classname The class to execute
     * @param target The target for GWT to operate on
     * @param arg1 Extra arguments to pass
     * @param arg2 Extra arguments to pass
     * @throws MojoExecutionException if a problem occurs running GWT
     */
    protected void runGWT(String classname, String target, String arg1, String arg2)
    throws MojoExecutionException
    {
	runGWT( classname, target, new String[] { arg1, arg2 } );
    }

    /**
     * Run the GWT tool.
     * @param classname The class to execute
     * @param target The target for GWT to operate on
     * @param arguments Extra arguments to pass
     * @throws MojoExecutionException if a problem occurs running GWT
     */
    protected void runGWT( String classname, String target, String[] arguments )
    throws MojoExecutionException
    {
        // Build the command line
        Commandline cl = new Commandline();
        cl.setExecutable( "java" );
        if ( isMac() ) {
            cl.createArgument().setValue( "-XstartOnFirstThread" );
        }
        cl.createArgument().setValue( "-cp" );
        cl.createArgument().setValue( getClassPath( false ) );
        if ( properties != null )
        {
            for ( Iterator it = properties.keySet().iterator(); it.hasNext(); )
            {
                String k = (String) it.next();
                String v = (String) properties.get( k );
                cl.createArgument().setValue( "-D" + k + "=" + v );
            }
        }
        cl.createArgument().setValue( classname );

	for( int i = 0; i < arguments.length; i++ ) {
	    cl.createArgument().setValue( arguments[i] );
	}

        cl.createArgument().setValue( "-out" );
        cl.createArgument().setValue( outputDirectory.getAbsolutePath() );
        cl.createArgument().setValue( target );
        
        runCommand( cl );
    }
    
    private boolean isMac() {
        return Os.isFamily( "mac" );
    }

    /**
     * Run the GWT tool.
     * @param classname The class to test
     * @throws MojoExecutionException if a problem occurs running JUnit
     */
    protected void runJUnit( String classname )
    throws MojoExecutionException
    {
        // Build the command line
        Commandline cl = new Commandline();
        cl.setExecutable( "java" );
        cl.createArgument().setValue( "-cp" );
        cl.createArgument().setValue( getClassPath( true ) );
        if ( properties != null )
        {
            for ( Iterator it = properties.keySet().iterator(); it.hasNext(); )
            {
                String k = (String) it.next();
                String v = (String) properties.get( k );
                cl.createArgument().setValue( "-D" + k + "=" + v );
            }
        }
        //cl.createArgument().setValue( "-Dgwt.args='-out " + outputDirectory.getAbsolutePath() + "'" );
        cl.createArgument().setValue( "junit.textui.TestRunner" );
        cl.createArgument().setValue( classname );
        
        runCommand( cl );
    }
    
    // // //  Private methods
    
    /**
     * Get the class path for the GWT execution.
     * @param addTest <code>true</code> to add test classes to path
     * @return The class path as a string
     */
    private String getClassPath( boolean addTest )
    {
        // Get the class path separator
        String cpsep = System.getProperty( "path.separator" );
        
        // Start the class path with the source directory (according to hint
        // in the GWT documentation
        StringBuffer retval = new StringBuffer();
        retval.append( sourceDirectory.getAbsolutePath() );
        
        // Add the compiled class directory
        retval.append( cpsep );
        retval.append( classDirectory.getAbsolutePath() );
        if ( addTest )
        {
            retval.append( cpsep );
            retval.append( testsourceDirectory.getAbsolutePath() );
            retval.append( cpsep );
            retval.append( testclassDirectory.getAbsolutePath() );
        }
        getLog().debug( "getArtifacts: " + project.getArtifacts() );
        getLog().debug( "getDependencyArtifacts: " + project.getDependencyArtifacts() );
        
        // Add any configured entries
        if ( pathelements != null )
        {
            for ( int i = 0; i < pathelements.length; i++ )
            {
                retval.append( cpsep );
                retval.append( pathelements[i] );
            }
        }
        
        // Add the non-provided dependencies except GWT jars
        for ( Iterator it = project.getArtifacts().iterator(); it.hasNext(); )
        {
            Artifact a = (Artifact) it.next();
            getLog().debug( "Dependency " + a + " (" + a.getFile() + ")" );
            File f = a.getFile();
            if ( ( f != null ) && ( !a.getScope().equalsIgnoreCase( "provided" ) )
                    && ( !a.getGroupId().equals( "com.google.gwt" ) ) )
            {
                retval.append( cpsep );
                retval.append( f.getAbsolutePath() );
            }
        }
        
        // Add the plugin's class path
        URLClassLoader loader = (URLClassLoader) getClass().getClassLoader();
        URL[] ua = loader.getURLs();
        for ( int i = 0; i < ua.length; i++ )
        {
            retval.append( cpsep );
            retval.append( ua[i].toExternalForm().substring( "file:".length() ) );
        }
        
        // Add the list of the jars in the GWT install directory
        // NOTE:  These jars MUST be referenced from the installation directory so that
        //        native libraries located in that directory can be located and loaded
        String[] contents = gwtDirectory.list();
        for ( int i = 0; i < contents.length; i++ )
        {
            if ( ( contents[i].endsWith( ".jar" ) ) && ( contents[i].indexOf( "servlet" ) == -1 ) )
            {
                File f = new File( gwtDirectory, contents[i] );
                retval.append( cpsep );
                retval.append( f.getAbsolutePath() );
            }
        }
        
        getLog().debug( "Class path: " + retval );
        
        return retval.toString();
    }
    
    /**
     * Run the command.
     * @param cl The command line to execute
     */
    private void runCommand( Commandline cl )
    throws MojoExecutionException
    {
        getLog().debug( "Command line: " + Commandline.toString( cl.getCommandline() ) );
        
        // Build the consumers
        StreamConsumer stdout = new StdoutConsumer( getLog() );
        StreamConsumer stderr = new StderrConsumer( getLog() );
        
        // Run the tool
        try
        {
            int result = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
            if ( result != 0 )
            {
                throw new MojoExecutionException( "Toolkit execution returned: \'" + result + "\'." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Unable to run " + cl.getExecutable(), e );
        }
    }
}
