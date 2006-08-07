package com.agilejava.docbkx.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.messaging.MessageHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.xml.sax.InputSource;

import com.agilejava.docbkx.maven.AbstractTransformerMojo;

/**
 * A replacement base class, to be inherited by the FO building plugin. This
 * base class will generate PDF from the FO output by overriding
 * {@link #postProcessResult(File)}.
 * 
 * @author Wilfred Springer
 * 
 */
public abstract class AbstractPdfMojo extends AbstractTransformerMojo {

    public void postProcessResult(File result) throws MojoExecutionException {
        super.postProcessResult(result);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = openFileForInput(result);
            out = openFileForOutput(getOutputFile(result));
            Logger logger = new AvalonMavenBridgeLogger(getLog(), true, true);
            MessageHandler.setScreenLogger(logger);
            Driver driver = new Driver(new InputSource(in), out);
            driver.setLogger(logger);
            driver.setRenderer(Driver.RENDER_PDF);
            driver.run();
        } catch (FOPException fope) {
            throw new MojoExecutionException("Failed to convert to PDF", fope);
        } catch (IOException ioe) {
            throw new MojoExecutionException("Failed to write to output file.");
        }
    }

    private InputStream openFileForInput(File file)
            throws MojoExecutionException {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException fnfe) {
            throw new MojoExecutionException("Failed to open " + file
                    + " for input.");
        }
    }

    private File getOutputFile(File inputFile) {
        String basename = FileUtils.basename(inputFile.getAbsolutePath());
        return new File(getTargetDirectory(), basename + "pdf");
    }

    private OutputStream openFileForOutput(File file)
            throws MojoExecutionException {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException fnfe) {
            throw new MojoExecutionException("Failed to open " + file
                    + " for output.");
        }
    }

}
