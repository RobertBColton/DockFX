/**
 * @file DockNode.java
 * @brief Class implementing basic dock node with floating and styling.
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Base class for a dock node that provides the layout of the content along with a title bar and a
 * styled border. The dock node can be detached and floated or closed and removed from the layout.
 * Dragging behavior is implemented through the title bar.
 * 
 * @since DockFX 0.1
 */
public class DockNode extends VBox implements EventHandler<MouseEvent> {
  /**
   * The style this dock node should use on its stage when set to floating.
   */
  private StageStyle stageStyle = StageStyle.TRANSPARENT;
  /**
   * The stage that this dock node is currently using when floating.
   */
  private Stage stage;

  /**
   * The contents of the dock node, i.e. a TreeView or ListView.
   */
  private Node contents;
  /**
   * The title bar that implements our dragging and state manipulation.
   */
  private DockTitleBar dockTitleBar;
  /**
   * The border pane used when floating to provide a styled custom border.
   */
  private BorderPane borderPane;

  /**
   * The dock pane this dock node belongs to when not floating.
   */
  private DockPane dockPane;

  /**
   * CSS pseudo class selector representing whether this node is currently floating.
   */
  private static final PseudoClass FLOATING_PSEUDO_CLASS = PseudoClass.getPseudoClass("floating");
  /**
   * CSS pseudo class selector representing whether this node is currently docked.
   */
  private static final PseudoClass DOCKED_PSEUDO_CLASS = PseudoClass.getPseudoClass("docked");
  /**
   * CSS pseudo class selector representing whether this node is currently maximized.
   */
  private static final PseudoClass MAXIMIZED_PSEUDO_CLASS = PseudoClass.getPseudoClass("maximized");

  /**
   * Boolean property maintaining whether this node is currently maximized.
   * 
   * @defaultValue false
   */
  private BooleanProperty maximizedProperty = new SimpleBooleanProperty(false) {

    @Override
    protected void invalidated() {
      DockNode.this.pseudoClassStateChanged(MAXIMIZED_PSEUDO_CLASS, get());
      if (borderPane != null) {
        borderPane.pseudoClassStateChanged(MAXIMIZED_PSEUDO_CLASS, get());
      }

      stage.setMaximized(get());

      // TODO: This is a work around to fill the screen bounds and not overlap the task bar when 
      // the window is undecorated as in Visual Studio. A similar work around needs applied for 
      // JFrame in Swing. http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4737788
      // Bug report filed:
      // https://bugs.openjdk.java.net/browse/JDK-8133330
      if (this.get()) {
        Screen screen = Screen
            .getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
            .get(0);
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());

        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
      }
    }

