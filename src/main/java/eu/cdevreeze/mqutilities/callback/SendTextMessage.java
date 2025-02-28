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

/**
 * {@link QueueCallback} that sends a {@link jakarta.jms.TextMessage} to a queue.
 *
 * @author Chris de Vreeze
 */
public class SendTextMessage implements QueueCallback<String> {

    private final String text;

    public SendTextMessage(String text) {
        this.text = text;
    }

    @Override
    public String apply(JMSContext jmsContext, String queueName) {
        JMSProducer jmsProducer = jmsContext.createProducer();

        jmsProducer.send(jmsContext.createQueue(queueName), text);

        return text;
    }
}
