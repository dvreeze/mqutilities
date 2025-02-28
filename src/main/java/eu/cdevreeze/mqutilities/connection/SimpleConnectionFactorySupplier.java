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

import com.ibm.msg.client.jakarta.jms.JmsConnectionFactory;
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import com.ibm.msg.client.jakarta.wmq.common.CommonConstants;
import eu.cdevreeze.mqutilities.ConnectionFactorySupplier;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;

/**
 * Simple {@link ConnectionFactorySupplier} using password authentication.
 * <p>
 * The configuration settings of the returned {@link jakarta.jms.ConnectionFactory} are taken
 * from system properties (with the same names as the corresponding constants in
 * {@link CommonConstants}), defaulting to the values for the Dockerized MQ queue manager
 * that can be used for training purposes.
 *
 * @author Chris de Vreeze
 */
public final class SimpleConnectionFactorySupplier implements ConnectionFactorySupplier {

    @Override
    public JmsConnectionFactory get() {
        try {
            String jakartaWmqProviderName = WMQConstants.JAKARTA_WMQ_PROVIDER;
            JmsFactoryFactory jmsFactoryFactory = JmsFactoryFactory.getInstance(jakartaWmqProviderName);

            JmsConnectionFactory cf = jmsFactoryFactory.createConnectionFactory();

            cf.setStringProperty(
                    CommonConstants.WMQ_HOST_NAME,
                    System.getProperty("WMQ_HOST_NAME", "localhost")
            );
            cf.setIntProperty(
                    CommonConstants.WMQ_PORT,
                    Integer.parseInt(System.getProperty("WMQ_PORT", "1414"))
            );
            cf.setStringProperty(
                    CommonConstants.WMQ_CHANNEL,
                    System.getProperty("WMQ_CHANNEL", "DEV.APP.SVRCONN")
            );
            cf.setIntProperty(
                    CommonConstants.WMQ_CONNECTION_MODE,
                    Integer.parseInt(System.getProperty("WMQ_CONNECTION_MODE", String.valueOf(CommonConstants.WMQ_CM_CLIENT)))
            );
            cf.setStringProperty(
                    CommonConstants.WMQ_QUEUE_MANAGER,
                    System.getProperty("WMQ_QUEUE_MANAGER", "QM1")
            );
            cf.setBooleanProperty(
                    CommonConstants.USER_AUTHENTICATION_MQCSP,
                    Boolean.parseBoolean(System.getProperty("USER_AUTHENTICATION_MQCSP", String.valueOf(true)))
            );
            cf.setStringProperty(
                    CommonConstants.USERID,
                    System.getProperty("USERID", "app")
            );
            cf.setStringProperty(
                    CommonConstants.PASSWORD,
                    System.getProperty("PASSWORD", "passw0rd")
            );
            return cf;
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }
}
