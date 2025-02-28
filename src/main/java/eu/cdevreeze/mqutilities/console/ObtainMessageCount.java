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
import eu.cdevreeze.mqutilities.callback.GetMessageCount;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Program that calls {@link eu.cdevreeze.mqutilities.callback.GetMessageCount} and shows the result.
 * <p>
 * The only program argument is the queue name. Other than that, system property
 * "ConnectionFactorySupplierClass" is required, whose instances may require their own required system
 * properties.
 *
 * @author Chris de Vreeze
 */
public class ObtainMessageCount {

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Objects.checkIndex(0, args.length);
        String queueName = args[0];

        String fqcnOfCFSupplier = Objects.requireNonNull(System.getProperty("ConnectionFactorySupplierClass"));

        @SuppressWarnings("unchecked")
        Class<ConnectionFactorySupplier> cfSupplierClass = (Class<ConnectionFactorySupplier>) Class.forName(fqcnOfCFSupplier);

        ConnectionFactorySupplier cfSupplier = cfSupplierClass.getDeclaredConstructor().newInstance();
        ConnectionFactory cf = cfSupplier.get();

        QueueCallback<Integer> getMessageCount = new GetMessageCount();

        int messageCount;
        try (JMSContext jmsContext = cf.createContext()) {
            messageCount = getMessageCount.apply(jmsContext, queueName);
        }

        System.out.printf("Queue '%s' currently has %d messages%n", queueName, messageCount);
    }
}
