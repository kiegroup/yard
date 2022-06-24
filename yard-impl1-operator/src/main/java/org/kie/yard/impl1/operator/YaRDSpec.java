package org.kie.yard.impl1.operator;

import org.kie.yard.api.model.YaRD;

public class YaRDSpec {

  /* TODO

              decision:
                x-kubernetes-preserve-unknown-fields: true
                properties:
                  inputs:
...
                type: object

   */
  private YaRD yard;

  public YaRD getYard() {
    return yard;
  }

  public void setYard(YaRD yard) {
    this.yard = yard;
  }

}
