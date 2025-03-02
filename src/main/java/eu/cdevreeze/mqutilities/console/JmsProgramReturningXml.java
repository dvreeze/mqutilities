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

import eu.cdevreeze.mqutilities.JmsContextToElementFunction;
import eu.cdevreeze.mqutilities.JmsContextToElementFunctionFactory;
import eu.cdevreeze.mqutilities.qualifier.connection.SSLConnection;
import eu.cdevreeze.mqutilities.qualifier.connection.SimpleConnection;
import eu.cdevreeze.yaidom4j.dom.immutabledom.Element;
import eu.cdevreeze.yaidom4j.dom.immutabledom.jaxpinterop.DocumentPrinter;
import eu.cdevreeze.yaidom4j.dom.immutabledom.jaxpinterop.DocumentPrinters;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Console program using a {@link JmsContextToElementFunction}. The first program argument
 * is the fully-qualified class name of the {@link JmsContextToElementFunctionFactory},
 * and the remaining program arguments are passed to the {@link JmsContextToElementFunctionFactory}
 * to create a {@link JmsContextToElementFunction}, which is subsequently run.
 *
 * @author Chris de Vreeze
 */
public class JmsProgramReturningXml {

    public static void main(String... args) {
        Objects.checkIndex(0, args.length);
        String fqcnOfFactory = args[0];
        // The remaining arguments typically include a queue name, such as DEV.QUEUE.1
        List<String> factoryArgs = Arrays.stream(args).skip(1).toList();

        Weld weld = new Weld();

        try (WeldContainer weldContainer = weld.initialize()) {
            // Config config = ConfigProvider.getConfig();

            Annotation connectionQualifier = getConnectionQualifier();
            Instance<ConnectionFactory> cfInstance = CDI.current().select(ConnectionFactory.class, connectionQualifier);

            Instance<JmsContextToElementFunctionFactory> functionFactoryInstance =
                    CDI.current().select(JmsContextToElementFunctionFactory.class, Any.Literal.INSTANCE);

            JmsContextToElementFunctionFactory functionFactory = functionFactoryInstance
                    .stream()
                    .filter(factory -> factoryNameMatches(factory, fqcnOfFactory))
                    .findFirst()
                    .orElseThrow();

            JmsContextToElementFunction function = functionFactory.apply(factoryArgs);

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

    // TODO Improve. These are hacks

    private static boolean factoryNameMatches(
            JmsContextToElementFunctionFactory factory,
            String fqcnOfFactory
    ) {
        try {
            return factory.getClass().getSimpleName().startsWith(Class.forName(fqcnOfFactory).getSimpleName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Annotation getConnectionQualifier() {
        if (Optional.ofNullable(System.getProperty("javax.net.ssl.keyStore")).isPresent()) {
            return new AnnotationLiteral<SSLConnection>() {
            };
        } else {
            return new AnnotationLiteral<SimpleConnection>() {
            };
        }
    }
}
