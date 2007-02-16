package org.codehaus.mojo.gwt;

public class GWTCompiler extends GWTCommand {
    
    public static GWTCompiler create(String module) {
        return new GWTCompiler(module);
        
    }

    public GWTCompiler(String module) {
        super("com.google.gwt.dev.GWTCompiler", module, (new String[] { "-style", "PRETTY" }));
    }

    void addGWTJarsToClasspath() {
        addGWTJarsFromPluginDependencies();
    }
    
    

}
