package org.opennms.maven.plugins.karaf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.karaf.features.BundleInfo;
import org.apache.karaf.features.internal.model.Dependency;
import org.apache.maven.monitor.logging.DefaultLog;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifactType;

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
		final FeatureBuilder fb = new FeatureBuilder("myFeature");

		final org.sonatype.aether.artifact.Artifact artifact = getBundleArtifact();
		m_mojo.addArtifact(fb, artifact);
		
		final List<BundleInfo> bundles = fb.getFeature().getBundles();
		assertNotNull(bundles);
		assertEquals(1, bundles.size());
		assertEquals("mvn:org.opennms.core/org.opennms.core.api/1.11.1-SNAPSHOT", bundles.get(0).getLocation());
	}

	@Test
	public void testJarDependency() throws Exception {
		final FeatureBuilder fb = new FeatureBuilder("myFeature");

		final org.sonatype.aether.artifact.Artifact artifact = getJarArtifact();
		m_mojo.addArtifact(fb, artifact);
		
		final List<BundleInfo> bundles = fb.getFeature().getBundles();
		assertNotNull(bundles);
		assertEquals(1, bundles.size());
		assertEquals("wrap:mvn:org.opennms.core.test-api/org.opennms.core.test-api.lib/1.10.4-SNAPSHOT", bundles.get(0).getLocation());
	}

	@Test
	public void testFeatureDependency() throws Exception {
		final FeatureBuilder fb = new FeatureBuilder("myFeature");

		final org.sonatype.aether.artifact.Artifact artifact = getFeatureArtifact();
		m_mojo.addArtifact(fb, artifact);

		final List<Dependency> features = fb.getFeature().getFeature();
		assertNotNull(features);
		assertEquals(1, features.size());
		assertEquals("vaadin", features.get(0).getName());
		assertEquals("6.7.3", features.get(0).getVersion());
	}

	@Test
	public void testPomDependency() throws Exception {
		final FeatureBuilder fb = new FeatureBuilder("myFeature");
		final org.sonatype.aether.artifact.Artifact artifact = getPomArtifact();
		m_mojo.addArtifact(fb, artifact);

		final List<Dependency> features = fb.getFeature().getFeature();
		assertNotNull(features);
		assertEquals(0, features.size());
	}

	@Test
	public void testAddLocalDependencies() throws Exception {
		final FeatureBuilder fb = new FeatureBuilder("myFeature");

		final Map<Artifact,String> localDependencies = new HashMap<Artifact,String>();
		localDependencies.put(getBundleArtifact(), "compile");
		localDependencies.put(getJarArtifact(), "compile");
		localDependencies.put(getFeatureArtifact(), "compile");

		m_mojo.addLocalDependencies(fb, localDependencies);

		final List<Dependency> features = fb.getFeature().getFeature();
		assertNotNull(features);
		assertEquals(1, features.size());
		
		final List<BundleInfo> bundles = fb.getFeature().getBundles();
		assertNotNull(bundles);
		assertEquals(2, bundles.size());
	}

	@Test
	public void testAddLocalDependenciesWithIgnoredScopes() throws Exception {
		final FeatureBuilder fb = new FeatureBuilder("myFeature");
		
		final Map<Artifact,String> localDependencies = new LinkedHashMap<Artifact,String>();
		localDependencies.put(getBundleArtifact(), "test");      // skipped
		localDependencies.put(getFeatureArtifact(), "provided"); // skipped
		localDependencies.put(getJarArtifact(), "runtime");      // added

		m_mojo.addLocalDependencies(fb, localDependencies);
		
		final List<BundleInfo> bundles = fb.getFeature().getBundles();
		final List<Dependency> features = fb.getFeature().getFeature();
		assertNotNull(bundles);
		assertNotNull(features);
		assertEquals(1, bundles.size());
		assertEquals(0, features.size());
		assertEquals("wrap:mvn:org.opennms.core.test-api/org.opennms.core.test-api.lib/1.10.4-SNAPSHOT", bundles.get(0).getLocation());
	}

	@Test
	public void testAddLocalDependenciesWithNonStandardIgnoredScopes() throws Exception {
		final ArrayList<String> ignored = new ArrayList<String>();
		ignored.add("compile");
		m_mojo.setIgnoredScopes(ignored);

		final FeatureBuilder fb = new FeatureBuilder("myFeature");
		
		final Map<Artifact,String> localDependencies = new LinkedHashMap<Artifact,String>();
		localDependencies.put(getBundleArtifact(), "test");      // added
		localDependencies.put(getFeatureArtifact(), "provided"); // added
		localDependencies.put(getJarArtifact(), "runtime");      // added

		m_mojo.addLocalDependencies(fb, localDependencies);
		
		final List<BundleInfo> bundles = fb.getFeature().getBundles();
		final List<Dependency> features = fb.getFeature().getFeature();
		assertNotNull(bundles);
		assertNotNull(features);
		assertEquals(2, bundles.size());
		assertEquals(1, features.size());
		assertEquals("mvn:org.opennms.core/org.opennms.core.api/1.11.1-SNAPSHOT", bundles.get(0).getLocation());
		assertEquals("wrap:mvn:org.opennms.core.test-api/org.opennms.core.test-api.lib/1.10.4-SNAPSHOT", bundles.get(1).getLocation());
	}

	private org.sonatype.aether.artifact.Artifact getBundleArtifact() {
		final File file = new File("src/test/resources/bundle.jar");
		final org.sonatype.aether.artifact.Artifact artifact = new org.sonatype.aether.util.artifact.DefaultArtifact("org.opennms.core", "org.opennms.core.api", null, "jar", "1.11.1-SNAPSHOT", new DefaultArtifactType("jar", "jar", "", "")).setFile(file);
		return artifact;
	}

	private org.sonatype.aether.artifact.Artifact getJarArtifact() {
		final File file = new File("src/test/resources/jar.jar");
		final org.sonatype.aether.artifact.Artifact artifact = new org.sonatype.aether.util.artifact.DefaultArtifact("org.opennms.core.test-api", "org.opennms.core.test-api.lib", "", "jar", "1.10.4-SNAPSHOT", new DefaultArtifactType("jar", "jar", "", "")).setFile(file);
		return artifact;
	}

	private org.sonatype.aether.artifact.Artifact getFeatureArtifact() {
		final File file = new File("src/test/resources/vaadin.xml");
		final org.sonatype.aether.artifact.Artifact artifact = new org.sonatype.aether.util.artifact.DefaultArtifact("com.example.features", "vaadin", "features", "xml", "6.7.3", new DefaultArtifactType("xml", "xml", "features", "")).setFile(file);
		return artifact;
	}

	private org.sonatype.aether.artifact.Artifact getPomArtifact() {
		final org.sonatype.aether.artifact.Artifact artifact = new org.sonatype.aether.util.artifact.DefaultArtifact("com.example.features", "pomOnly", "", "pom", "1.0", new DefaultArtifactType("pom", "pom", "", "")).setFile(new File("pom.xml"));
		return artifact;
	}

	@SuppressWarnings("unused")
	private void debug(final String string) {
		for (final String line : string.trim().split("\n")) {
			System.err.println("[DEBUG] " + line);
		}
	}
}
