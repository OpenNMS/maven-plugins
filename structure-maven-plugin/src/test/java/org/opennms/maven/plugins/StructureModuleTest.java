package org.opennms.maven.plugins;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class StructureModuleTest {
    @Rule
    public MojoRule rule = new MojoRule();

    @Test
    @Ignore
    public void canRenderGraph() throws Exception {
        File pom = new File( "target/test-classes/project-to-test/" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        StructureMojo myMojo = (StructureMojo) rule.lookupConfiguredMojo( pom, "structure" );
        assertNotNull( myMojo );
        myMojo.execute();

        File outputDirectory = ( File ) rule.getVariableValueFromObject( myMojo, "outputDirectory" );
        assertNotNull( outputDirectory );
        assertTrue( outputDirectory.exists() );

        File graph = new File( outputDirectory, "structure-graph.json" );
        assertTrue( graph.exists() );
    }

}

