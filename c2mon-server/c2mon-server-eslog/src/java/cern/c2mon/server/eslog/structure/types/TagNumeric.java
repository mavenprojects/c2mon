package cern.c2mon.server.eslog.structure.types;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents a Tag in ElasticSearch.
 * This type of TagES contains a numeric value.
 * @author Alban Marguet.
 */
@Slf4j
public class TagNumeric extends TagES implements TagESInterface {
  /**
   * Set value of TagNumeric to the value of Tag in C2MON if it was numeric.
   * @param value Object supposed to be numeric.
   */
  @Override
  public void setValue(Object value) {
    if (value == null) {
      log.trace("setValue() TagNumeric - Value is not set (value= " + value + ").");
    }
    else if (value instanceof Number) {
      this.value = value;
    }
    else {
      log.trace("setValue() - value has value " + value + ".");
      throw new IllegalArgumentException("setValue() - Cannot instantiate new TagNumeric in ElasticSearch because the value has class=" + value.getClass().getName() + ")");
    }
  }
}