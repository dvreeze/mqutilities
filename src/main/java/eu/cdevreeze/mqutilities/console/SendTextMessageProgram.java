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

import eu.cdevreeze.mqutilities.jmscontextfunction.SendTextMessage;

import java.util.Objects;

/**
 * Program that calls {@link SendTextMessage} and shows the result.
 * <p>
 * The only program arguments are the queue name and message text.
 *
 * @author Chris de Vreeze
 */
public class SendTextMessageProgram {

    public static void main(String[] args) throws Exception {
        Objects.checkIndex(1, args.length);
        String queueName = args[0]; // e.g. DEV.QUEUE.1
        String messageText = args[1];

        JmsProgramReturningJson.main(
                SendTextMessage.class.getSimpleName(),
                queueName,
                messageText
        );
    }
}
