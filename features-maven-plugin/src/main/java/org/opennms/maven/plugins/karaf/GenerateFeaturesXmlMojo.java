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
import java.util.List;
import java.util.jar.JarFile;

import javax.xml.bind.JAXBException;

import org.apache.karaf.features.BundleInfo;
import org.apache.karaf.features.internal.model.Feature;
import org.apache.karaf.features.internal.model.Features;
import org.apache.karaf.features.internal.model.JaxbUtil;
import org.apache.karaf.tooling.features.ManifestUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * Goal which generates a karaf features.xml from maven
 * 
 * @goal generate-features-xml
 * @phase process-resources
 * @requiresDependencyResolution
 */
@SuppressWarnings("restriction")
public class GenerateFeaturesXmlMojo extends AbstractMojo {

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
     * The maven project's helper.
     *
     * @component
     * @required
     * @readonly
     */
    protected MavenProjectHelper projectHelper;

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
    	final FeaturesBuilder featuresBuilder = new FeaturesBuilder(project.getArtifactId());
    	final FeatureBuilder projectFeatureBuilder = featuresBuilder.createFeature(project.getArtifactId(), project.getVersion());
    	if (project.getName() != project.getArtifactId()) {
    		projectFeatureBuilder.setDescription(project.getName());
    	}
    	projectFeatureBuilder.setDetails(project.getDescription());

    	addFeaturesFromConfiguration(projectFeatureBuilder);
    	addBundlesFromConfiguration(projectFeatureBuilder);
    	addDependenciesFromMaven(featuresBuilder, projectFeatureBuilder);

    	final Features features = featuresBuilder.getFeatures();

    	if (projectFeatureBuilder.isEmpty()) {
    		features.getFeature().remove(projectFeatureBuilder.getFeature());
    	}

    	FileWriter writer = null;
		try {
			outputFile.getParentFile().mkdirs();
			writer = new FileWriter(outputFile);
			JaxbUtil.marshal(features, writer);
		} catch (final IOException e) {
			throw new MojoExecutionException("Unable to open outputFile (" + outputFile + ") for writing.", e);
		} catch (final JAXBException e) {
			throw new MojoExecutionException("Unable to marshal the feature XML to outputFile (" + outputFile + ")", e);
		} finally {
			IOUtil.close(writer);
		}
		
		projectHelper.attachArtifact(project, "xml", "features", outputFile);
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

	void addDependenciesFromMaven(final FeaturesBuilder featuresBuilder, final FeatureBuilder projectFeatureBuilder) throws MojoExecutionException {
		getLog().debug("project = " + project);

		addLocalDependencies(featuresBuilder, projectFeatureBuilder, project);
	}

