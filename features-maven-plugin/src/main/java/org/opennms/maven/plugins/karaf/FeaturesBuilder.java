package org.opennms.maven.plugins.karaf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.features.internal.model.Feature;
import org.apache.karaf.features.internal.model.Features;
import org.apache.karaf.features.internal.model.JaxbUtil;
import org.apache.karaf.tooling.features.Dependency31Helper;
import org.apache.karaf.tooling.features.DependencyHelper;
import org.apache.karaf.tooling.features.DependencyHelperFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;

public class FeaturesBuilder {
    private String m_name;
    private boolean m_importRepositories = false;
    private List<String> m_importRepositoryExclusions = new ArrayList<String>();
    private List<String> m_repositories = new ArrayList<String>();
    private List<Feature> m_features = new ArrayList<Feature>();

    private Log m_log = new SystemStreamLog();

    protected DependencyHelper m_dependencyHelper;

    /*
    static {
        URL.setURLStreamHandlerFactory(new CustomBundleURLStreamHandlerFactory());
    }
    */

    public FeaturesBuilder() {
    }

    public FeaturesBuilder(final String name) {
        m_name = name;
    }

    public FeaturesBuilder(final String name, final PlexusContainer container, final MavenProject project, final MavenSession session) throws MojoExecutionException {
        m_name = name;
        m_dependencyHelper = DependencyHelperFactory.createDependencyHelper(container, project, session, m_log);
    }

    public FeaturesBuilder setName(final String name) {
        m_name = name;
        return this;
    }

    public FeaturesBuilder addRepository(final String repository) throws MojoExecutionException {
        if (m_importRepositories) {
            // If the repository exclusions contain this full repository URL, then just add it 
            // to the current <features> repository as a <repository> reference.
            if (m_importRepositoryExclusions.contains(repository)) {
                m_repositories.add(repository);
                return this;
            }

            // Fetch the repository <features> XML file using the mvn URL handler
            InputStream stream;
            try {
                // TODO: Figure out how to use Java's default URL scheme handling instead of
                // manually specifying the pax-url-mvn Handler()
                if (repository.startsWith("mvn:")) {
                    if (m_dependencyHelper == null) {
                        throw new MojoExecutionException("No dependency resolver initialized!");
                    }
                    final Artifact repositoryArtifact = m_dependencyHelper.mvnToArtifact(repository);
                    final Object a = getArtifact(repositoryArtifact);
                    final File resolved = m_dependencyHelper.resolve(a, m_log);
                    stream = new FileInputStream(resolved);
                } else if (repository.startsWith("file:")) {
                    stream = new URL(repository).openStream();
                } else {
                    throw new MalformedURLException("Cannot handle this URL scheme: " + repository);
                }
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Could not load URL: " + repository, e);
            } catch (IOException e) {
                throw new MojoExecutionException("Could not load URL: " + repository, e);
            }

            // Unmarshall the mvn resource as a Features instance.
            Features features = JaxbUtil.unmarshal(stream, false);

            // If this repository is excluded from importing, then just add it to the current
            // <features> repository as a <repository> reference.
            if (m_importRepositoryExclusions.contains(features.getName())) {
                m_repositories.add(repository);
            } else {
                // Recursively add any child repository references 
                for (String childRepo : features.getRepository()) {
                    addRepository(childRepo);
                }
                // Import any features from this repository to the current repository
                for (Feature feature : features.getFeature()) {
                    addFeature(feature);
                }
            }
        } else {
            m_repositories.add(repository);
        }
        return this;
    }

    protected Object getArtifact(final Artifact repositoryArtifact) {
        final String groupId = repositoryArtifact.getGroupId();
        final String artifactId = repositoryArtifact.getArtifactId();
        final String classifier = repositoryArtifact.getClassifier();
        final String version = repositoryArtifact.getVersion();

        if (m_dependencyHelper instanceof Dependency31Helper) {
            final org.eclipse.aether.artifact.ArtifactType type = new org.eclipse.aether.artifact.DefaultArtifactType(repositoryArtifact.getType());
            return new org.eclipse.aether.artifact.DefaultArtifact(groupId, artifactId, classifier, null, version, type);
        } else {
            final org.sonatype.aether.artifact.ArtifactType type = new org.sonatype.aether.util.artifact.DefaultArtifactType(repositoryArtifact.getType());
            return new org.sonatype.aether.util.artifact.DefaultArtifact(groupId, artifactId, classifier, null, version, type);
        }
    }

    public FeaturesBuilder addFeature(final Feature feature) {
        for (final Feature f : m_features) {
            final String featureDesc = feature.getName() + ":" + feature.getVersion();
            final String existingDesc = f.getName() + ":" + f.getVersion();
            if (featureDesc.equals(existingDesc)) {
                //throw new IllegalArgumentException(feature.getName() + " already exists!");
                //new SystemStreamLog().debug("duplicate feature: " + featureDesc);
                return this;
            }
        }
        m_features.add(feature);
        return this;
    }

    public FeatureBuilder createFeature(final String name) {
        return createFeature(name, null);
    }

    public FeatureBuilder createFeature(final String name, final String version) {
        final FeatureBuilder featureBuilder = new FeatureBuilder(name, version);
        m_features.add(featureBuilder.getFeature());
        return featureBuilder;
    }

    public Features getFeatures() {
        final org.opennms.maven.plugins.karaf.model.Features features = new org.opennms.maven.plugins.karaf.model.Features();
        features.setName(m_name);

        for (final String repository : m_repositories) {
            features.addRepository(repository);
        }

        for (final Feature feature : m_features) {
            if (!FeatureBuilder.isEmpty(feature)) {
                features.addFeature(feature);
            }
        }

        return features;
    }

    public void setImportRepositories(boolean importRepositories) {
        m_importRepositories = importRepositories;
    }

    public void setImportRepositoryExclusions(List<String> importRepositoryExclusions) {
        m_importRepositoryExclusions = importRepositoryExclusions;
    }
}
