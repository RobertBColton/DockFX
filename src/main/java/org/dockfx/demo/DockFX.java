/**
 * @file DockFX.java
 * @brief Driver demonstrating basic dock layout with prototypes. Maintained in a separate package
 *        to ensure the encapsulation of org.dockfx private package members.
 *
 * @section License
 *          This Source Code Form is subject to the terms of the Mozilla Public
 *          License, v. 2.0. If a copy of the MPL was not distributed with this
 *          file, You can obtain one at https://mozilla.org/MPL/2.0/.
 **/

package org.dockfx.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

public class DockFX extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("DockFX");

    // create a dock pane that will manage our dock nodes and handle the layout
    DockPane dockPane = new DockPane();

    // create a default test node for the center of the dock area
    TabPane tabs = new TabPane();
    HTMLEditor htmlEditor = new HTMLEditor();
    try {
      htmlEditor.setHtmlText(new String(Files.readAllBytes(Paths.get("readme.html"))));
    } catch (IOException e) {
      e.printStackTrace();
    }

    // empty tabs ensure that dock node has its own background color when floating
    tabs.getTabs().addAll(new Tab("Tab 1", htmlEditor), new Tab("Tab 2"), new Tab("Tab 3"));

    TableView<String> tableView = new TableView<String>();
    // this is why @SupressWarnings is used above
    // we don't care about the warnings because this is just a demonstration
    // for docks not the table view
    tableView.getColumns().addAll(new TableColumn<String, String>("A"),
        new TableColumn<String, String>("B"), new TableColumn<String, String>("C"));

    // load an image to caption the dock nodes
    Image dockImage = new Image(DockFX.class.getResource("docknode.png").toExternalForm());

    // create and dock some prototype dock nodes to the middle of the dock pane
    // the preferred sizes are used to specify the relative size of the node
    // to the other nodes

    // we can use this to give our central content a larger area where
    // the top and bottom nodes have a preferred width of 300 which means that
    // when a node is docked relative to them such as the left or right dock below
    // they will have 300 / 100 + 300 (400) or 75% of their previous width
    // after both the left and right node's are docked the center docks end up with 50% of the width

    DockNode tabsDock = new DockNode(tabs, "Tabs Dock", new ImageView(dockImage));
    tabsDock.setPrefSize(300, 100);
    tabsDock.dock(dockPane, DockPos.TOP);
    DockNode tableDock = new DockNode(tableView);
    // let's disable our table from being undocked
    tableDock.setDockTitleBar(null);
    tableDock.setPrefSize(300, 100);
    tableDock.dock(dockPane, DockPos.BOTTOM);

    final Menu menu1 = new Menu("File");
    final Menu menu2 = new Menu("Options");
    final Menu menu3 = new Menu("Help");

    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().addAll(menu1, menu2, menu3);

    ToolBar toolBar = new ToolBar(
        new Button("New"),
        new Button("Open"),
        new Button("Save"),
        new Separator(),
        new Button("Clean"),
        new Button("Compile"),
        new Button("Run"),
        new Separator(),
        new Button("Debug"),
        new Button("Profile")
    );

    VBox vbox = new VBox();
    vbox.getChildren().addAll(menuBar, toolBar, dockPane);
    VBox.setVgrow(dockPane, Priority.ALWAYS);

    primaryStage.setScene(new Scene(vbox, 800, 500));
    primaryStage.sizeToScene();

    primaryStage.show();

    // can be created and docked before or after the scene is created
    // and the stage is shown
    DockNode treeDock = new DockNode(generateRandomTree(), "Tree Dock", new ImageView(dockImage));
    treeDock.setPrefSize(100, 100);
    treeDock.dock(dockPane, DockPos.LEFT);
    treeDock = new DockNode(generateRandomTree(), "Tree Dock", new ImageView(dockImage));
    treeDock.setPrefSize(100, 100);
    treeDock.dock(dockPane, DockPos.RIGHT);

    // test the look and feel with both Caspian and Modena
    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
    // initialize the default styles for the dock pane and undocked nodes using the DockFX
    // library's internal Default.css stylesheet
    // unlike other custom control libraries this allows the user to override them globally
    // using the style manager just as they can with internal JavaFX controls
    // this must be called after the primary stage is shown
    // https://bugs.openjdk.java.net/browse/JDK-8132900
    DockPane.initializeDefaultUserAgentStylesheet();

    // TODO: after this feel free to apply your own global stylesheet using the StyleManager class
  }

  private TreeView<String> generateRandomTree() {
    // create a demonstration tree view to use as the contents for a dock node
    TreeItem<String> root = new TreeItem<String>("Root");
    TreeView<String> treeView = new TreeView<String>(root);
    treeView.setShowRoot(false);

    // populate the prototype tree with some random nodes
    Random rand = new Random();
    for (int i = 4 + rand.nextInt(8); i > 0; i--) {
      TreeItem<String> treeItem = new TreeItem<String>("Item " + i);
      root.getChildren().add(treeItem);
      for (int j = 2 + rand.nextInt(4); j > 0; j--) {
        TreeItem<String> childItem = new TreeItem<String>("Child " + j);
        treeItem.getChildren().add(childItem);
      }
    }

    return treeView;
  }
}
