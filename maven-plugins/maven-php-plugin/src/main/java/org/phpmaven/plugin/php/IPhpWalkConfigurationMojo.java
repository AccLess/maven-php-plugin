/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.phpmaven.plugin.php;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;


/**
 * Interface for mojos that are aware to be configured for walk helpers. Mainly introduces Getters.
 * 
 * @author Martin Eisengardt
 */
public interface IPhpWalkConfigurationMojo {

    /**
     * Returns files and directories to exclude.
     * @return files and directories to exclude.
     */
    String[] getExcludes();

    /**
     * Returns files and directories to include.
     * @return files and directories to include.
     */
    String[] getIncludes();

    /**
     * Returns how php files will be identified after the last point.
     * @return how php files will be identified after the last point.
     */
    String getPhpFileEnding();
    
    /**
     * Returns the maven project we are configured on.
     * 
     * @return The maven project.
     */
    MavenProject getProject();
    
    /**
     * Returns the logging delegate.
     * 
     * @return logging delegate.
     */
    Log getLog();
    
    /**
     * Returns the The Maven session to be used.
     * @return the maven session.
     */
    MavenSession getSession();
    

}
