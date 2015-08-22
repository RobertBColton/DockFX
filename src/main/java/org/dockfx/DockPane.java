/**
 * @file DockPane.java
 * @brief Class implementing a generic dock pane for the layout of dock nodes.
 *
 * @section License
 *
 *          This file is a part of the DockFX Library. Copyright (C) 2015 Robert B. Colton
 *
 *          This program is free software: you can redistribute it and/or modify it under the terms
 *          of the GNU Lesser General Public License as published by the Free Software Foundation,
 *          either version 3 of the License, or (at your option) any later version.
 *
 *          This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *          WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *          PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 *          You should have received a copy of the GNU Lesser General Public License along with this
 *          program. If not, see <http://www.gnu.org/licenses/>.
 **/

package org.dockfx;

import java.util.Stack;

import com.sun.javafx.css.StyleManager;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.util.Duration;

/**
 * Base class for a dock pane that provides the layout of the dock nodes. Stacking the dock nodes to
 * the center in a TabPane will be added in a future release. For now the DockPane uses the relative
 * sizes of the dock nodes and lays them out in a tree of SplitPanes.
 * 
 * @since DockFX 0.1
 */
public class DockPane extends StackPane implements EventHandler<DockEvent> {
  /**
   * The current root node of this dock pane's layout.
   */
  private Node root;

  /**
   * Whether a DOCK_ENTER event has been received by this dock pane since the last DOCK_EXIT event
   * was received.
   */
  private boolean receivedEnter = false;

  /**
   * The current node in this dock pane that we may be dragging over.
   */
  private Node dockNodeDrag;
  /**
   * The docking area of the current dock indicator button if any is selected. This is either the
   * root or equal to dock node drag.
   */
  private Node dockAreaDrag;
  /**
   * The docking position of the current dock indicator button if any is selected.
   */
  private DockPos dockPosDrag;

  /**
   * The docking area shape with a dotted animated border on the indicator overlay popup.
   */
  private Rectangle dockAreaIndicator;
  /**
   * The timeline used to animate the borer of the docking area indicator shape. Because JavaFX has
   * no CSS styling for timelines/animations yet we will make this private and offer an accessor for
   * the user to programmatically modify the animation or disable it.
   */
  private Timeline dockAreaStrokeTimeline;
  /**
   * The popup used to display the root dock indicator buttons and the docking area indicator.
   */
  private Popup dockIndicatorOverlay;

  /**
   * The grid pane used to lay out the local dock indicator buttons. This is the grid used to lay
   * out the buttons in the circular indicator.
   */
  private GridPane dockPosIndicator;
  /**
   * The popup used to display the local dock indicator buttons. This allows these indicator buttons
   * to be displayed outside the window of this dock pane.
   */
  private Popup dockIndicatorPopup;

  /**
   * Base class for a dock indicator button that allows it to be displayed during a dock event and
   * continue to receive input.
   * 
   * @since DockFX 0.1
   */
  public class DockPosButton extends Button {
    /**
     * Whether this dock indicator button is used for docking a node relative to the root of the
     * dock pane.
     */
    private boolean dockRoot = true;
    /**
     * The docking position indicated by this button.
     */
    private DockPos dockPos = DockPos.CENTER;

    /**
     * Creates a new dock indicator button.
     */
    public DockPosButton(boolean dockRoot, DockPos dockPos) {
      super();
      this.dockRoot = dockRoot;
      this.dockPos = dockPos;
    }

    /**
     * Whether this dock indicator button is used for docking a node relative to the root of the
     * dock pane.
     * 
     * @param dockRoot Whether this indicator button is used for docking a node relative to the root
     *        of the dock pane.
     */
    public final void setDockRoot(boolean dockRoot) {
      this.dockRoot = dockRoot;
    }

    /**
     * The docking position indicated by this button.
     * 
     * @param dockPos The docking position indicated by this button.
     */
    public final void setDockPos(DockPos dockPos) {
      this.dockPos = dockPos;
    }

    /**
     * The docking position indicated by this button.
     * 
     * @return The docking position indicated by this button.
     */
    public final DockPos getDockPos() {
      return dockPos;
    }

    /**
     * Whether this dock indicator button is used for docking a node relative to the root of the
     * dock pane.
     * 
     * @return Whether this indicator button is used for docking a node relative to the root of the
     *         dock pane.
     */
    public final boolean isDockRoot() {
      return dockRoot;
    }
  }

