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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

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
     * @parameter alias="gwtHome"
     * @required
     */
    File gwtDirectory;
    
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
    
    /**
     * Map of plugin artifacts
     * 
     * @parameter expression="${plugin.artifacts}"
     * @require
     * @readonly
     * 
     */
    private List pluginArtifacts;
    
    /**
     * @parameter expression="${project.artifacts}"
     * @require
     * @readonly
     */
    private Set projectDependencyArtifacts;
    
    
    
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
    
    private void setClassPathEntries(boolean addTest, GWTCommand gwt) {
        // Start the class path with the source directory (according to hint
        // in the GWT documentation
        gwt.addClassPathEntry(sourceDirectory.getAbsolutePath());
        
        // Add the compiled class directory
        gwt.addClassPathEntry( classDirectory.getAbsolutePath() );
        if ( addTest )
        {
            gwt.addClassPathEntry( testsourceDirectory.getAbsolutePath() );
            gwt.addClassPathEntry( testclassDirectory.getAbsolutePath() );
        }
        getLog().debug( "getArtifacts: " + project.getArtifacts() );
        getLog().debug( "getDependencyArtifacts: " + project.getDependencyArtifacts() );
        
        gwt.addConfiguredClasspathEntries();
        
        gwt.addProjectClasspathEntries();
        
        gwt.addPluginClasspathEntries();
        
        gwt.addGWTJarsToClasspath();
        
        getLog().debug( "Class path: " + gwt.getClassPathEntries() );
    }

    protected void requireGWTDirectory(String goal) throws MojoExecutionException {
        if (gwtDirectory == null) {
            throw new MojoExecutionException("gwtDirectory must be configured to run the "+goal+" goal.");
        }
    }
    
    protected void configure(GWTCommand gwt, boolean addTest ) {
        
        gwt.setPluginDependencies(pluginArtifacts);
        gwt.setProjectDependencies(projectDependencyArtifacts);
        gwt.setPathElements(pathelements);
        gwt.setGWTDirectory(gwtDirectory);
        gwt.setLog(getLog());
        gwt.setSystemProperties(this.properties);
        gwt.setOutputDirectory(this.outputDirectory);
        setClassPathEntries(addTest, gwt);
    }

}
