package org.codehaus.mojo.gwt;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class GWTCommand {

    private String m_classname;

    private String m_target;

    private String[] m_arguments;

    private Map m_systemProperties;

    private File m_outputDirectory;

    private Log m_logger;
    
    private List m_classPathEntries = new LinkedList();

    private File m_gwtDirectory;

    private List m_pluginDependencies;

    private Set m_projectDependencies;

    private String[] m_pathElements;

    public GWTCommand(String classname, String target, String[] arguments) {
        m_classname = classname;
        m_target = target;
        m_arguments = arguments;
    }

    public static GWTCommand create(String classname, String target,
            String[] arguments) {
        return new GWTCommand(classname, target, arguments);
    }

    public String[] getArguments() {
        return m_arguments;
    }

    public void setArguments(String[] arguments) {
        m_arguments = arguments;
    }

    public String getClassname() {
        return m_classname;
    }

    public void setClassname(String classname) {
        m_classname = classname;
    }

    public String getTarget() {
        return m_target;
    }

    public void setTarget(String target) {
        m_target = target;
    }

    public void setSystemProperties(Map properties) {
        m_systemProperties = properties;
    }

    public Map getSystemProperties() {
        return m_systemProperties;
    }

    public void setOutputDirectory(File outputDirectory) {
        m_outputDirectory = outputDirectory;
    }

    public File getOutputDirectory() {
        return m_outputDirectory;
    }

    // // // Consumers for tool output

    /**
     * Consumer to receive lines sent to stdout. The lines are logged as info.
     */
    public static class StdoutConsumer implements StreamConsumer {
        /** Logger to receive the lines. */
        private Log logger;

        /**
         * Constructor.
         * 
         * @param log
         *            The logger to receive the lines
         */
        public StdoutConsumer(Log log) {
            logger = log;
        }

        /**
         * Consume a line.
         * 
         * @param string
         *            The line to consume
         */
        public void consumeLine(String string) {
            logger.info(string);
        }
    }

    // // // Consumers for tool output

    /**
     * Consumer to receive lines sent to stderr. The lines are logged as
     * warnings.
     */
    public static class StderrConsumer implements StreamConsumer {
        /** Logger to receive the lines. */
        private Log logger;

        /**
         * Constructor.
         * 
         * @param log
         *            The logger to receive the lines
         */
        public StderrConsumer(Log log) {
            logger = log;
        }

        /**
         * Consume a line.
         * 
         * @param string
         *            The line to consume
         */
        public void consumeLine(String string) {
            logger.warn(string);
        }
    }

    public void setLog(Log log) {
        m_logger = log;
    }
    
    public Log getLog() {
        return m_logger;
    }

    /**
     * Run the command.
     * @param cl The command line to execute
     */
    void runCommand( Commandline cl )
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

    public List getClassPathEntries() {
        return m_classPathEntries;
    }

    void addClassPathEntry(String path) {
        getClassPathEntries().add( path );
    }

    String getClassPath() {
        // Get the class path separator
        String cpsep = System.getProperty( "path.separator" );
        
        StringBuffer retval = new StringBuffer();
        
        boolean first = true;
        for(Iterator it = getClassPathEntries().iterator(); it.hasNext();) {
            String entry = (String)it.next();
            if (first) {
                first = false;
            } else {
                retval.append( cpsep );
            }
            retval.append( entry );
        }
        
        return retval.toString();
    }

    void getJUnit() throws MojoExecutionException {
        String testClass = getTarget();
        Commandline cl = setupCommandLine();
        //cl.createArgument().setValue( "-Dgwt.args='-out " + outputDirectory.getAbsolutePath() + "'" );
        cl.createArgument().setValue( getClassname() );
        cl.createArgument().setValue( testClass );
        
        runCommand( cl );
    }

    void run() throws MojoExecutionException {
        // Build the command line
        Commandline cl = setupCommandLine();
        
        cl.createArgument().setValue( getClassname() );
    
        for( int i = 0; i < getArguments().length; i++ ) {
            cl.createArgument().setValue( getArguments()[i] );
        }
    
        cl.createArgument().setValue( "-out" );
        cl.createArgument().setValue( getOutputDirectory().getAbsolutePath() );
        cl.createArgument().setValue( getTarget() );
        
        runCommand( cl );
    }

    private Commandline setupCommandLine() {
        Commandline cl = new Commandline();
        cl.setExecutable( "java" );
        if ( isMac() ) {
            cl.createArgument().setValue( "-XstartOnFirstThread" );
        }
        cl.createArgument().setValue( "-cp" );
        cl.createArgument().setValue( getClassPath() );
        if ( getSystemProperties() != null )
        {
            for ( Iterator it = getSystemProperties().keySet().iterator(); it.hasNext(); )
            {
                String k = (String) it.next();
                String v = (String) getSystemProperties().get( k );
                cl.createArgument().setValue( "-D" + k + "=" + v );
            }
        }
        return cl;
    }

    boolean isMac() {
        return Os.isFamily( "mac" );
    }

    public File getGWTDirectory() {
        return m_gwtDirectory;
    }
    public void setGWTDirectory(File gwtDirectory) {
        m_gwtDirectory = gwtDirectory;
    }

    public void setPathElements(String[] pathelements) {
        m_pathElements = pathelements;
    }

    public String[] getPathElements() {
        return m_pathElements;
    }

    public List getPluginDependencies() {
        return m_pluginDependencies;
    }

    public void setPluginDependencies(List pluginDependencies) {
        m_pluginDependencies = pluginDependencies;
    }

    public Set getProjectDependencies() {
        return m_projectDependencies;
    }

    public void setProjectDependencies(Set projectDependencies) {
        m_projectDependencies = projectDependencies;
    }

    protected void addGWTJarsFromPluginDependencies() {
        for ( Iterator it = getPluginDependencies().iterator(); it.hasNext(); )
        {
            Artifact a = (Artifact) it.next();
            getLog().debug( "Dependency " + a + " (" + a.getFile() + ")" );
            File f = a.getFile();
            if ( ( f != null ) && ( a.getGroupId().equals( "com.google.gwt" ) ) )
            {
                addClassPathEntry( f.getAbsolutePath() );
            }
        }
        
    }

    protected void addGWTJarsFromGWTDirectory() {
        // Add the list of the jars in the GWT install directory
        // NOTE:  These jars MUST be referenced from the installation directory so that
        //        native libraries located in that directory can be located and loaded
        
        if (m_gwtDirectory == null) {
            return;
        }
        String[] contents = m_gwtDirectory.list();
        if (contents == null) {
           return;
        }
        for ( int i = 0; i < contents.length; i++ )
        {
            if ( ( contents[i].endsWith( ".jar" ) ) && ( contents[i].indexOf( "servlet" ) == -1 ) )
            {
                File f = new File( m_gwtDirectory, contents[i] );
                getLog().debug( "GWTDirJar " + f );
                addClassPathEntry( f.getAbsolutePath() );
            }
        }
    }

    void addConfiguredClasspathEntries() {
        
        String[] pathelements = getPathElements();
        
        // Add any configured entries
        if ( pathelements != null )
        {
            for ( int i = 0; i < pathelements.length; i++ )
            {
                addClassPathEntry( pathelements[i] );
            }
        }
    }

    void addArtifactsToClasspath(Collection artifacts) {
        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact a = (Artifact) it.next();
            getLog().debug( "Dependency " + a + " (" + a.getFile() + ")" );
            File f = a.getFile();
            if ( ( f != null ) && ( !a.getScope().equalsIgnoreCase( "provided" ) )
                    && ( !a.getGroupId().equals( "com.google.gwt" ) ) )
            {
                addClassPathEntry( f.getAbsolutePath() );
            }
        }
    }

    void addPluginClasspathEntries() {
        // Add the plugin's class
        addArtifactsToClasspath(getPluginDependencies());
     }

    void addProjectClasspathEntries() {
        // Add the non-provided dependencies except GWT jars
        addArtifactsToClasspath(getProjectDependencies());
    }

    void addGWTJarsToClasspath() {
        
        addGWTJarsFromGWTDirectory();
    }


}
