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

package eu.cdevreeze.mqutilities.callback;

import eu.cdevreeze.mqutilities.QueueCallback;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@link QueueCallback} that sends a {@link jakarta.jms.TextMessage} to a queue, where
 * the message is read from a file.
 *
 * @author Chris de Vreeze
 */
public class SendTextMessageFromFile implements QueueCallback<String> {

    private final Path file;

    public SendTextMessageFromFile(Path file) {
        this.file = file;
    }

    @Override
    public String apply(JMSContext jmsContext, String queueName) {
        String messageText = null;
        try {
            messageText = Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        JMSProducer jmsProducer = jmsContext.createProducer();

        jmsProducer.send(jmsContext.createQueue(queueName), messageText);

        return messageText;
    }
}
