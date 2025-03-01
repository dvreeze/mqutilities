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
import eu.cdevreeze.mqutilities.QueueCallbackReturningJson;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Console program using a {@link QueueCallbackReturningJson}. The first program argument
 * is a queue name, the second one is the fully-qualified class name of the {@link QueueCallbackReturningJson},
 * and the third one is a JSON string that acts as input for the constructor of the
 * {@link QueueCallbackReturningJson}.
 *
 * @author Chris de Vreeze
 */
public class JmsProgramReturningJson {

    public static void main(String[] args) throws Exception {
        Objects.checkIndex(2, args.length);
        String queueName = args[0]; // e.g. DEV.QUEUE.1
        String fqcnOfQueueCallback = args[1];
        JsonObject constructorInput;
        try (JsonParser parser = Json.createParser(new StringReader(args[2]))) {
            constructorInput = parser.getObject();
        }

        String fqcnOfCFSupplier = Objects.requireNonNull(System.getProperty("ConnectionFactorySupplierClass"));

        @SuppressWarnings("unchecked")
        Class<ConnectionFactorySupplier> cfSupplierClass = (Class<ConnectionFactorySupplier>) Class.forName(fqcnOfCFSupplier);

        ConnectionFactorySupplier cfSupplier = cfSupplierClass.getDeclaredConstructor().newInstance();
        ConnectionFactory cf = cfSupplier.get();

        @SuppressWarnings("unchecked")
        Class<QueueCallbackReturningJson> callbackClass = (Class<QueueCallbackReturningJson>) Class.forName(fqcnOfQueueCallback);

        QueueCallbackReturningJson callback = callbackClass.getDeclaredConstructor(JsonObject.class)
                .newInstance(constructorInput);

        JsonObject result;
        try (JMSContext jmsContext = cf.createContext()) {
            result = callback.apply(jmsContext, queueName);
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
