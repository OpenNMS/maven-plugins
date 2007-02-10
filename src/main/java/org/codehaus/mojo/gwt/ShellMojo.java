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
 * Goal which runs the GWT development shell.
 *
 * @goal shell
 * @requiresDependencyResolution compile
 * @author Shinobu Kawai
 */
public class ShellMojo
    extends AbstractGWTMojo
{

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        // Run the tool
        runGWT( "com.google.gwt.dev.GWTShell", ( getModuleName() + "/" + getHomePage() ) );
    }
}
