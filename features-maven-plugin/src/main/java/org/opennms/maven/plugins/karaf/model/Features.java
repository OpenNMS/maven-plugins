package org.opennms.maven.plugins.karaf.model;

import java.util.ArrayList;

import org.apache.karaf.features.internal.model.Feature;


public class Features extends org.apache.karaf.features.internal.model.Features {

    public void addRepository(final String repository) {
        if (this.repository == null) {
            this.repository = new ArrayList<String>();
        }
        this.repository.add(repository);
    }

    public void addFeature(final Feature feature) {
        if (this.feature == null) {
            this.feature = new ArrayList<Feature>();
        }
        this.feature.add(feature);
    }

}
