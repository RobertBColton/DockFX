package org.dockfx;

/**
 * To support the delayed open process for some specific applications, this interface implementation is used.
 */
public interface DelayOpenHandler {
  public DockNode open(String nodeName);
}
