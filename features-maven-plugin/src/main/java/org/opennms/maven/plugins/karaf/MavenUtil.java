package org.opennms.maven.plugins.karaf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;

public class MavenUtil {
    private static final Pattern mvnPattern = Pattern.compile("(?:wrap\\:)?mvn:([^/ ]+)/([^/ ]+)/([^/ ]*)(/([^/ ]+)(/([^/ ]+))?)?");

    /**
     * Convert an Artifact into a PAX URL mvn format.
     *
     * @param artifact the Artifact.
     * @return the corresponding PAX URL mvn format (mvn:<groupId>/<artifactId>/<version>/<type>/<classifier>)
     */
    public static String artifactToMvn(Artifact artifact) {
        String bundleName;
        if (artifact.getType().equals("jar") && isEmpty(artifact.getClassifier())) {
            bundleName = String.format("mvn:%s/%s/%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion());
        } else {
            if (isEmpty(artifact.getClassifier())) {
                bundleName = String.format("mvn:%s/%s/%s/%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getType());
            } else {
                bundleName = String.format("mvn:%s/%s/%s/%s/%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getType(), artifact.getClassifier());
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

    static String getFileName(final Artifact artifact) {
        return artifact.getArtifactId() + "-" + artifact.getBaseVersion() + (artifact.getClassifier() != null ? "-" + artifact.getClassifier() : "") + "." + artifact.getType();
    }

    static String getDir(final Artifact artifact) {
        return artifact.getGroupId().replace('.', '/') + "/" + artifact.getArtifactId() + "/" + artifact.getBaseVersion() + "/";
    }

    /**
     * Generate the maven-metadata-local.xml for the given Maven <code>Artifact</code>.
     *
     * @param artifact the Maven <code>Artifact</code>.
     * @param target   the target maven-metadata-local.xml file to generate.
     * @throws IOException if the maven-metadata-local.xml can't be generated.
     */
    static void generateMavenMetadata(Artifact artifact, File target) throws IOException {
        target.getParentFile().mkdirs();
        Metadata metadata = new Metadata();
        metadata.setGroupId(artifact.getGroupId());
        metadata.setArtifactId(artifact.getArtifactId());
        metadata.setVersion(artifact.getVersion());
        metadata.setModelVersion("1.1.0");

        Versioning versioning = new Versioning();
        versioning.setLastUpdatedTimestamp(new Date(System.currentTimeMillis()));
        Snapshot snapshot = new Snapshot();
        //snapshot.setLocalCopy(true);
        versioning.setSnapshot(snapshot);
        SnapshotVersion snapshotVersion = new SnapshotVersion();
        snapshotVersion.setClassifier(artifact.getClassifier());
        snapshotVersion.setVersion(artifact.getVersion());
        snapshotVersion.setExtension(artifact.getType());
        snapshotVersion.setUpdated(versioning.getLastUpdated());
        versioning.addSnapshotVersion(snapshotVersion);

        metadata.setVersioning(versioning);

        MetadataXpp3Writer metadataWriter = new MetadataXpp3Writer();
        Writer writer = new FileWriter(target);
        metadataWriter.write(writer, metadata);
    }
}
