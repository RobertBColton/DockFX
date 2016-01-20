package org.dockfx.pane;

import org.dockfx.DockNode;

import javafx.scene.control.Tab;

/**
 * DockNodeTab class holds Tab for ContentTabPane
 *
 * @author HongKee Moon
 */
public class DockNodeTab extends Tab {

  final private DockNode dockNode;

  public DockNodeTab(DockNode node) {
    this.dockNode = node;
    setClosable(false);

    setGraphic(dockNode.getDockTitleBar());
    setContent(dockNode);
    dockNode.tabbedProperty().set(true);
  }
}
