package org.kie.yard.impl1.operator;

import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus;

public class YaRDStatus extends ObservedGenerationAwareStatus {

    private String url;

    private String status;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
