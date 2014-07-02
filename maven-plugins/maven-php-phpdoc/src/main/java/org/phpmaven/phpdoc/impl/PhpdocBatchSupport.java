/**
 * Copyright 2010-2012 by PHP-maven.org
 * 
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

package org.phpmaven.phpdoc.impl;

import java.io.File;
import java.io.IOException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.phpmaven.core.BuildPluginConfiguration;
import org.phpmaven.core.ConfigurationParameter;
import org.phpmaven.core.IComponentFactory;
import org.phpmaven.exec.IPhpExecutableConfiguration;
import org.phpmaven.phpdoc.IPhpdocRequest;
import org.phpmaven.phpdoc.IPhpdocSupport;
import org.phpmaven.phpexec.library.IPhpExecutable;
import org.phpmaven.phpexec.library.PhpCoreException;
import org.phpmaven.phpexec.library.PhpErrorException;
import org.phpmaven.phpexec.library.PhpException;
import org.phpmaven.phpexec.library.PhpWarningException;

/**
 * Implementation of phpdoc support invoking the phpdoc batch.
 * 
 * @author Martin Eisengardt <Martin.Eisengardt@googlemail.com>
 * @since 2.0.0
 */
@Component(role = IPhpdocSupport.class, instantiationStrategy = "per-lookup", hint = "PHP_EXE")
@BuildPluginConfiguration(groupId = "org.github.phpmaven", artifactId = "maven-php-phpdoc", filter = {
        "phpdocService", "installPhpdoc", "installFolder"
        })
public class PhpdocBatchSupport extends AbstractPhpdocSupport {

    /**
     * Path to phpDoc. If nothing is configured phpdoc is expected in the path.
     */
    @Configuration(name = "phpDocFilePath", value = "phpdoc")
    private String phpDocFilePath;

    /**
     * The phpdoc configuraton file. The default is ${project.basedir}/src/site/phpdoc/phpdoc.config
     */
    @ConfigurationParameter(name = "phpDocConfigFile", expression = "${project.basedir}/src/site/phpdoc/phpdoc.config")
    private File phpDocConfigFile;

    /**
     * The generated phpDoc file.
     */
    @ConfigurationParameter(
            name = "generatedPhpDocConfigFile",
            expression = "${project.build.directory}/temp/phpdoc/phpdoc.ini")
    private File generatedPhpDocConfigFile;
    
    /**
     * The executable config.
     */
    @Configuration(name = "executableConfig", value = "")
    private Xpp3Dom executableConfig;
    
    /**
     * The component factory.
     */
    @Requirement
    private IComponentFactory factory;
    
    /**
     * The maven session.
     */
    @ConfigurationParameter(name = "session", expression = "${session}")
    private MavenSession session;
    
    /**
     * The phpdoc version to be used.
     */
    @Configuration(name = "phpdocVersion", value = "1.4.2")
    private String phpdocVersion;
    
    /**
     * The additional arguments passed to phpdoc.
     */
    @Configuration(name = "arguments", value = "")
    private String arguments;

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateReport(Log log, IPhpdocRequest request) throws PhpException {
        try {
            final IPhpExecutable exec = this.factory.lookup(
                    IPhpExecutableConfiguration.class,
                    this.executableConfig,
                    this.session).getPhpExecutable();
            
            if (this.phpdocVersion.startsWith("1.")) {
                writeIni(log, request, phpDocConfigFile, generatedPhpDocConfigFile);
            } else {
                writeXml(log, request, phpDocConfigFile, generatedPhpDocConfigFile);
            }
            final String path = System.getProperty("java.library.path") + File.pathSeparator + System.getenv("PATH");
            log.debug("PATH: " + path);
            final String[] paths = path.split(File.pathSeparator);
            File phpDocFile = null;
            if ("phpdoc".equals(phpDocFilePath)) {
                for (int i = 0; i < paths.length; i++) {
                    final File file = new File(paths[i], "phpdoc");
                    if (file.isFile()) {
                        phpDocFile = file;
                        break;
                    }
                }
            } else {
                phpDocFile = new File(phpDocFilePath);
            }
            if (phpDocFile == null || !phpDocFile.isFile()) {
                throw new PhpCoreException("phpdoc not found in path");
            }
            String command = "\"" + phpDocFile + "\" -c \"" + generatedPhpDocConfigFile.getAbsolutePath() + "\"";
            if (arguments != null && arguments.length() > 0) {
                command += " " + arguments;
            }
            log.debug("Executing PHPDocumentor: " + command);
            // XXX: commandLine.setWorkingDirectory(phpDocFile.getParent());
            String result;
            try {
                result = exec.execute(command, phpDocFile);
            } catch (PhpWarningException ex) {
                result = ex.getAppendedOutput();
                // silently ignore; only errors are important
            }
            for (final String line : result.split("\n")) {
                if (line.startsWith("ERROR:")) {
                    // this is a error of phpdocumentor.
                    log.error("Got error from php-documentor. " +
                        "Enable debug (-X) to fetch the php output.\n" +
                        line);
                    throw new PhpErrorException(phpDocFile, line);
                }
            }
        } catch (PlexusConfigurationException ex) {
            throw new PhpCoreException("Errors invoking phpdoc", ex);
        } catch (IOException ex) {
            throw new PhpCoreException("Errors invoking phpdoc", ex);
        } catch (ComponentLookupException ex) {
            throw new PhpCoreException("Errors invoking phpdoc", ex);
        }
    }

}
