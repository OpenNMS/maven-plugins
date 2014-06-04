package org.opennms.maven.plugins.karaf;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.opennms.maven.plugins.karaf.model.internal.Feature;
import org.opennms.maven.plugins.karaf.model.internal.Features;
import org.opennms.maven.plugins.karaf.model.internal.JaxbUtil;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.ops4j.pax.url.mvn.internal.Connection;
import org.ops4j.pax.url.mvn.internal.config.MavenConfigurationImpl;

import shaded.org.ops4j.util.property.PropertiesPropertyResolver;

public class FeaturesBuilder {
    private String m_name;
    private String m_basedir;
    private boolean m_importRepositories = false;
    private List<String> m_importRepositoryExclusions = new ArrayList<String>();
    private List<String> m_repositories = new ArrayList<String>();
    private List<Feature> m_features = new ArrayList<Feature>();

    public FeaturesBuilder() {
    }

    public FeaturesBuilder(final String name) {
        m_name = name;
    }

    public FeaturesBuilder(final String name, final String basedir) {
        m_name = name;
        m_basedir = basedir;
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
                    stream = new URL(null, repository, new FeaturesHandler(m_basedir)).openStream();
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
        final Features features = new Features();
        features.setName(m_name);

        for (final String repository : m_repositories) {
            features.getRepository().add(repository);
        }

        for (final Feature feature : m_features) {
            if (!FeatureBuilder.isEmpty(feature)) {
                features.getFeature().add(feature);
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

    public class FeaturesHandler extends URLStreamHandler {
        private final String m_basedir;
        public FeaturesHandler(final String basedir) {
            m_basedir = basedir;
        }

        @Override
        protected URLConnection openConnection(final URL u) throws IOException {
            final Properties props = new Properties();
            props.putAll(System.getProperties());
            if (m_basedir != null) {
                props.put("org.ops4j.pax.url.mvn.localRepository", m_basedir);
            }

            final PropertiesPropertyResolver propertyResolver = new PropertiesPropertyResolver( props );
            final MavenConfigurationImpl config = new MavenConfigurationImpl( propertyResolver, ServiceConstants.PID );
            return new Connection( u, config );
        }
    }
}
