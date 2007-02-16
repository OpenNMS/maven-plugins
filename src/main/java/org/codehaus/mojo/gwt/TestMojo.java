package org.codehaus.mojo.gwt;

/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which compiles a GWT project.
 *
 * @goal test
 * @phase test
 * @requiresDependencyResolution test
 * @author Bob Allison
 */
public class TestMojo
    extends AbstractGWTMojo
{
    
    /**
     * The GWT test suite class.
     *
     * @parameter
     * @required
     */
    private String testClass;

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        requireGWTDirectory("test");
        // Run the tool
        GWTCommand gwt = GWTCommand.create("junit.textui.TestRunner", testClass, null);
        configure(gwt, true);
        
        gwt.getJUnit();
    }
}