  /**
   * A collection used to manage the indicator buttons and automate hit detection during DOCK_OVER
   * events.
   */
  private ObservableList<DockPosButton> dockPosButtons;


  /**
   * Creates a new DockPane adding event handlers for dock events and creating the indicator
   * overlays.
   */
  public DockPane() {
    super();

    this.addEventHandler(DockEvent.ANY, this);
    this.addEventFilter(DockEvent.ANY, new EventHandler<DockEvent>() {

      @Override
      public void handle(DockEvent event) {

        if (event.getEventType() == DockEvent.DOCK_ENTER) {
          DockPane.this.receivedEnter = true;
        } else if (event.getEventType() == DockEvent.DOCK_OVER) {
          DockPane.this.dockNodeDrag = null;
        }
      }

    });

    dockIndicatorPopup = new Popup();
    dockIndicatorPopup.setAutoFix(false);

    dockIndicatorOverlay = new Popup();
    dockIndicatorOverlay.setAutoFix(false);

    StackPane dockRootPane = new StackPane();
    dockRootPane.prefWidthProperty().bind(this.widthProperty());
    dockRootPane.prefHeightProperty().bind(this.heightProperty());

    dockAreaIndicator = new Rectangle();
    dockAreaIndicator.setManaged(false);
    dockAreaIndicator.setMouseTransparent(true);

    dockAreaStrokeTimeline = new Timeline();
    dockAreaStrokeTimeline.setCycleCount(Timeline.INDEFINITE);
    // 12 is the cumulative offset of the stroke dash array in the Default.css style sheet
    // RFE filed for CSS styled timelines/animations:
    // https://bugs.openjdk.java.net/browse/JDK-8133837
    KeyValue kv = new KeyValue(dockAreaIndicator.strokeDashOffsetProperty(), 12);
    KeyFrame kf = new KeyFrame(Duration.millis(500), kv);
    dockAreaStrokeTimeline.getKeyFrames().add(kf);
    dockAreaStrokeTimeline.play();

    DockPosButton dockCenter = new DockPosButton(false, DockPos.CENTER);
    dockCenter.getStyleClass().add("dock-center");

    DockPosButton dockTop = new DockPosButton(false, DockPos.TOP);
    dockTop.getStyleClass().add("dock-top");
    DockPosButton dockRight = new DockPosButton(false, DockPos.RIGHT);
    dockRight.getStyleClass().add("dock-right");
    DockPosButton dockBottom = new DockPosButton(false, DockPos.BOTTOM);
    dockBottom.getStyleClass().add("dock-bottom");
    DockPosButton dockLeft = new DockPosButton(false, DockPos.LEFT);
    dockLeft.getStyleClass().add("dock-left");

    DockPosButton dockTopRoot = new DockPosButton(true, DockPos.TOP);
    StackPane.setAlignment(dockTopRoot, Pos.TOP_CENTER);
    dockTopRoot.getStyleClass().add("dock-top-root");

    DockPosButton dockRightRoot = new DockPosButton(true, DockPos.RIGHT);
    StackPane.setAlignment(dockRightRoot, Pos.CENTER_RIGHT);
    dockRightRoot.getStyleClass().add("dock-right-root");

    DockPosButton dockBottomRoot = new DockPosButton(true, DockPos.BOTTOM);
    StackPane.setAlignment(dockBottomRoot, Pos.BOTTOM_CENTER);
    dockBottomRoot.getStyleClass().add("dock-bottom-root");

    DockPosButton dockLeftRoot = new DockPosButton(true, DockPos.LEFT);
    StackPane.setAlignment(dockLeftRoot, Pos.CENTER_LEFT);
    dockLeftRoot.getStyleClass().add("dock-left-root");

    // TODO: dockCenter goes first when tabs are added in a future version
    dockPosButtons = FXCollections.observableArrayList(dockTop, dockRight, dockBottom, dockLeft,
        dockTopRoot, dockRightRoot, dockBottomRoot, dockLeftRoot);

    dockPosIndicator = new GridPane();
    dockPosIndicator.add(dockTop, 1, 0);
    dockPosIndicator.add(dockRight, 2, 1);
    dockPosIndicator.add(dockBottom, 1, 2);
    dockPosIndicator.add(dockLeft, 0, 1);
    // dockPosIndicator.add(dockCenter, 1, 1);

    dockRootPane.getChildren().addAll(dockAreaIndicator, dockTopRoot, dockRightRoot, dockBottomRoot,
        dockLeftRoot);

    dockIndicatorOverlay.getContent().add(dockRootPane);
    dockIndicatorPopup.getContent().addAll(dockPosIndicator);

    this.getStyleClass().add("dock-pane");
    dockRootPane.getStyleClass().add("dock-root-pane");
    dockPosIndicator.getStyleClass().add("dock-pos-indicator");
    dockAreaIndicator.getStyleClass().add("dock-area-indicator");
  }

