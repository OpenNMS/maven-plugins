package org.opennms.maven.plugins.karaf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;

public class MavenUtil {
    private static final Pattern mvnPattern = Pattern.compile("(?:wrap\\:)?mvn:([^/ ]+)/([^/ ]+)/([^/ ]*)(/([^/ ]+)(/([^/ ]+))?)?");

    /**
     * Convert a Maven <code>Artifact</code> into a PAX URL mvn format.
     *
     * @param artifact the Maven <code>Artifact</code>.
     * @return the corresponding PAX URL mvn format (mvn:<groupId>/<artifactId>/<version>/<type>/<classifier>)
     */
	public static String artifactToMvn(Artifact artifact) {
        return artifactToMvn(RepositoryUtils.toArtifact(artifact));
    }

    /**
     * Convert an Aether <code>org.sonatype.aether.artifact.Artifact</code> into a PAX URL mvn format.
     *
     * @param artifact the Aether <code>org.sonatype.aether.artifact.Artifact</code>.
     * @return the corresponding PAX URL mvn format (mvn:<groupId>/<artifactId>/<version>/<type>/<classifier>)
     */
	public static String artifactToMvn(org.sonatype.aether.artifact.Artifact artifact) {
        String bundleName;
        if (artifact.getExtension().equals("jar") && isEmpty(artifact.getClassifier())) {
            bundleName = String.format("mvn:%s/%s/%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion());
        } else {
            if (isEmpty(artifact.getClassifier())) {
                bundleName = String.format("mvn:%s/%s/%s/%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getExtension());
            } else {
                bundleName = String.format("mvn:%s/%s/%s/%s/%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getExtension(), artifact.getClassifier());
            }
        }
        return bundleName;
    }

	private static boolean isEmpty(final String classifier) {
        return classifier == null || classifier.length() == 0;
    }

	public static Artifact mvnToArtifact(final String location) {
        Matcher m = mvnPattern.matcher(location);
        if (!m.matches()) {
        	return null;
        }
        
        final String groupId    = m.group(1);
        final String artifactId = m.group(2);
        final String version    = m.group(3);
        final String type       = m.group(5) == null? "jar":m.group(5);
        final String classifier = m.group(7);

        final ArtifactHandler handler = new DefaultArtifactHandler(type);
        final Artifact artifact = new DefaultArtifact(groupId, artifactId, version, "compile", type, classifier, handler);
        
        return artifact;
	}
}
