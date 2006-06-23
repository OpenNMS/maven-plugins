package org.opennms.maven.plugins.assembly;

import java.io.File;

import org.apache.maven.plugins.assembly.model.Assembly;
import org.apache.maven.project.MavenProject;

/**
 * Set the main artifact for a plugin
 * 
 * @goal assembly
 * @phase package
 * @author <a href="brozow@opennms.org">Matt Brozowski</a>
 * @version $Id$
 */
public class AssemblyMojo extends AbstractAssemblyMojo {
	
	public AssemblyMojo() {
		shouldIncludeSelfAsDependency = false;
	}


	protected void attachArtifact(Assembly assembly, String format, File destFile) {
		project.getArtifact().setFile(destFile);
	}

	protected MavenProject getExecutedProject() {
		return project;
	}

}
