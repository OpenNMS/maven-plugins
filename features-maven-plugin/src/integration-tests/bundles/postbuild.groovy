import org.opennms.maven.plugins.karaf.Validator;

System.err.println("basedir = " + basedir);
System.err.println("localRepositoryPath = " + localRepositoryPath);
System.err.println("context = " + context);

final File expectedXml = new File(basedir, "src/main/resources/features.xml");
if (!expectedXml.exists()) {
	throw new Exception(expectedXml + " does not exist!");
}
final File featuresXml = new File(basedir, "target/features/features.xml");
if (!featuresXml.exists()) {
	throw new Exception(featuresXml + " does not exist!");
}

Validator.assertXmlEquals(expectedXml, featuresXml);