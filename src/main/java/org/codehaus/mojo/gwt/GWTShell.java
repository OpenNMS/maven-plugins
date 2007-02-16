package org.codehaus.mojo.gwt;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

public class GWTShell extends GWTCommand {
    
    public GWTShell(String url) {
        super("com.google.gwt.dev.GWTShell", url, (new String[0]));
    }

    public static GWTShell create(String url) {
        return new GWTShell(url);
    }

    void run() throws MojoExecutionException {
        validateGWTHome();
        super.run();
    }

    private void validateGWTHome() throws MojoExecutionException {
        if (getGWTDirectory() == null) {
            throw new MojoExecutionException("You must set gwtDirectory to run the gwt:shell goal");
        }
        
        File[] files = getGWTDirectory().listFiles();
        for ( int i = 0; i < files.length; i++ )
        {
            File f = files[i];
            if ( ( f.getName().endsWith( ".jar" ) ) && ( f.getName().startsWith("gwt-dev") ) )
            {
                return;
            }
        }
        
        throw new MojoExecutionException("gwtDirectory does not contain the gwt-dev-xxx.jar needed to run the GWTShell.  Make sure that you have set it correctly!");

    }
    
    

}
