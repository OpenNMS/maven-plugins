package org.opennms.maven.plugins.karaf.model;

import java.util.ArrayList;

import org.apache.karaf.features.internal.model.Bundle;
import org.apache.karaf.features.internal.model.Config;
import org.apache.karaf.features.internal.model.ConfigFile;
import org.apache.karaf.features.internal.model.Dependency;

public class Feature extends org.apache.karaf.features.internal.model.Feature {

    public Feature() {
        super();
    }

    public Feature(final String name) {
        super(name);
    }

    public Feature(final String name, final String version) {
        super(name,version);
    }

    public void addBundle(final Bundle bundle) {
        if (this.bundle == null) {
            this.bundle = new ArrayList<Bundle>();
        }
        this.bundle.add(bundle);
    }

    public void addConfig(final Config c) {
        if (this.config == null) {
            this.config = new ArrayList<Config>();
        }
        this.config.add(c);
    }

    public void addConfigFile(final ConfigFile file) {
        if (this.configfile == null) {
            this.configfile = new ArrayList<ConfigFile>();
        }
        this.configfile.add(file);
    }

    public void addDependency(final Dependency dependency) {
        if (this.feature == null) {
            this.feature = new ArrayList<Dependency>();
        }
        this.feature.add(dependency);
    }

}
