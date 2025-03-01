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
import eu.cdevreeze.mqutilities.JmsContextToJsonObjectFunction;
import eu.cdevreeze.mqutilities.JmsContextToJsonObjectFunctionFactory;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

import java.io.StringWriter;
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
        Class<JmsContextToJsonObjectFunctionFactory> factoryClass =
                (Class<JmsContextToJsonObjectFunctionFactory>) Class.forName(fqcnOfFactory);

        JmsContextToJsonObjectFunctionFactory factory = factoryClass.getDeclaredConstructor()
                .newInstance();

        JmsContextToJsonObjectFunction function = factory.apply(factoryArgs);

        // Do the actual work within a JMSContext
        JsonObject result;
        try (JMSContext jmsContext = cf.createContext()) {
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