    @Override
    public String getName() {
      return "maximized";
    }
  };

  /**
   * Creates a default DockNode with a default title bar and layout.
   * 
   * @param contents The contents of the dock node which may be a tree or another scene graph node.
   * @param title The caption title of this dock node which maintains bidirectional state with the
   *        title bar and stage.
   * @param graphic The caption graphic of this dock node which maintains bidirectional state with
   *        the title bar and stage.
   */
  public DockNode(Node contents, String title, Node graphic) {
    this.titleProperty.setValue(title);
    this.graphicProperty.setValue(graphic);
    this.contents = contents;

    dockTitleBar = new DockTitleBar(this);

    getChildren().addAll(dockTitleBar, contents);
    VBox.setVgrow(contents, Priority.ALWAYS);

    this.getStyleClass().add("dock-node");
  }

  /**
   * Creates a default DockNode with a default title bar and layout.
   * 
   * @param contents The contents of the dock node which may be a tree or another scene graph node.
   * @param title The caption title of this dock node which maintains bidirectional state with the
   *        title bar and stage.
   */
  public DockNode(Node contents, String title) {
    this(contents, title, null);
  }

  /**
   * Creates a default DockNode with a default title bar and layout.
   * 
   * @param contents The contents of the dock node which may be a tree or another scene graph node.
   */
  public DockNode(Node contents) {
    this(contents, null, null);
  }

  /**
   * The stage style that will be used when the dock node is floating. This must be set prior to
   * setting the dock node to floating.
   * 
   * @param stageStyle The stage style that will be used when the node is floating.
   */
  public void setStageStyle(StageStyle stageStyle) {
    this.stageStyle = stageStyle;
  }

  /**
   * Changes the contents of the dock node.
   * 
   * @param contents The new contents of this dock node.
   */
  public void setContents(Node contents) {
    this.getChildren().set(this.getChildren().indexOf(this.contents), contents);
    this.contents = contents;
  }

  /**
   * Changes the title bar in the layout of this dock node. This can be used to remove the dock
   * title bar from the dock node by passing null.
   * 
   * @param dockTitleBar null The new title bar of this dock node, can be set null indicating no
   *        title bar is used.
   */
  public void setDockTitleBar(DockTitleBar dockTitleBar) {
    if (dockTitleBar != null) {
      if (this.dockTitleBar != null) {
        this.getChildren().set(this.getChildren().indexOf(this.dockTitleBar), dockTitleBar);
      } else {
        this.getChildren().add(0, dockTitleBar);
      }
    } else {
      this.getChildren().remove(this.dockTitleBar);
    }

    this.dockTitleBar = dockTitleBar;
  }

  /**
   * Whether the node is currently maximized.
   * 
   * @param maximized Whether the node is currently maximized.
   */
  public final void setMaximized(boolean maximized) {
    maximizedProperty.set(maximized);
  }

  /**
   * Whether the node is currently floating.
   * 
   * @param floating Whether the node is currently floating.
   * @param translation null The offset of the node after being set floating. Used for aligning it
   *        with its layout bounds inside the dock pane when it becomes detached. Can be null
   *        indicating no translation.
   */
  public void setFloating(boolean floating, Point2D translation) {
    if (floating && !this.isFloating()) {
      // position the new stage relative to the old scene offset
      Point2D floatScene = this.localToScene(0, 0);
      Point2D floatScreen = this.localToScreen(0, 0);

      // setup window stage
      dockTitleBar.setVisible(this.isCustomTitleBar());
      dockTitleBar.setManaged(this.isCustomTitleBar());

      if (this.isDocked()) {
        this.undock();
      }

      stage = new Stage();
      stage.titleProperty().bind(titleProperty);
      if (dockPane != null && dockPane.getScene() != null
          && dockPane.getScene().getWindow() != null) {
        stage.initOwner(dockPane.getScene().getWindow());
      }

      stage.initStyle(stageStyle);

      // offset the new stage to cover exactly the area the dock was local to the scene
      // this is useful for when the user presses the + sign and we have no information
      // on where the mouse was clicked
      Point2D stagePosition;
      if (this.isDecorated()) {
        Window owner = stage.getOwner();
        stagePosition = floatScene.add(new Point2D(owner.getX(), owner.getY()));
      } else {
        stagePosition = floatScreen;
      }
      if (translation != null) {
        stagePosition = stagePosition.add(translation);
      }

      // the border pane allows the dock node to
      // have a drop shadow effect on the border
      // but also maintain the layout of contents
      // such as a tab that has no content
      borderPane = new BorderPane();
      borderPane.getStyleClass().add("dock-node-border");
      borderPane.setCenter(this);

      Scene scene = new Scene(borderPane);

      // apply the floating property so we can get its padding size
      // while it is floating to offset it by the drop shadow
      // this way it pops out above exactly where it was when docked
      this.floatingProperty.set(floating);
      this.applyCss();

      // apply the border pane css so that we can get the insets and
      // position the stage properly
      borderPane.applyCss();
      Insets insetsDelta = borderPane.getInsets();

      double insetsWidth = insetsDelta.getLeft() + insetsDelta.getRight();
      double insetsHeight = insetsDelta.getTop() + insetsDelta.getBottom();

      stage.setX(stagePosition.getX() - insetsDelta.getLeft());
      stage.setY(stagePosition.getY() - insetsDelta.getTop());

      stage.setMinWidth(borderPane.minWidth(this.getHeight()) + insetsWidth);
      stage.setMinHeight(borderPane.minHeight(this.getWidth()) + insetsHeight);

      borderPane.setPrefSize(this.getWidth() + insetsWidth, this.getHeight() + insetsHeight);

      stage.setScene(scene);

      if (stageStyle == StageStyle.TRANSPARENT) {
        scene.setFill(null);
      }

      stage.setResizable(this.isStageResizable());
      if (this.isStageResizable()) {
        stage.addEventFilter(MouseEvent.MOUSE_PRESSED, this);
        stage.addEventFilter(MouseEvent.MOUSE_MOVED, this);
        stage.addEventFilter(MouseEvent.MOUSE_DRAGGED, this);
      }

      // we want to set the client area size
      // without this it subtracts the native border sizes from the scene
      // size
      stage.sizeToScene();

      stage.show();
    } else if (!floating && this.isFloating()) {
      this.floatingProperty.set(floating);

      stage.removeEventFilter(MouseEvent.MOUSE_PRESSED, this);
      stage.removeEventFilter(MouseEvent.MOUSE_MOVED, this);
      stage.removeEventFilter(MouseEvent.MOUSE_DRAGGED, this);

      stage.close();
    }
  }

  /**
   * Whether the node is currently floating.
   * 
   * @param floating Whether the node is currently floating.
   */
  public void setFloating(boolean floating) {
    setFloating(floating, null);
  }

  /**
   * The dock pane that was last associated with this dock node. Either the dock pane that it is
   * currently docked to or the one it was detached from. Can be null if the node was never docked.
   * 
   * @return The dock pane that was last associated with this dock node.
   */
  public final DockPane getDockPane() {
    return dockPane;
  }

  /**
   * The dock title bar associated with this dock node.
   * 
   * @return The dock title bar associated with this node.
   */
  public final DockTitleBar getDockTitleBar() {
    return this.dockTitleBar;
  }

  /**
   * The stage associated with this dock node. Can be null if the dock node was never set to
   * floating.
   * 
   * @return The stage associated with this node.
   */
  public final Stage getStage() {
    return stage;
  }

  /**
   * The border pane used to parent this dock node when floating. Can be null if the dock node was
   * never set to floating.
   * 
   * @return The stage associated with this node.
   */
  public final BorderPane getBorderPane() {
    return borderPane;
  }

  /**
   * The contents managed by this dock node.
   * 
   * @return The contents managed by this dock node.
   */
  public final Node getContents() {
    return contents;
  }

  /**
   * Object property maintaining bidirectional state of the caption graphic for this node with the
   * dock title bar or stage.
   * 
   * @defaultValue null
   */
  public final ObjectProperty<Node> graphicProperty() {
    return graphicProperty;
  }

  private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>() {
    @Override
    public String getName() {
      return "graphic";
    }
  };

  public final Node getGraphic() {
    return graphicProperty.get();
  }

  public final void setGraphic(Node graphic) {
    this.graphicProperty.setValue(graphic);
  }

  /**
   * Boolean property maintaining bidirectional state of the caption title for this node with the
   * dock title bar or stage.
   * 
   * @defaultValue "Dock"
   */
  public final StringProperty titleProperty() {
    return titleProperty;
  }

  private StringProperty titleProperty = new SimpleStringProperty("Dock") {
    @Override
    public String getName() {
      return "title";
    }
  };

  public final String getTitle() {
    return titleProperty.get();
  }

  public final void setTitle(String title) {
    this.titleProperty.setValue(title);
  }

  /**
   * Boolean property maintaining whether this node is currently using a custom title bar. This can
   * be used to force the default title bar to show when the dock node is set to floating instead of
   * using native window borders.
   * 
   * @defaultValue true
   */
  public final BooleanProperty customTitleBarProperty() {
    return customTitleBarProperty;
  }

  private BooleanProperty customTitleBarProperty = new SimpleBooleanProperty(true) {
    @Override
    public String getName() {
      return "customTitleBar";
    }
  };

  public final boolean isCustomTitleBar() {
    return customTitleBarProperty.get();
  }

  public final void setUseCustomTitleBar(boolean useCustomTitleBar) {
    if (this.isFloating()) {
      dockTitleBar.setVisible(useCustomTitleBar);
      dockTitleBar.setManaged(useCustomTitleBar);
    }
    this.customTitleBarProperty.set(useCustomTitleBar);
  }

  /**
   * Boolean property maintaining whether this node is currently floating.
   * 
   * @defaultValue false
   */
  public final BooleanProperty floatingProperty() {
    return floatingProperty;
  }

  private BooleanProperty floatingProperty = new SimpleBooleanProperty(false) {
    @Override
    protected void invalidated() {
      DockNode.this.pseudoClassStateChanged(FLOATING_PSEUDO_CLASS, get());
      if (borderPane != null) {
        borderPane.pseudoClassStateChanged(FLOATING_PSEUDO_CLASS, get());
      }
    }

    @Override
    public String getName() {
      return "floating";
    }
  };

  public final boolean isFloating() {
    return floatingProperty.get();
  }

  /**
   * Boolean property maintaining whether this node is currently floatable.
   * 
   * @defaultValue true
   */
  public final BooleanProperty floatableProperty() {
    return floatableProperty;
  }

  private BooleanProperty floatableProperty = new SimpleBooleanProperty(true) {
    @Override
    public String getName() {
      return "floatable";
    }
  };

  public final boolean isFloatable() {
    return floatableProperty.get();
  }

  public final void setFloatable(boolean floatable) {
    if (!floatable && this.isFloating()) {
      this.setFloating(false);
    }
    this.floatableProperty.set(floatable);
  }

  /**
   * Boolean property maintaining whether this node is currently closable.
   * 
   * @defaultValue true
   */
  public final BooleanProperty closableProperty() {
    return closableProperty;
  }

  private BooleanProperty closableProperty = new SimpleBooleanProperty(true) {
    @Override
    public String getName() {
      return "closable";
    }
  };

  public final boolean isClosable() {
    return closableProperty.get();
  }

  public final void setClosable(boolean closable) {
    this.closableProperty.set(closable);
  }

  /**
   * Boolean property maintaining whether this node is currently resizable.
   * 
   * @defaultValue true
   */
  public final BooleanProperty resizableProperty() {
    return stageResizableProperty;
  }

  private BooleanProperty stageResizableProperty = new SimpleBooleanProperty(true) {
    @Override
    public String getName() {
      return "resizable";
    }
  };

  public final boolean isStageResizable() {
    return stageResizableProperty.get();
  }

  public final void setStageResizable(boolean resizable) {
    stageResizableProperty.set(resizable);
  }

  /**
   * Boolean property maintaining whether this node is currently docked. This is used by the dock
   * pane to inform the dock node whether it is currently docked.
   * 
   * @defaultValue false
   */
  public final BooleanProperty dockedProperty() {
    return dockedProperty;
  }

  private BooleanProperty dockedProperty = new SimpleBooleanProperty(false) {
    @Override
    protected void invalidated() {
      if (get()) {
        if (dockTitleBar != null) {
          dockTitleBar.setVisible(true);
          dockTitleBar.setManaged(true);
        }
      }

      DockNode.this.pseudoClassStateChanged(DOCKED_PSEUDO_CLASS, get());
    }

    @Override
    public String getName() {
      return "docked";
    }
  };

  public final boolean isDocked() {
    return dockedProperty.get();
  }

  public final BooleanProperty maximizedProperty() {
    return maximizedProperty;
  }

  public final boolean isMaximized() {
    return maximizedProperty.get();
  }

  public final boolean isDecorated() {
    return stageStyle != StageStyle.TRANSPARENT && stageStyle != StageStyle.UNDECORATED;
  }

  /**
   * Dock this node into a dock pane.
   * 
   * @param dockPane The dock pane to dock this node into.
   * @param dockPos The docking position relative to the sibling of the dock pane.
   * @param sibling The sibling node to dock this node relative to.
   */
  public void dock(DockPane dockPane, DockPos dockPos, Node sibling) {
    dockImpl(dockPane);
    dockPane.dock(this, dockPos, sibling);
  }

  /**
   * Dock this node into a dock pane.
   * 
   * @param dockPane The dock pane to dock this node into.
   * @param dockPos The docking position relative to the sibling of the dock pane.
   */
  public void dock(DockPane dockPane, DockPos dockPos) {
    dockImpl(dockPane);
    dockPane.dock(this, dockPos);
  }

  private final void dockImpl(DockPane dockPane) {
    if (isFloating()) {
      setFloating(false);
    }
    this.dockPane = dockPane;
    this.dockedProperty.set(true);
  }

  /**
   * Detach this node from its previous dock pane if it was previously docked.
   */
  public void undock() {
    if (dockPane != null) {
      dockPane.undock(this);
    }
    this.dockedProperty.set(false);
  }

  /**
   * Close this dock node by setting it to not floating and making sure it is detached from any dock
   * pane.
   */
  public void close() {
    if (isFloating()) {
      setFloating(false);
    } else if (isDocked()) {
      undock();
    }
  }

  /**
   * The last position of the mouse that was within the minimum layout bounds.
   */
  private Point2D sizeLast;
  /**
   * Whether we are currently resizing in a given direction.
   */
  private boolean sizeWest = false, sizeEast = false, sizeNorth = false, sizeSouth = false;

  /**
   * Gets whether the mouse is currently in this dock node's resize zone.
   * 
   * @return Whether the mouse is currently in this dock node's resize zone.
   */
  public boolean isMouseResizeZone() {
    return sizeWest || sizeEast || sizeNorth || sizeSouth;
  }

  @Override
  public void handle(MouseEvent event) {
    Cursor cursor = Cursor.DEFAULT;

    // TODO: use escape to cancel resize/drag operation like visual studio
    if (!this.isFloating() || !this.isStageResizable()) {
      return;
    }

    if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
      sizeLast = new Point2D(event.getScreenX(), event.getScreenY());
    } else if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
      Insets insets = borderPane.getPadding();

      sizeWest = event.getX() < insets.getLeft();
      sizeEast = event.getX() > borderPane.getWidth() - insets.getRight();
      sizeNorth = event.getY() < insets.getTop();
      sizeSouth = event.getY() > borderPane.getHeight() - insets.getBottom();

      if (sizeWest) {
        if (sizeNorth) {
          cursor = Cursor.NW_RESIZE;
        } else if (sizeSouth) {
          cursor = Cursor.SW_RESIZE;
        } else {
          cursor = Cursor.W_RESIZE;
        }
      } else if (sizeEast) {
        if (sizeNorth) {
          cursor = Cursor.NE_RESIZE;
        } else if (sizeSouth) {
          cursor = Cursor.SE_RESIZE;
        } else {
          cursor = Cursor.E_RESIZE;
        }
      } else if (sizeNorth) {
        cursor = Cursor.N_RESIZE;
      } else if (sizeSouth) {
        cursor = Cursor.S_RESIZE;
      }

      this.getScene().setCursor(cursor);
    } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && this.isMouseResizeZone()) {
      Point2D sizeCurrent = new Point2D(event.getScreenX(), event.getScreenY());
      Point2D sizeDelta = sizeCurrent.subtract(sizeLast);

      double newX = stage.getX(), newY = stage.getY(), newWidth = stage.getWidth(),
          newHeight = stage.getHeight();

      if (sizeNorth) {
        newHeight -= sizeDelta.getY();
        newY += sizeDelta.getY();
      } else if (sizeSouth) {
        newHeight += sizeDelta.getY();
      }

      if (sizeWest) {
        newWidth -= sizeDelta.getX();
        newX += sizeDelta.getX();
      } else if (sizeEast) {
        newWidth += sizeDelta.getX();
      }

      // TODO: find a way to do this synchronously and eliminate the flickering of moving the stage
      // around, also file a bug report for this feature if a work around can not be found this
      // primarily occurs when dragging north/west but it also appears in native windows and Visual
      // Studio, so not that big of a concern.
      // Bug report filed:
      // https://bugs.openjdk.java.net/browse/JDK-8133332
      double currentX = sizeLast.getX(), currentY = sizeLast.getY();
      if (newWidth >= stage.getMinWidth()) {
        stage.setX(newX);
        stage.setWidth(newWidth);
        currentX = sizeCurrent.getX();
      }

      if (newHeight >= stage.getMinHeight()) {
        stage.setY(newY);
        stage.setHeight(newHeight);
        currentY = sizeCurrent.getY();
      }
      sizeLast = new Point2D(currentX, currentY);
      // we do not want the title bar getting these events
      // while we are actively resizing
      if (sizeNorth || sizeSouth || sizeWest || sizeEast) {
        event.consume();
      }
    }
  }
}
