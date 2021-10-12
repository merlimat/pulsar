package org.apache.pulsar.client.impl;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TableViewConfigurationData implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private String topicName = null;
    private long autoUpdatePartitionsSeconds = 60;
}
