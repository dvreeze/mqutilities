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

package eu.cdevreeze.mqutilities.connection;

import eu.cdevreeze.mqutilities.qualifier.connection.SimpleConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;

/**
 * CDI-injectable "simple" JMS {@link ConnectionFactory}.
 *
 * @author Chris de Vreeze
 */
@ApplicationScoped
public class SimpleConnectionFactories {

    @Produces
    @SimpleConnection
    @ApplicationScoped
    public ConnectionFactory getConnectionFactory() {
        return new SimpleConnectionFactorySupplier().get();
    }
}
