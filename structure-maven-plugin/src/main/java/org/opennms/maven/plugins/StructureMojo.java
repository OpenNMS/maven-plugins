package org.opennms.maven.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.google.gson.Gson;

@Mojo(name = "structure", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class StructureMojo extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter( defaultValue = "${project.build.directory}", property = "outputDir", required = true )
    private File outputDirectory;

    /**
     * Name of the file.
     */
    @Parameter(defaultValue = "structure-graph.json")
    private String fileName;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        // Only execute in the root
        if (!project.isExecutionRoot()) {
            return;
        }

        // Map
        final List<StructureModule> modules = toModules(project);
        getLog().info(String.format("Built structure %d modules.", modules.size()));

        // Convert to JSON
        final Gson gson = new Gson();
        final String json = gson.toJson(modules);

        // Write to file
        final Path path = Paths.get(outputDirectory.getAbsolutePath(), fileName);
        try {
            getLog().info(String.format("Writing graph to: %s", path));
            Files.write(path, json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new MojoExecutionException("Error occurred while writing graph to: " + path, e);
        }
    }

    private static List<StructureModule> toModules(MavenProject project) {
        final List<StructureModule> modules = new LinkedList<>();
        modules.add(new StructureModule((project)));
        for (MavenProject child : project.getCollectedProjects()) {
            modules.add(new StructureModule(child));
        }
        return modules;
    }

}