	void addLocalDependencies(final FeaturesBuilder featuresBuilder, final FeatureBuilder projectFeatureBuilder, final MavenProject project) throws MojoExecutionException {
        
		for (final Dependency dependency : project.getDependencies()) {
			getLog().debug("getting artifact for dependency " + dependency);

			if (getIgnoredScopes().contains(dependency.getScope())) {
        		getLog().debug("Dependency " + dependency + " is in scope: " + dependency.getScope() + ", ignoring.");
        		continue;
			}

			org.apache.maven.artifact.Artifact matched = null;
			for (final org.apache.maven.artifact.Artifact art : project.getArtifacts()) {
				if (!dependency.getGroupId().equals(art.getGroupId())) { continue; }
				if (!dependency.getArtifactId().equals(art.getArtifactId())) { continue; }
				if (!dependency.getVersion().equals(art.getVersion())) { continue; }
				if (!dependency.getType().equals(art.getType())) { continue; }
				if (dependency.getClassifier() == null && art.getClassifier() != null) { continue; }
				if (dependency.getClassifier() != null && !dependency.getClassifier().equals(art.getClassifier())) { continue; }

				matched = art;
				break;
			}
			
			if (matched == null) {
				throw new MojoExecutionException("Unable to match artifact for dependency: " + dependency);
			} else {
				getLog().debug("Found match for dependency: " + dependency);
				try {
					addBundleArtifact(featuresBuilder, projectFeatureBuilder, matched);
				} catch (final Exception e) {
					throw new MojoExecutionException("An error occurred while adding artifact " + matched + " to the features file.", e);
				}
			}
			/*
			final org.apache.maven.artifact.Artifact art = artifactFactory.createArtifactWithClassifier(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), dependency.getType(), dependency.getClassifier());
			try {
				resolver.resolve(art, remoteRepositories, localRepository);
			} catch (Exception e) {
				throw new MojoExecutionException("failed to resolve dependency: " + dependency, e);
			}*/

			// getLog().debug("found artifact result: " + art + " (" + art.getFile() + ")");
			// addBundleArtifact(fb, art);
			/*
			final org.sonatype.aether.graph.Dependency dep = RepositoryUtils.toDependency(dependency, repoSession.getArtifactTypeRegistry());
			final DependencyRequest request = new DependencyRequest();
			final DependencyNode root = new DefaultDependencyNode(dep);
			request.setRoot(root);
			final DependencyResult result;
			try {
				result = repoSystem.resolveDependencies(repoSession, request);
			} catch (final DependencyResolutionException e) {
				throw new MojoExecutionException("failed to resolve dependency: " + dependency, e);
			}
			
			for (final ArtifactResult artifactResult : result.getArtifactResults()) {
				getLog().debug("found artifact result: " + artifactResult);
				// addBundleArtifact(fb, artifact);
			}
			*/
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

	void addBundleArtifact(final FeaturesBuilder featuresBuilder, final FeatureBuilder projectFeatureBuilder, final Artifact artifact) throws IOException, JAXBException, MojoExecutionException {
		getLog().debug("addBundleArtifact: " + artifact);

		final File file = artifact.getFile();
		JarFile jf = null;
		try {
			jf = new JarFile(file);
		} catch (final Exception e) {
			// we just want to see if it's something with a manifest, ignore zip failures
		}

		if (isFeature(artifact)) {
			final Features features = readFeaturesFile(file);
			for (final Feature feature : features.getFeature()) {
				if (projectFeatureBuilder.getFeature().getName().equals(feature.getName())) {
					getLog().warn("addBundleArtifact: Found feature named '" + feature.getName() + "' in artifact '" + artifact + "', but we already have a feature with that name.  Skipping.");
				} else {
					getLog().info("addBundleArtifact: Including feature '" + feature.getName() + "' from " + artifact);
					featuresBuilder.addFeature(feature);
				}
			}
		} else if ("pom".equals(artifact.getType())) {
			getLog().debug("addBundleArtifact: " + artifact + " is a POM.  Skipping.");
			// skip POM dependencies that aren't features
			return;
		} else if (jf != null && jf.getManifest() != null && ManifestUtils.isBundle(jf.getManifest())) {
			final String bundleName = MavenUtil.artifactToMvn(artifact);
			projectFeatureBuilder.addBundle(bundleName);
		} else {
			boolean found = false;

			for (final BundleInfo bundle : projectFeatureBuilder.getFeature().getBundles()) {
				final Artifact bundleArtifact = MavenUtil.mvnToArtifact(bundle.getLocation().replace("wrap:", ""));
				if (bundleArtifact != null && bundleArtifact.toString().equals(artifact.toString())) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				throw new MojoExecutionException("artifact " + artifact + " is not a bundle!");
			} else {
				getLog().debug("Added a non-bundle artifact, but it's already defined in the <bundles> configuration.  Skipping.");
			}
		}
	}

	List<String> getIgnoredScopes() {
		if (ignoredScopes == null) {
			return DependencyHelper.DEFAULT_IGNORED_SCOPES;
		}
		return ignoredScopes;
	}

	private boolean isFeature(final Artifact artifact) {
		if ("kar".equals(artifact.getType()) || FEATURE_CLASSIFIER.equals(artifact.getClassifier())) {
			return true;
		}
		return false;
	}

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

    void setIgnoredScopes(final List<String> scopes) {
    	ignoredScopes = scopes;
    }
}
