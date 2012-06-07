/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.maven.plugins.karaf;

import static org.apache.karaf.deployer.kar.KarArtifactInstaller.FEATURE_CLASSIFIER;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import javax.xml.bind.JAXBException;

import org.apache.karaf.features.internal.model.Features;
import org.apache.karaf.features.internal.model.JaxbUtil;
import org.apache.karaf.tooling.features.DependencyHelper;
import org.apache.karaf.tooling.features.ManifestUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;

/**
 * Goal which generates a karaf features.xml from maven
 * 
 * @goal generate-features-xml
 * @phase process-resources
 */
@SuppressWarnings("restriction")
public class GenerateFeaturesXmlMojo extends AbstractMojo {
    private static final List<String> DEFAULT_IGNORED_SCOPES = Collections.unmodifiableList(Arrays.asList(new String[] {"test", "provided"}));

	/**
     * (wrapper) The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject project;

    /**
     * The project's remote repositories to use for the resolution of plugins and their dependencies.
     *
     * @parameter default-value="${project.remotePluginRepositories}"
     * @required
     * @readonly
     */
    List<RemoteRepository> pluginRepos;

    /**
     * The project's remote repositories to use for the resolution of project dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    List<RemoteRepository> projectRepos;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @required
     * @readonly
     */
    RepositorySystemSession repoSession;

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     * @required
     * @readonly
     */
    RepositorySystem repoSystem;

    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}/features/features.xml"
     */
	private File outputFile;

    /**
     * Features to include in the generated features.xml file.
     * @parameter
     */
	private List<String> features;

    /**
     * Bundles to include in the generated features.xml file.
     * @parameter
     */
	private List<String> bundles;
	
	/**
	 * Scopes to ignore when processing dependencies.
	 * @parameter
	 */
	private List<String> ignoredScopes;

	public void execute() throws MojoExecutionException {
    	final FeaturesBuilder builder = new FeaturesBuilder(project.getArtifactId());
    	final FeatureBuilder fb = builder.createFeature(project.getArtifactId(), project.getVersion());
    	
    	addFeaturesFromConfiguration(fb);
    	addBundlesFromConfiguration(fb);
    	addDependenciesFromMaven(fb);

    	FileWriter writer = null;
		try {
			outputFile.getParentFile().mkdirs();
			writer = new FileWriter(outputFile);
			JaxbUtil.marshal(builder.getFeatures(), writer);
		} catch (final IOException e) {
			throw new MojoExecutionException("Unable to open outputFile (" + outputFile + ") for writing.", e);
		} catch (final JAXBException e) {
			throw new MojoExecutionException("Unable to marshal the feature XML to outputFile (" + outputFile + ")", e);
		} finally {
			IOUtil.close(writer);
		}
    }

	void addFeaturesFromConfiguration(final FeatureBuilder featureBuilder) {
		getLog().debug("checking for features in the <configuration> tag");
		if (features != null) {
	    	for (final String feature : features) {
	    		getLog().debug("found feature " + feature);
	    		addFeature(featureBuilder, feature);
	    	}
    	}
	}

	void addBundlesFromConfiguration(final FeatureBuilder featureBuilder) throws MojoExecutionException {
		getLog().debug("checking for bundles in the <configuration> tag");
		if (bundles != null) {
	    	for (final String bundle : bundles) {
	    		getLog().debug("found bundle " + bundle);
	    		addBundle(featureBuilder, bundle);
	    	}
    	}
	}

	void addDependenciesFromMaven(final FeatureBuilder fb) throws MojoExecutionException {
		if (pluginRepos == null)  { getLog().warn("plugin repository is missing, skipping dependency-walking");  return; }
		if (projectRepos == null) { getLog().warn("project repository is missing, skipping dependency-walking"); return; }
		if (repoSession == null)  { getLog().warn("repository session is missing, skipping dependency-walking"); return; }
		if (repoSystem == null)   { getLog().warn("repository system is missing, skipping dependency-walking");  return; }

		final DependencyHelper dependencyHelper = new DependencyHelper(pluginRepos, projectRepos, repoSession, repoSystem);
        dependencyHelper.getDependencies(project, true);
        
        getLog().debug("dependencyHelper found: " + dependencyHelper.getTreeListing());

        final Map<Artifact, String> localDependencies = dependencyHelper.getLocalDependencies();
		addLocalDependencies(fb, localDependencies);
	}

	void addLocalDependencies(final FeatureBuilder fb, final Map<Artifact, String> localDependencies) throws MojoExecutionException {
		for (final Map.Entry<Artifact,String> entry : localDependencies.entrySet()) {
        	final Artifact artifact = entry.getKey();
        	final String scope = entry.getValue();

        	if (getIgnoredScopes().contains(scope)) {
        		getLog().debug("Artifact " + artifact + " is in scope: " + scope + ", ignoring.");
        	} else {
	        	try {
					addArtifact(fb, artifact);
				} catch (final Exception e) {
					throw new MojoExecutionException("Unable to add artifact: " + artifact, e);
				}
        	}
        }
	}

	void addFeature(final FeatureBuilder featureBuilder, final String feature) {
		getLog().debug("addFeature: " + feature);

		if (feature.contains("/")) {
			final String[] featureInfo = feature.split("/");
			if (featureInfo == null || featureInfo.length != 2) {
				getLog().debug("Invalid feature '" + feature + "'. Must be just a feature name, or feature/version.");
			} else {
				featureBuilder.addFeature(featureInfo[0], featureInfo[1]);
			}
		} else {
			// no version specified, add directly
			featureBuilder.addFeature(feature);
		}
	}
	
	void addBundle(final FeatureBuilder featureBuilder, final String bundle) throws MojoExecutionException {
		getLog().debug("addBundle: " + bundle);

		if (bundle.contains("@")) {
			final String[] bundleInfo = bundle.split("@");
			if (bundleInfo == null || bundleInfo.length != 2) {
				getLog().debug("Invalid bundle '" + bundle + "'. Must be a bundle specification, optionally followed by @ and an integer start level.");
			} else {
				final Integer startLevel;
				try {
					startLevel = Integer.valueOf(bundleInfo[1]);
				} catch (final NumberFormatException e) {
					throw new MojoExecutionException("Bundle specification ('" + bundle + "') contained a start level, but it was unparseable.");
				}
				featureBuilder.addBundle(bundleInfo[0], startLevel);
			}
		} else {
			// no startLevel specified
			featureBuilder.addBundle(bundle);
		}
	}

	void addArtifact(final FeatureBuilder fb, Artifact artifact) throws IOException, JAXBException {
		getLog().debug("addArtifact: " + artifact);

		final File file;
		if (artifact.getFile() == null) {
			file = resolve(artifact);
		} else {
			file = artifact.getFile();
		}

		JarFile jf = null;
		try {
			jf = new JarFile(file);
		} catch (final ZipException e) {
			// we just want to see if it's something with a manifest, ignore zip failures
		}

		if (isFeature(artifact)) {
			fb.addFeature(artifact.getArtifactId(), artifact.getVersion());
		} else if ("pom".equals(artifact.getExtension())) {
			// skip POM dependencies that aren't features
			return;
		} else if (jf != null && jf.getManifest() != null && ManifestUtils.isBundle(jf.getManifest())) {
			final String bundleName = MavenUtil.artifactToMvn(artifact);
			fb.addBundle(bundleName);
		} else {
			final String bundleName = MavenUtil.artifactToMvn(artifact);
			fb.addBundle("wrap:" + bundleName);
		}
	}

	List<String> getIgnoredScopes() {
		if (ignoredScopes == null) {
			return DEFAULT_IGNORED_SCOPES;
		}
		return ignoredScopes;
	}

	private boolean isFeature(final Artifact artifact) {
		if ("kar".equals(artifact.getExtension()) || FEATURE_CLASSIFIER.equals(artifact.getClassifier())) {
			return true;
		}
		return false;
	}

    @SuppressWarnings("unused")
	private Features readFeaturesFile(final File featuresFile) throws JAXBException, IOException {
        Features features = null;
        InputStream in = null;
        try {
        	in = new FileInputStream(featuresFile);
            features = JaxbUtil.unmarshal(in, false);
        } finally {
        	IOUtil.close(in);
        }
        return features;
    }

    private File resolve(final Artifact artifact) {
    	final ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(projectRepos);

        getLog().debug("Resolving artifact " + artifact + " from " + projectRepos);

        ArtifactResult result;
        try {
            result = repoSystem.resolveArtifact(repoSession, request);
        } catch (final ArtifactResolutionException e) {
            getLog().warn("could not resolve " + artifact, e);
            return null;
        }

        getLog().debug("Resolved artifact " + artifact + " to " + result.getArtifact().getFile() + " from " + result.getRepository());
        return result.getArtifact().getFile();
    }
    
    void setIgnoredScopes(final List<String> scopes) {
    	ignoredScopes = scopes;
    }
}
