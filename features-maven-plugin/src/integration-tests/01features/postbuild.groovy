import org.opennms.maven.plugins.karaf.Validator;

final File expectedXml = new File(basedir, "src/main/resources/features.xml");
final File featuresXml = new File(basedir, "target/features/features.xml");

Validator.assertXmlEquals(expectedXml, featuresXml);