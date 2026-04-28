/*
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
package org.apache.pulsar.client.api.v5;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Cleanup;
import org.apache.pulsar.client.api.v5.config.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.v5.schema.Schema;
import org.testng.annotations.Test;

/**
 * Coverage for multi-generation DAG-following: an existing subscription must transparently
 * follow a chain of splits, draining each generation in order without dropping or
 * duplicating messages.
 *
 * <p>{@link V5SegmentSplitTest} covers a single split; this exercises a chain (split,
 * then split one of the children) with a producer + consumer running throughout.
 *
 * <p><b>Known gap (not asserted here):</b> a brand-new subscription created with EARLIEST
 * after a split currently only sees the active children's data, not the sealed parent.
 * Replaying sealed-segment data for new subscriptions is a separate feature.
 */
public class V5DAGFollowingTest extends V5ClientBaseTest {

    @Test
    public void testMultiGenerationDAGFollowing() throws Exception {
        String topic = newScalableTopic(1);

        @Cleanup
        Producer<String> producer = v5Client.newProducer(Schema.string())
                .topic(topic)
                .create();
        @Cleanup
        QueueConsumer<String> consumer = v5Client.newQueueConsumer(Schema.string())
                .topic(topic)
                .subscriptionName("multi-gen")
                .subscriptionInitialPosition(SubscriptionInitialPosition.EARLIEST)
                .subscribe();

        Set<String> sent = new HashSet<>();
        int batch = 20;

        // Generation 0: single segment.
        for (int i = 0; i < batch; i++) {
            String v = "g0-" + i;
            producer.newMessage().key("k0-" + i).value(v).send();
            sent.add(v);
        }

        // Split 0 → {1, 2}: now 2 active.
        admin.scalableTopics().splitSegment(topic, activeIds(topic).get(0));
        waitForActiveCount(topic, 2);

        // Generation 1: 2 segments.
        for (int i = 0; i < batch; i++) {
            String v = "g1-" + i;
            producer.newMessage().key("k1-" + i).value(v).send();
            sent.add(v);
        }

        // Split one of the children: pick the smaller-id active child for determinism.
        // After this we have 3 active.
        List<Long> active = activeIds(topic);
        admin.scalableTopics().splitSegment(topic, active.get(0));
        waitForActiveCount(topic, 3);

        // Generation 2: 3 segments.
        for (int i = 0; i < batch; i++) {
            String v = "g2-" + i;
            producer.newMessage().key("k2-" + i).value(v).send();
            sent.add(v);
        }

        // Drain: 3 generations × batch each.
        Set<String> received = new HashSet<>();
        int total = 3 * batch;
        for (int i = 0; i < total; i++) {
            Message<String> msg = consumer.receive(Duration.ofSeconds(10));
            assertNotNull(msg, "missed message #" + i
                    + " (received so far: " + received.size() + "/" + total + ")");
            received.add(msg.value());
            consumer.acknowledge(msg.id());
        }

        assertEquals(received.size(), total, "expected " + total + " distinct messages");
        assertEquals(received, sent, "received set must cover every generation");
    }

    private List<Long> activeIds(String topic) throws Exception {
        var meta = admin.scalableTopics().getMetadata(topic);
        List<Long> ids = new ArrayList<>();
        for (var seg : meta.getSegments().values()) {
            if (seg.isActive()) {
                ids.add(seg.getSegmentId());
            }
        }
        java.util.Collections.sort(ids);
        return ids;
    }

    private void waitForActiveCount(String topic, int expected) throws Exception {
        long deadline = System.currentTimeMillis() + 10_000L;
        while (System.currentTimeMillis() < deadline) {
            if (activeIds(topic).size() == expected) {
                return;
            }
            Thread.sleep(100);
        }
        assertEquals(activeIds(topic).size(), expected,
                "layout never converged to " + expected + " active segments");
    }
}
