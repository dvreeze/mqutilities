/*
 * Copyright 2025-2025 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.mqutilities.console;

import com.google.common.base.Preconditions;
import eu.cdevreeze.mqutilities.JmsContextToElementFunction;
import eu.cdevreeze.mqutilities.JmsContextToElementFunctionFactory;
import eu.cdevreeze.mqutilities.qualifier.connection.ConnectionType;
import eu.cdevreeze.mqutilities.qualifier.connection.HasConnectionTypeQualifier;
import eu.cdevreeze.yaidom4j.dom.immutabledom.Element;
import eu.cdevreeze.yaidom4j.dom.immutabledom.jaxpinterop.DocumentPrinter;
import eu.cdevreeze.yaidom4j.dom.immutabledom.jaxpinterop.DocumentPrinters;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Console program using a {@link JmsContextToElementFunction}. The first program argument
 * is the name of the {@link JmsContextToElementFunction} to create and run,
 * and the remaining program arguments are passed to the {@link JmsContextToElementFunctionFactory}
 * to create a {@link JmsContextToElementFunction}, which is subsequently run.
 *
 * @author Chris de Vreeze
 */
public class JmsProgramReturningXml {

    public static void main(String... args) {
        Objects.checkIndex(0, args.length);
        String jmsContextFunctionName = args[0];
        // The remaining arguments typically include a queue name, such as DEV.QUEUE.1
        List<String> factoryArgs = Arrays.stream(args).skip(1).toList();

        Weld weld = new Weld();

        try (WeldContainer weldContainer = weld.initialize()) {
            Config config = ConfigProvider.getConfig();

            // Typically, system property "connectionType" has been passed to the program
            ConnectionType connectionType =
                    ConnectionType.parse(config.getConfigValue("connectionType").getValue());

            Annotation connectionQualifier = new HasConnectionTypeQualifier(connectionType);
            Instance<ConnectionFactory> cfInstance = CDI.current().select(ConnectionFactory.class, connectionQualifier);

            Preconditions.checkArgument(
                    cfInstance.isResolvable(),
                    String.format("Could not resolve ConnectionFactory with required qualifier '%s'", connectionQualifier)
            );

            Instance<JmsContextToElementFunctionFactory> functionFactoryInstance =
                    CDI.current().select(JmsContextToElementFunctionFactory.class, NamedLiteral.of(jmsContextFunctionName));

            Preconditions.checkArgument(
                    functionFactoryInstance.isResolvable(),
                    String.format("Could not resolve function with name '%s'", jmsContextFunctionName)
            );

            JmsContextToElementFunction function = functionFactoryInstance.get().apply(factoryArgs);

            // Do the actual work within a JMSContext
            Element result;
            try (JMSContext jmsContext = cfInstance.get().createContext()) {
                result = function.apply(jmsContext);
            }

            DocumentPrinter docPrinter = DocumentPrinters.instance();
            String xmlString = docPrinter.print(result);

            System.out.println(xmlString);
        }
    }
}