  /**
   * The Timeline used to animate the docking area indicator in the dock indicator overlay for this
   * dock pane.
   * 
   * @return The Timeline used to animate the docking area indicator in the dock indicator overlay
   *         for this dock pane.
   */
  public final Timeline getDockAreaStrokeTimeline() {
    return dockAreaStrokeTimeline;
  }

  /**
   * Helper function to retrieve the URL of the default style sheet used by DockFX.
   * 
   * @return The URL of the default style sheet used by DockFX.
   */
  public final static String getDefaultUserAgentStyleheet() {
    return DockPane.class.getResource("Default.css").toExternalForm();
  }

  /**
   * Helper function to add the default style sheet of DockFX to the user agent style sheets.
   */
  public final static void initializeDefaultUserAgentStylesheet() {
    StyleManager.getInstance()
        .addUserAgentStylesheet(DockPane.class.getResource("Default.css").toExternalForm());
  }

  /**
   * A cache of all dock node event handlers that we have created for tracking the current docking
   * area.
   */
  private ObservableMap<Node, DockNodeEventHandler> dockNodeEventFilters =
      FXCollections.observableHashMap();

  /**
   * A wrapper to the type parameterized generic EventHandler that allows us to remove it from its
   * listener when the dock node becomes detached. It is specifically used to monitor which dock
   * node in this dock pane's layout we are currently dragging over.
   * 
   * @since DockFX 0.1
   */
  private class DockNodeEventHandler implements EventHandler<DockEvent> {
    /**
     * The node associated with this event handler that reports to the encapsulating dock pane.
     */
    private Node node = null;

    /**
     * Creates a default dock node event handler that will help this dock pane track the current
     * docking area.
     * 
     * @param node The node that is to listen for docking events and report to the encapsulating
     *        docking pane.
     */
    public DockNodeEventHandler(Node node) {
      this.node = node;
    }

    @Override
    public void handle(DockEvent event) {
      DockPane.this.dockNodeDrag = node;
    }
  }

