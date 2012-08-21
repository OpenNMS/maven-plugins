package org.opennms.maven.plugins.karaf;

import java.util.ArrayList;
import java.util.List;

import org.opennms.maven.plugins.karaf.model.internal.Feature;
import org.opennms.maven.plugins.karaf.model.internal.Features;

public class FeaturesBuilder {
	private String m_name;
	private List<String> m_repositories = new ArrayList<String>();
	private List<Feature> m_features = new ArrayList<Feature>();

	public FeaturesBuilder() {
	}

	public FeaturesBuilder(final String name) {
		m_name = name;
	}

	public FeaturesBuilder setName(final String name) {
		m_name = name;
		return this;
	}

	public FeaturesBuilder addRepository(final String repository) {
		m_repositories.add(repository);
		return this;
	}

	public FeaturesBuilder addFeature(final Feature feature) {
		for (final Feature f : m_features) {
			final String featureDesc = feature.getName() + ":" + feature.getVersion();
			final String existingDesc = f.getName() + ":" + f.getVersion();
			if (featureDesc.equals(existingDesc)) {
				throw new IllegalArgumentException(feature.getName() + " already exists!");
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

}
