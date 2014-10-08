package org.opennms.maven.plugins.karaf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.karaf.features.BundleInfo;
import org.apache.karaf.features.ConfigFileInfo;
import org.junit.Test;

public class FeatureBuilderTest {

	@Test
	public void testEmptyFeatures() {
		FeaturesBuilder builder = new FeaturesBuilder();
		assertNotNull(builder);
		builder.setName("empty");
		assertEquals("empty", builder.getFeatures().getName());
	}

	@Test
	public void testCreateFeature() {
		FeaturesBuilder builder = new FeaturesBuilder("foo");
		FeatureBuilder featureBuilder = builder.createFeature("blah").addBundle("foo");
		assertNotNull(featureBuilder);
		assertNotNull(featureBuilder.getFeature());
		assertEquals("blah", featureBuilder.getFeature().getName());
		assertNotNull(builder.getFeatures().getFeature());
		assertEquals(1, builder.getFeatures().getFeature().size());
	}
	
	@Test
	public void testCreateVersionedFeature() {
		FeaturesBuilder builder = new FeaturesBuilder("foo");
		FeatureBuilder featureBuilder = builder.createFeature("blah", "1.0-SNAPSHOT").addBundle("foo");
		assertNotNull(featureBuilder);
		assertNotNull(featureBuilder.getFeature());
		assertTrue(builder.getFeatures().getFeature().size() > 0);
		assertEquals("1.0-SNAPSHOT", builder.getFeatures().getFeature().get(0).getVersion());
	}
	
	@Test
	public void testFeatureWithDescription() {
		FeatureBuilder fb = new FeatureBuilder("blah");
		fb.setVersion("1.0").setDescription("description");
		assertEquals("1.0", fb.getFeature().getVersion());
		assertEquals("description", fb.getFeature().getDescription());
	}
	
	@Test
	public void testFeatureWithDetails() {
		FeatureBuilder fb = new FeatureBuilder("a");
		fb.setDetails("monkeys fling poo");
		// not yet - wait for KARAF-1515 to be merged // assertNull(fb.getFeature().getVersion());
		assertEquals("monkeys fling poo", fb.getFeature().getDetails());
	}
	
	@Test
	public void testFeatureWithConfig() {
		FeatureBuilder fb = new FeatureBuilder("a");
		fb.addConfig("name", "value = bar");
		final List<org.apache.karaf.features.internal.model.Config> config = fb.getFeature().getConfig();
		assertNotNull(config);
		assertEquals(1, config.size());
		assertEquals("name", config.get(0).getName());
	}
	
	@Test
	public void testFeatureWithConfigFile() {
		FeatureBuilder fb = new FeatureBuilder("a");
		fb.addConfigFile("src/main/resources/foo.cfg", "myfeature.cfg");
		final List<ConfigFileInfo> configFiles = fb.getFeature().getConfigurationFiles();
		assertNotNull(configFiles);
		assertEquals(1, configFiles.size());
		assertEquals("myfeature.cfg", configFiles.get(0).getFinalname());
		
		fb.addConfigFile("src/main/resources/bar.cfg", "mybar.cfg", true);
		assertEquals(2, configFiles.size());
		assertEquals("mybar.cfg", configFiles.get(1).getFinalname());
		assertTrue(configFiles.get(1).isOverride());
	}
	
	@Test
	public void testFeatureWithFeatureDependency() {
		FeatureBuilder fb = new FeatureBuilder("a");
		fb.addFeature("feature1").addFeature("feature2", "1.0");
		final List<org.apache.karaf.features.Dependency> deps = fb.getFeature().getDependencies();
		assertNotNull(deps);
		assertEquals(2, deps.size());
		assertEquals("feature1", deps.get(0).getName());
		assertEquals("feature2", deps.get(1).getName());
		assertEquals("1.0", deps.get(1).getVersion());
	}
	
	@Test
	public void testFeatureWithBundleDependency() {
		FeatureBuilder fb = new FeatureBuilder("a");
		fb.addBundle("mvn:foo/bar/1.0").addBundle("mvn:baz/ack/1.0", 25);
		final List<BundleInfo> bundles = fb.getFeature().getBundles();
		assertNotNull(bundles);
		assertEquals(2, bundles.size());
		assertEquals("mvn:foo/bar/1.0", bundles.get(0).getLocation());
		assertEquals("mvn:baz/ack/1.0", bundles.get(1).getLocation());
		assertEquals(25, bundles.get(1).getStartLevel());
	}
}
