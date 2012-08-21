package org.opennms.maven.plugins.karaf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.opennms.maven.plugins.karaf.model.BundleInfo;
import org.opennms.maven.plugins.karaf.model.Feature;
import org.opennms.maven.plugins.karaf.model.internal.Dependency;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;

public class GenerateFeaturesXmlMojoTest {
	GenerateFeaturesXmlMojo m_mojo;
	
	@Before
	public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);
        
        m_mojo = new GenerateFeaturesXmlMojo();
        final Logger logger = new ConsoleLogger();
        final DefaultLog log = new DefaultLog(logger);
        logger.setThreshold(0);
		m_mojo.setLog(log);
	}

	@Test
	public void testFeature() throws Exception {
		final FeatureBuilder fb = new FeatureBuilder("myfeature");
		m_mojo.addFeature(fb, "test");
		final List<Dependency> featureList = fb.getFeature().getFeature();
		assertNotNull(featureList);
		assertEquals(1, featureList.size());
		assertEquals("test", featureList.get(0).getName());
		final String version = featureList.get(0).getVersion();
		assertTrue(version == null || version == "0.0.0");

		m_mojo.addFeature(fb, "test2/1.0");
		assertEquals(2, featureList.size());
		assertEquals("test2", featureList.get(1).getName());
		assertEquals("1.0", featureList.get(1).getVersion());
	}

	@Test
	public void testBundle() throws Exception {
		final FeatureBuilder fb = new FeatureBuilder("myFeature");
		m_mojo.addBundle(fb, "mvn:foo/bar/1.0");
		final List<BundleInfo> bundles = fb.getFeature().getBundles();
		assertNotNull(bundles);
		assertEquals(1, bundles.size());
		assertEquals("mvn:foo/bar/1.0", bundles.get(0).getLocation());
		assertEquals(0, bundles.get(0).getStartLevel());
		
		m_mojo.addBundle(fb, "mvn:foo/baz/1.2@20");
		assertEquals(2, bundles.size());
		assertEquals("mvn:foo/baz/1.2", bundles.get(1).getLocation());
		assertEquals(20, bundles.get(1).getStartLevel());
	}
	
	@Test
	public void testBundleDependency() throws Exception {
		final FeaturesBuilder featuresBuilder = new FeaturesBuilder("myFeature");
		final FeatureBuilder projectFeatureBuilder = featuresBuilder.createFeature("myFeature");

		final Artifact artifact = getBundleArtifact();
		m_mojo.addBundleArtifact(featuresBuilder, projectFeatureBuilder, artifact);
		
		final List<BundleInfo> bundles = projectFeatureBuilder.getFeature().getBundles();
		assertNotNull(bundles);
		assertEquals(1, bundles.size());
		assertEquals("mvn:org.opennms.core/org.opennms.core.api/1.11.1-SNAPSHOT", bundles.get(0).getLocation());
	}

	@Test(expected=MojoExecutionException.class)
	public void testJarDependency() throws Exception {
		final FeaturesBuilder featuresBuilder = new FeaturesBuilder("myFeature");
		final FeatureBuilder projectFeatureBuilder = featuresBuilder.createFeature("myFeature");

		final Artifact artifact = getJarArtifact();
		m_mojo.addBundleArtifact(featuresBuilder, projectFeatureBuilder, artifact);
	}

	@Test
	public void testFeatureDependency() throws Exception {
		final FeaturesBuilder featuresBuilder = new FeaturesBuilder("myFeature");
		final FeatureBuilder projectFeatureBuilder = featuresBuilder.createFeature("myFeature");

		final Artifact artifact = getFeatureArtifact();
		m_mojo.addBundleArtifact(featuresBuilder, projectFeatureBuilder, artifact);

		final List<? extends Feature> features = featuresBuilder.getFeatures().getFeature();
		assertNotNull(features);
		assertEquals(1, features.size());
		assertEquals("vaadin", features.get(0).getName());
		assertEquals("6.7.3", features.get(0).getVersion());
	}

	@Test
	public void testPomDependency() throws Exception {
		final FeaturesBuilder featuresBuilder = new FeaturesBuilder("myFeature");
		final FeatureBuilder projectFeatureBuilder = featuresBuilder.createFeature("myFeature");

		final Artifact artifact = getPomArtifact();
		m_mojo.addBundleArtifact(featuresBuilder, projectFeatureBuilder, artifact);

		final List<Dependency> features = projectFeatureBuilder.getFeature().getFeature();
		assertNotNull(features);
		assertEquals(0, features.size());
	}

	@Test
	public void testAlreadyWrapped() throws Exception {
		final FeaturesBuilder featuresBuilder = new FeaturesBuilder("myFeature");
		final FeatureBuilder projectFeatureBuilder = featuresBuilder.createFeature("myFeature");
		
		final Artifact artifact = getJarArtifact();
		m_mojo.addBundle(projectFeatureBuilder, "wrap:mvn:org.opennms.core.test-api/org.opennms.core.test-api.lib/1.10.4-SNAPSHOT");
		m_mojo.addBundleArtifact(featuresBuilder, projectFeatureBuilder, artifact);
		
		final List<BundleInfo> bundles = projectFeatureBuilder.getFeature().getBundles();
		assertNotNull(bundles);
		assertEquals(1, bundles.size());
	}

	private Artifact getBundleArtifact() {
		final File file = new File("src/test/resources/bundle.jar");
		final String groupId = "org.opennms.core";
		final String artifactId = "org.opennms.core.api";
		final String classifier = "";
		final String type = "jar";
		final String version = "1.11.1-SNAPSHOT";
		final String scope = "compile";
		final Artifact artifact = createArtifact(file, groupId, artifactId, version, type, classifier, scope);
		return artifact;
	}

	private Artifact getJarArtifact() {
		final File file = new File("src/test/resources/jar.jar");
		final String groupId = "org.opennms.core.test-api";
		final String artifactId = "org.opennms.core.test-api.lib";
		final String classifier = "";
		final String type = "jar";
		final String version = "1.10.4-SNAPSHOT";
		final String scope = "compile";
		final Artifact artifact = createArtifact(file, groupId, artifactId, version, type, classifier, scope);
		return artifact;
	}

	private Artifact getFeatureArtifact() {
		final File file = new File("src/test/resources/vaadin.xml");
		final String groupId = "com.example.features";
		final String artifactId = "vaadin";
		final String classifier = "features";
		final String type = "xml";
		final String version = "6.7.3";
		final String scope = "compile";
		final Artifact artifact = createArtifact(file, groupId, artifactId, version, type, classifier, scope);
		return artifact;
	}

	private Artifact getPomArtifact() {
		final File file = new File("pom.xml");
		final String groupId = "com.example.features";
		final String artifactId = "pomOnly";
		final String classifier = "";
		final String type = "pom";
		final String version = "1.0";
		final String scope = "compile";
		final Artifact artifact = createArtifact(file, groupId, artifactId, version, type, classifier, scope);
		return artifact;
	}

	private Artifact createArtifact(final File file, final String groupId, final String artifactId, final String version, final String type, final String classifier, final String scope) {
		final ArtifactHandler handler = new DefaultArtifactHandler(type);
		final DefaultArtifact artifact = new DefaultArtifact(groupId, artifactId, version, scope, type, classifier, handler);
		artifact.setFile(file);
		return artifact;
	}

	@SuppressWarnings("unused")
	private void debug(final String string) {
		for (final String line : string.trim().split("\n")) {
			System.err.println("[DEBUG] " + line);
		}
	}
}
