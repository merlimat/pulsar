/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.client.impl;

import static org.testng.Assert.assertEquals;
import java.util.concurrent.TimeUnit;
import lombok.Cleanup;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerConsumerBase;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.TableView;
import org.awaitility.Awaitility;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = "broker-impl")
public class TableViewTest extends ProducerConsumerBase {

    @Override
    @BeforeMethod
    protected void setup() throws Exception {
        super.internalSetup();
        super.producerBaseSetup();
    }

    @Override
    @AfterMethod(alwaysRun = true)
    protected void cleanup() throws Exception {
        super.internalCleanup();
    }

    @DataProvider(name = "topic-is-partitioned")
    public Object[][] topicType(){
        return new Object[][] {{Boolean.FALSE}, {Boolean.TRUE}};
    }

    @Test(dataProvider = "topic-is-partitioned")
    public void testCreateTableView(boolean isPartitioned) throws Exception {
        final String topic = newTopicName();

        if (isPartitioned) {
            admin.topics().createPartitionedTopic(topic, 16);
        }

        @Cleanup
        Producer<Integer> producer = pulsarClient.newProducer(Schema.INT32)
                .topic(topic)
                .create();

        @Cleanup
        TableView<Integer> tableView = pulsarClient.newTableView(Schema.INT32)
                .topic(topic)
                .create();

        for (int i = 0; i < 100; i++) {
            producer.newMessage()
                    .key(Integer.toString(i  % 10))
                    .value(i)
                    .send();
        }

        // Account for small delay in applying the changes to the view
        Awaitility.await().untilAsserted(() -> {
            assertEquals(tableView.size(), 10);
            assertEquals(tableView.isEmpty(), false);

            for (int i = 0; i < 10; i++) {
                assertEquals(tableView.get(Integer.toString(i)).intValue(), 90 + i);
            }
        });
    }

    @Test
    public void testPartitionsChange() throws Exception {
        final String topic = newTopicName();

        admin.topics().createPartitionedTopic(topic, 2);

        @Cleanup
        TableView<Integer> tableView = pulsarClient.newTableView(Schema.INT32)
                .topic(topic)
                .autoUpdatePartitionsInterval(1, TimeUnit.SECONDS)
                .create();


        admin.topics().updatePartitionedTopic(topic, 16);

        @Cleanup
        Producer<Integer> producer = pulsarClient.newProducer(Schema.INT32)
                .topic(topic)
                .create();

        for (int i = 0; i < 100; i++) {
            producer.newMessage()
                    .key(Integer.toString(i  % 10))
                    .value(i)
                    .send();
        }

        // Account for small delay in applying the changes to the view
        Awaitility.await().untilAsserted(() -> {
            assertEquals(tableView.size(), 10);
            assertEquals(tableView.isEmpty(), false);

            for (int i = 0; i < 10; i++) {
                assertEquals(tableView.get(Integer.toString(i)).intValue(), 90 + i);
            }
        });
    }
}
