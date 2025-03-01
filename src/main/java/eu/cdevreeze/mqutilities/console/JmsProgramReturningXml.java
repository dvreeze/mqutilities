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

import eu.cdevreeze.mqutilities.ConnectionFactorySupplier;
import eu.cdevreeze.mqutilities.JmsContextToElementFunction;
import eu.cdevreeze.mqutilities.JmsContextToElementFunctionFactory;
import eu.cdevreeze.yaidom4j.dom.immutabledom.Element;
import eu.cdevreeze.yaidom4j.dom.immutabledom.jaxpinterop.DocumentPrinter;
import eu.cdevreeze.yaidom4j.dom.immutabledom.jaxpinterop.DocumentPrinters;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Console program using a {@link JmsContextToElementFunction}. The first program argument
 * is the fully-qualified class name of the {@link JmsContextToElementFunctionFactory},
 * and the remaining program arguments are passed to the {@link JmsContextToElementFunctionFactory}
 * to create a {@link JmsContextToElementFunction}, which is subsequently run.
 *
 * @author Chris de Vreeze
 */
public class JmsProgramReturningXml {

    public static void main(String... args) throws Exception {
        Objects.checkIndex(0, args.length);
        String fqcnOfFactory = args[0];
        // The remaining arguments typically include a queue name, such as DEV.QUEUE.1
        List<String> factoryArgs = Arrays.stream(args).skip(1).toList();

        String fqcnOfCFSupplier = Objects.requireNonNull(System.getProperty("ConnectionFactorySupplierClass"));

        @SuppressWarnings("unchecked")
        Class<ConnectionFactorySupplier> cfSupplierClass = (Class<ConnectionFactorySupplier>) Class.forName(fqcnOfCFSupplier);

        ConnectionFactorySupplier cfSupplier = cfSupplierClass.getDeclaredConstructor().newInstance();
        ConnectionFactory cf = cfSupplier.get();

        @SuppressWarnings("unchecked")
        Class<JmsContextToElementFunctionFactory> factoryClass =
                (Class<JmsContextToElementFunctionFactory>) Class.forName(fqcnOfFactory);

        JmsContextToElementFunctionFactory factory = factoryClass.getDeclaredConstructor()
                .newInstance();

        JmsContextToElementFunction function = factory.apply(factoryArgs);

        // Do the actual work within a JMSContext
        Element result;
        try (JMSContext jmsContext = cf.createContext()) {
            result = function.apply(jmsContext);
        }

        DocumentPrinter docPrinter = DocumentPrinters.instance();
        String xmlString = docPrinter.print(result);

        System.out.println(xmlString);
    }
}
