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

import eu.cdevreeze.mqutilities.jmscontextfunction.GetMessageCountFactory;

import java.util.Objects;

/**
 * Program that calls {@link eu.cdevreeze.mqutilities.jmscontextfunction.GetMessageCount} and shows the result.
 * <p>
 * The only program argument is the queue name.
 *
 * @author Chris de Vreeze
 */
public class GetMessageCountProgram {

    public static void main(String... args) throws Exception {
        Objects.checkIndex(0, args.length);
        String queueName = args[0]; // e.g. DEV.QUEUE.1

        JmsProgramReturningJson.main(
                GetMessageCountFactory.class.getCanonicalName(),
                queueName
        );
    }
}
