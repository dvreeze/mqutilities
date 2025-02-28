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
import eu.cdevreeze.mqutilities.QueueCallback;
import eu.cdevreeze.mqutilities.callback.SendTextMessageFromFile;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Program that calls {@link SendTextMessageFromFile} and shows the result.
 * <p>
 * The only program arguments are the queue name and message text file path. Other than that, system property
 * "ConnectionFactorySupplierClass" is required, whose instances may require their own required system
 * properties.
 *
 * @author Chris de Vreeze
 */
public class FileBasedTextMessageSender {

    public static void main(String[] args) throws Exception {
        Objects.checkIndex(1, args.length);
        String queueName = args[0]; // e.g. DEV.QUEUE.1
        Path messageFile = Path.of(args[1]);

        String fqcnOfCFSupplier = Objects.requireNonNull(System.getProperty("ConnectionFactorySupplierClass"));

        @SuppressWarnings("unchecked")
        Class<ConnectionFactorySupplier> cfSupplierClass = (Class<ConnectionFactorySupplier>) Class.forName(fqcnOfCFSupplier);

        ConnectionFactorySupplier cfSupplier = cfSupplierClass.getDeclaredConstructor().newInstance();
        ConnectionFactory cf = cfSupplier.get();

        QueueCallback<String> sendTextMessageFromFile = new SendTextMessageFromFile(messageFile);

        String messageText;
        try (JMSContext jmsContext = cf.createContext()) {
            messageText = sendTextMessageFromFile.apply(jmsContext, queueName);
        }

        System.out.printf("Just sent message to queue '%s'%n", queueName);
        System.out.printf("Message sent:%n%s%n", messageText);
    }
}