  /**
   * Dock the node into this dock pane at the given docking position relative to the sibling in the
   * layout. This is used to relatively position the dock nodes to other nodes given their preferred
   * size.
   * 
   * @param node The node that is to be docked into this dock pane.
   * @param dockPos The docking position of the node relative to the sibling.
   * @param sibling The sibling of this node in the layout.
   */
  public void dock(Node node, DockPos dockPos, Node sibling) {
    DockNodeEventHandler dockNodeEventHandler = new DockNodeEventHandler(node);
    dockNodeEventFilters.put(node, dockNodeEventHandler);
    node.addEventFilter(DockEvent.DOCK_OVER, dockNodeEventHandler);

    SplitPane split = (SplitPane) root;
    if (split == null) {
      split = new SplitPane();
      split.getItems().add(node);
      root = split;
      this.getChildren().add(root);
      return;
    }

    // find the parent of the sibling
    if (sibling != null && sibling != root) {
      Stack<Parent> stack = new Stack<Parent>();
      stack.push((Parent) root);
      while (!stack.isEmpty()) {
        Parent parent = stack.pop();

        ObservableList<Node> children = parent.getChildrenUnmodifiable();

        if (parent instanceof SplitPane) {
          SplitPane splitPane = (SplitPane) parent;
          children = splitPane.getItems();
        }

        for (int i = 0; i < children.size(); i++) {
          if (children.get(i) == sibling) {
            split = (SplitPane) parent;
          } else if (children.get(i) instanceof Parent) {
            stack.push((Parent) children.get(i));
          }
        }
      }
    }

    Orientation requestedOrientation = (dockPos == DockPos.LEFT || dockPos == DockPos.RIGHT)
        ? Orientation.HORIZONTAL : Orientation.VERTICAL;

    // if the orientation is different then reparent the split pane
    if (split.getOrientation() != requestedOrientation) {
      if (split.getItems().size() > 1) {
        SplitPane splitPane = new SplitPane();
        if (split == root && sibling == root) {
          this.getChildren().set(this.getChildren().indexOf(root), splitPane);
          splitPane.getItems().add(split);
          root = splitPane;
        } else {
          split.getItems().set(split.getItems().indexOf(sibling), splitPane);
          splitPane.getItems().add(sibling);
        }

        split = splitPane;
      }
      split.setOrientation(requestedOrientation);
    }

    // finally dock the node to the correct split pane
    ObservableList<Node> splitItems = split.getItems();

    double magnitude = 0;

    if (splitItems.size() > 0) {
      if (split.getOrientation() == Orientation.HORIZONTAL) {
        for (Node splitItem : splitItems) {
          magnitude += splitItem.prefWidth(0);
        }
      } else {
        for (Node splitItem : splitItems) {
          magnitude += splitItem.prefHeight(0);
        }
      }
    }

    if (dockPos == DockPos.LEFT || dockPos == DockPos.TOP) {
      int relativeIndex = 0;
      if (sibling != null && sibling != root) {
        relativeIndex = splitItems.indexOf(sibling);
      }

      splitItems.add(relativeIndex, node);

      if (splitItems.size() > 1) {
        if (split.getOrientation() == Orientation.HORIZONTAL) {
          split.setDividerPosition(relativeIndex,
              node.prefWidth(0) / (magnitude + node.prefWidth(0)));
        } else {
          split.setDividerPosition(relativeIndex,
              node.prefHeight(0) / (magnitude + node.prefHeight(0)));
        }
      }
    } else if (dockPos == DockPos.RIGHT || dockPos == DockPos.BOTTOM) {
      int relativeIndex = splitItems.size();
      if (sibling != null && sibling != root) {
        relativeIndex = splitItems.indexOf(sibling) + 1;
      }

      splitItems.add(relativeIndex, node);
      if (splitItems.size() > 1) {
        if (split.getOrientation() == Orientation.HORIZONTAL) {
          split.setDividerPosition(relativeIndex - 1,
              1 - node.prefWidth(0) / (magnitude + node.prefWidth(0)));
        } else {
          split.setDividerPosition(relativeIndex - 1,
              1 - node.prefHeight(0) / (magnitude + node.prefHeight(0)));
        }
      }
    }

  }

  /**
   * Dock the node into this dock pane at the given docking position relative to the root in the
   * layout. This is used to relatively position the dock nodes to other nodes given their preferred
   * size.
   * 
   * @param node The node that is to be docked into this dock pane.
   * @param dockPos The docking position of the node relative to the sibling.
   */
  public void dock(Node node, DockPos dockPos) {
    dock(node, dockPos, root);
  }

  /**
   * Detach the node from this dock pane removing it from the layout.
   * 
   * @param node The node that is to be removed from this dock pane.
   */
  public void undock(DockNode node) {
    DockNodeEventHandler dockNodeEventHandler = dockNodeEventFilters.get(node);
    node.removeEventFilter(DockEvent.DOCK_OVER, dockNodeEventHandler);
    dockNodeEventFilters.remove(node);

    // depth first search to find the parent of the node
    Stack<Parent> findStack = new Stack<Parent>();
    findStack.push((Parent) root);
    while (!findStack.isEmpty()) {
      Parent parent = findStack.pop();

      ObservableList<Node> children = parent.getChildrenUnmodifiable();

      if (parent instanceof SplitPane) {
        SplitPane split = (SplitPane) parent;
        children = split.getItems();
      }

      for (int i = 0; i < children.size(); i++) {
        if (children.get(i) == node) {
          children.remove(i);

          // start from the root again and remove any SplitPane's with no children in them
          Stack<Parent> clearStack = new Stack<Parent>();
          clearStack.push((Parent) root);
          while (!clearStack.isEmpty()) {
            parent = clearStack.pop();

            children = parent.getChildrenUnmodifiable();

            if (parent instanceof SplitPane) {
              SplitPane split = (SplitPane) parent;
              children = split.getItems();
            }

            for (i = 0; i < children.size(); i++) {
              if (children.get(i) instanceof SplitPane) {
                SplitPane split = (SplitPane) children.get(i);
                if (split.getItems().size() < 1) {
                  children.remove(i);
                  continue;
                } else {
                  clearStack.push(split);
                }
              }

            }
          }

          return;
        } else if (children.get(i) instanceof Parent) {
          findStack.push((Parent) children.get(i));
        }
      }
    }
  }

