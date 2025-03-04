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
import eu.cdevreeze.mqutilities.JmsContextToJsonObjectFunction;
import eu.cdevreeze.mqutilities.JmsContextToJsonObjectFunctionFactory;
import eu.cdevreeze.mqutilities.qualifier.connection.ConnectionType;
import eu.cdevreeze.mqutilities.qualifier.connection.HasConnectionTypeQualifier;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.microprofile.config.Config;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Console program using a {@link JmsContextToJsonObjectFunction}. The first program argument
 * is the name of the {@link JmsContextToJsonObjectFunction} to create and run,
 * and the remaining program arguments are passed to the {@link JmsContextToJsonObjectFunctionFactory}
 * to create a {@link JmsContextToJsonObjectFunction}, which is subsequently run.
 *
 * @author Chris de Vreeze
 */
public class JmsProgramReturningJson {

    public static void main(String... args) {
        Objects.checkIndex(0, args.length);
        String jmsContextFunctionName = args[0];
        // The remaining arguments typically include a queue name, such as DEV.QUEUE.1
        List<String> factoryArgs = Arrays.stream(args).skip(1).toList();

        Weld weld = new Weld();

        try (WeldContainer weldContainer = weld.initialize()) {
            Instance<Config> configInstance = CDI.current().select(Config.class, Default.Literal.INSTANCE);

            Preconditions.checkArgument(
                    configInstance.isResolvable(),
                    String.format("Could not resolve Config with required qualifier '%s'", Default.Literal.INSTANCE)
            );

            Config config = configInstance.get();

            // Typically, system property "connectionType" has been passed to the program
            ConnectionType connectionType =
                    ConnectionType.parse(config.getConfigValue("connectionType").getValue());

            Annotation connectionQualifier = new HasConnectionTypeQualifier(connectionType);
            Instance<ConnectionFactory> cfInstance = CDI.current().select(ConnectionFactory.class, connectionQualifier);

            Preconditions.checkArgument(
                    cfInstance.isResolvable(),
                    String.format("Could not resolve ConnectionFactory with required qualifier '%s'", connectionQualifier)
            );

            Instance<JmsContextToJsonObjectFunctionFactory> functionFactoryInstance =
                    CDI.current().select(JmsContextToJsonObjectFunctionFactory.class, NamedLiteral.of(jmsContextFunctionName));

            Preconditions.checkArgument(
                    functionFactoryInstance.isResolvable(),
                    String.format("Could not resolve function with name '%s'", jmsContextFunctionName)
            );

            JmsContextToJsonObjectFunction function = functionFactoryInstance.get().apply(factoryArgs);

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
}
