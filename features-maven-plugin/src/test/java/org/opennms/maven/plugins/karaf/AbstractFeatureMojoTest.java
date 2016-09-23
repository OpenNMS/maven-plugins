package org.opennms.maven.plugins.karaf;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.karaf.tooling.features.model.Feature;
import org.junit.Test;

import com.google.common.collect.Sets;

public class AbstractFeatureMojoTest {

    @Test
    public void getMatchingFeaturesReturnsFeatureWithHighestVersion() {
        Feature a1 = new Feature("a");
        a1.setVersion("1.0.0");
 
        Feature a2 = new Feature("a");
        a2.setVersion("2.0.0");

        assertEquals(Sets.newHashSet(a2), AbstractFeatureMojo.getMatchingFeatures(toFeaturesMap(a1, a2), "a"));
    }

    @Test
    public void getMatchingFeaturesReturnsAllFeaturesInRange() {
        Feature a1 = new Feature("a");
        a1.setVersion("1.0.0");
 
        Feature a2 = new Feature("a");
        a2.setVersion("2.0.0");

        assertEquals(Sets.newHashSet(a1, a2), AbstractFeatureMojo.getMatchingFeatures(toFeaturesMap(a1, a2), "a/1.0"));
        assertEquals(Sets.newHashSet(a1), AbstractFeatureMojo.getMatchingFeatures(toFeaturesMap(a1, a2), "a/[1.0,2.0)"));
    }

    private static Map<String, Feature> toFeaturesMap(Feature...features) {
        return Arrays.asList(features).stream()
            .collect(Collectors.toMap(f -> f.getName() + "/" + f.getVersion(), f -> f));
    }
}