  @Override
  public void handle(DockEvent event) {
    if (event.getEventType() == DockEvent.DOCK_ENTER) {
      if (!dockIndicatorPopup.isShowing()) {
        Point2D topLeft = DockPane.this.localToScreen(0, 0);
        dockIndicatorOverlay.show(DockPane.this, topLeft.getX(), topLeft.getY());
        dockIndicatorPopup.show(DockPane.this, 0, 0);
        // even though we've shown the stage set both of these invisible until
        // we are actually docking over something below
        dockPosIndicator.setVisible(false);
        dockAreaIndicator.setVisible(false);
      }
    } else if (event.getEventType() == DockEvent.DOCK_OVER) {
      this.receivedEnter = false;

      dockPosDrag = null;
      dockAreaDrag = dockNodeDrag;

      for (DockPosButton dockIndicatorButton : dockPosButtons) {
        if (dockIndicatorButton
            .contains(dockIndicatorButton.screenToLocal(event.getScreenX(), event.getScreenY()))) {
          dockPosDrag = dockIndicatorButton.getDockPos();
          if (dockIndicatorButton.isDockRoot()) {
            dockAreaDrag = root;
          }
          dockIndicatorButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
          break;
        } else {
          dockIndicatorButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), false);
        }
      }

      if (dockPosDrag != null) {
        Point2D originToScene = dockAreaDrag.localToScene(0, 0);

        dockAreaIndicator.setVisible(true);
        dockAreaIndicator.relocate(originToScene.getX(), originToScene.getY());
        if (dockPosDrag == DockPos.RIGHT) {
          dockAreaIndicator.setTranslateX(dockAreaDrag.getLayoutBounds().getWidth() / 2);
        } else {
          dockAreaIndicator.setTranslateX(0);
        }

        if (dockPosDrag == DockPos.BOTTOM) {
          dockAreaIndicator.setTranslateY(dockAreaDrag.getLayoutBounds().getHeight() / 2);
        } else {
          dockAreaIndicator.setTranslateY(0);
        }

        if (dockPosDrag == DockPos.LEFT || dockPosDrag == DockPos.RIGHT) {
          dockAreaIndicator.setWidth(dockAreaDrag.getLayoutBounds().getWidth() / 2);
        } else {
          dockAreaIndicator.setWidth(dockAreaDrag.getLayoutBounds().getWidth());
        }
        if (dockPosDrag == DockPos.TOP || dockPosDrag == DockPos.BOTTOM) {
          dockAreaIndicator.setHeight(dockAreaDrag.getLayoutBounds().getHeight() / 2);
        } else {
          dockAreaIndicator.setHeight(dockAreaDrag.getLayoutBounds().getHeight());
        }
      } else {
        dockAreaIndicator.setVisible(false);
      }

      if (dockNodeDrag != null) {
        Point2D originToScreen = dockNodeDrag.localToScreen(0, 0);

        double posX = originToScreen.getX() + dockNodeDrag.getLayoutBounds().getWidth() / 2
            - dockPosIndicator.getWidth() / 2;
        double posY = originToScreen.getY() + dockNodeDrag.getLayoutBounds().getHeight() / 2
            - dockPosIndicator.getHeight() / 2;
        dockIndicatorPopup.setX(posX);
        dockIndicatorPopup.setY(posY);
        
        // set it visible after moving the window
        dockPosIndicator.setVisible(true);
      } else {
        dockPosIndicator.setVisible(false);
      }
    }

    if (event.getEventType() == DockEvent.DOCK_RELEASED && event.getContents() != null) {
      if (dockPosDrag != null && dockIndicatorOverlay.isShowing()) {
        DockNode dockNode = (DockNode) event.getContents();
        dockNode.dock(this, dockPosDrag, dockAreaDrag);
      }
    }
    
    if ((event.getEventType() == DockEvent.DOCK_EXIT && !this.receivedEnter)
        || event.getEventType() == DockEvent.DOCK_RELEASED) {
      if (dockIndicatorPopup.isShowing()) {
        dockIndicatorOverlay.hide();
        dockIndicatorPopup.hide();
      }
    }
  }
}
