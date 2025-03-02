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

import eu.cdevreeze.mqutilities.JmsContextToJsonObjectFunction;
import eu.cdevreeze.mqutilities.JmsContextToJsonObjectFunctionFactory;
import eu.cdevreeze.mqutilities.qualifier.connection.SSLConnection;
import eu.cdevreeze.mqutilities.qualifier.connection.SimpleConnection;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Console program using a {@link JmsContextToJsonObjectFunction}. The first program argument
 * is the fully-qualified class name of the {@link JmsContextToJsonObjectFunctionFactory},
 * and the remaining program arguments are passed to the {@link JmsContextToJsonObjectFunctionFactory}
 * to create a {@link JmsContextToJsonObjectFunction}, which is subsequently run.
 *
 * @author Chris de Vreeze
 */
public class JmsProgramReturningJson {

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

            Instance<JmsContextToJsonObjectFunctionFactory> functionFactoryInstance =
                    CDI.current().select(JmsContextToJsonObjectFunctionFactory.class, Any.Literal.INSTANCE);

            JmsContextToJsonObjectFunctionFactory functionFactory = functionFactoryInstance
                    .stream()
                    .filter(factory -> factoryNameMatches(factory, fqcnOfFactory))
                    .findFirst()
                    .orElseThrow();

            JmsContextToJsonObjectFunction function = functionFactory.apply(factoryArgs);

            // Do the actual work within a JMSContext
            JsonObject result;
            try (JMSContext jmsContext = cfInstance.get().createContext()) {
                result = function.apply(jmsContext);
            }

            StringWriter sw = new StringWriter();
            Map<String, Object> props = new HashMap<>();
            props.put(JsonGenerator.PRETTY_PRINTING, true);
            JsonWriterFactory jsonWriterFactory = Json.createWriterFactory(props);
            try (JsonWriter jsonWriter = jsonWriterFactory.createWriter(sw)) {
                jsonWriter.writeObject(result);
            }
            String resultAsString = sw.toString();

            System.out.println(resultAsString);
        }
    }

    // TODO Improve. These are hacks

    private static boolean factoryNameMatches(
            JmsContextToJsonObjectFunctionFactory factory,
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
