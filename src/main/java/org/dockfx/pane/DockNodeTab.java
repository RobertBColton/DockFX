package org.dockfx.pane;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import org.dockfx.DockNode;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;

/**
 * DockNodeTab class holds Tab for ContentTabPane
 *
 * @author HongKee Moon
 */
public class DockNodeTab extends Tab {

  final private DockNode dockNode;

  final private SimpleStringProperty title;

  public DockNodeTab(DockNode node) {
    this.dockNode = node;
    setClosable(false);

    title = new SimpleStringProperty("");
    title.bind(dockNode.titleProperty());

    setGraphic(dockNode.getDockTitleBar());
    setContent(dockNode);
    dockNode.tabbedProperty().set(true);
  }

  public String getTitle()
  {
    return title.getValue();
  }

  public SimpleStringProperty titleProperty()
  {
    return title;
  }
}
