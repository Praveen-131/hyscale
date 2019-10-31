/**
 * Copyright 2019 Pramati Prism, Inc.
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
package io.hyscale.controller.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import io.hyscale.builder.core.models.ImageBuilder;
import io.hyscale.builder.services.config.ImageBuilderConfig;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.controller.commands.HyscaleCtlCommand;
import io.hyscale.controller.core.exception.ControllerErrorCodes;
import io.hyscale.controller.util.ExceptionHandler;
import io.hyscale.controller.util.ShutdownHook;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.ParameterException;

/**
 * Starting point for the hyscale tool
 * <p>
 * This class is responsible for initializing the spring application context
 * and execute the given commands. It works on top of picoli
 * {@See <a href="https://picocli.info/">https://picocli.info/</a>}
 *
 * </p>
 */
@SpringBootApplication
@ComponentScan("io.hyscale")
public class HyscaleCtlInitializer implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(HyscaleCtlInitializer.class);

    @Autowired
    private IFactory factory;
    
    @Autowired
    private ExceptionHandler exceptionHandler;

    static {
        System.setProperty(ImageBuilderConfig.IMAGE_BUILDER_PROP, ImageBuilder.LOCAL.name());
        System.setProperty(ToolConstants.HYSCALECTL_LOGS_DIR_PROPERTY, SetupConfig.getToolLogDir());
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(HyscaleCtlInitializer.class);
        app.run(args);
    }

    public void run(String... args) {

        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
            CommandLine commandLine = new CommandLine(new HyscaleCtlCommand(), factory);
            commandLine.setExecutionExceptionHandler(exceptionHandler);
            commandLine.execute(args);
        } catch (ParameterException e) {
            logger.error("Error while processing command, error {}", ControllerErrorCodes.INVALID_COMMAND.getErrorMessage(), e);
        } catch (Throwable e) {
        	logger.error("Unexpected error in processing command, error {}", ControllerErrorCodes.UNEXPECTED_ERROR.getErrorMessage(), e);
        }
    }

}