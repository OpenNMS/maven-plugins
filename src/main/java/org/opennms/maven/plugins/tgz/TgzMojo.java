package org.opennms.maven.plugins.tgz;

import java.io.File;

import org.apache.maven.plugins.assembly.model.Assembly;
import org.apache.maven.project.MavenProject;

/**
 * Set the main artifact for a plugin
 * 
 * @goal tgz
 * @phase package
 * @requiresProject
 * @author <a href="brozow@opennms.org">Matt Brozowski</a>
 * @version $Id$
 */
public class TgzMojo extends AbstractAssemblyMojo {
	
	public TgzMojo() {
		shouldIncludeSelfAsDependency = false;
	}


	protected void attachArtifact(Assembly assembly, String format, File destFile) {
		project.getArtifact().setFile(destFile);
	}

	protected MavenProject getExecutedProject() {
		return project;
	}

}
