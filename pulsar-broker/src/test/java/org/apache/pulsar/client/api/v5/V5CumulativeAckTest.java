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
import static org.testng.Assert.assertNull;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import lombok.Cleanup;
import org.apache.pulsar.client.api.v5.config.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.v5.schema.Schema;
import org.testng.annotations.Test;

/**
 * Coverage for {@link StreamConsumer#acknowledgeCumulative(MessageId)} on a multi-segment
 * scalable topic.
 *
 * <p>Each message returned by a V5 stream consumer carries a position vector — a snapshot
 * of the latest delivered offset on every active segment as of that point in the stream.
 * Cumulative-acking a single {@link MessageId} must therefore advance the cursor on
 * <em>every</em> segment, not just the segment that produced this particular message.
 *
 * <p>We assert this end-to-end: produce N messages across multiple segments, drain them,
 * cumulative-ack the last received id, then attach a fresh consumer on the same
 * subscription. The fresh consumer must observe an empty backlog.
 */
public class V5CumulativeAckTest extends V5ClientBaseTest {

    @Test
    public void testCumulativeAckCoversAllSegments() throws Exception {
        String topic = newScalableTopic(4);
        String subscription = "cum-ack-sub";

        // Subscribe before producing so the subscription cursor exists at the start of
        // every segment — an EARLIEST consumer would also work, but this keeps the test
        // unambiguous.
        @Cleanup
        Producer<String> producer = v5Client.newProducer(Schema.string())
                .topic(topic)
                .create();

        StreamConsumer<String> consumer = v5Client.newStreamConsumer(Schema.string())
                .topic(topic)
                .subscriptionName(subscription)
                .subscriptionInitialPosition(SubscriptionInitialPosition.EARLIEST)
                .subscribe();

        int n = 100;
        Set<String> sent = new HashSet<>();
        for (int i = 0; i < n; i++) {
            String v = "v-" + i;
            producer.newMessage().key("k-" + i).value(v).send();
            sent.add(v);
        }

        Set<String> received = new HashSet<>();
        MessageId last = null;
        for (int i = 0; i < n; i++) {
            Message<String> msg = consumer.receive(Duration.ofSeconds(5));
            assertNotNull(msg, "missed message #" + i);
            received.add(msg.value());
            last = msg.id();
        }
        assertEquals(received, sent, "should drain every produced message");

        // Single cumulative ack on the last id — the position vector embedded in this id
        // must advance the cursor on every segment.
        assertNotNull(last);
        consumer.acknowledgeCumulative(last);

        // Close and re-open the consumer on the same subscription. With a complete
        // cumulative ack, the new attach must see no backlog.
        consumer.close();

        @Cleanup
        StreamConsumer<String> reopened = v5Client.newStreamConsumer(Schema.string())
                .topic(topic)
                .subscriptionName(subscription)
                .subscribe();

        Message<String> stale = reopened.receive(Duration.ofMillis(500));
        assertNull(stale,
                "after a single cumulative ack of the last received id, no message"
                        + " should remain unacked on any segment");
    }
}
