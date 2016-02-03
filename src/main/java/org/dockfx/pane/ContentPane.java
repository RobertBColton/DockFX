package org.dockfx.pane;

import org.dockfx.DockPos;

import java.util.List;
import java.util.Stack;

import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * ContentPane interface has common functions for Content window
 *
 * @author HongKee Moon
 */
public interface ContentPane {

  /**
   * The enum ContentPane Type.
   */
  public enum Type {
    /**
     * The SplitPane.
     */
    SplitPane,
    /**
     * The TabPane.
     */
    TabPane
  }

  /**
   * Gets type of ContentPane.
   *
   * @return the type
   */
  Type getType();

  /**
   * Add a node.
   *
   * @param root the root
   * @param sibling the sibling
   * @param node the node
   * @param dockPos the dock pos
   */
  void addNode(Node root, Node sibling, Node node, DockPos dockPos);

  /**
   * Remove the node.
   *
   * @param stack the stack
   * @param node the node
   * @return true if the node removed successfully, otherwise false
   */
  boolean removeNode(Stack<Parent> stack, Node node);

  /**
   * Gets sibling's parent.
   *
   * @param stack the stack
   * @param sibling the sibling
   * @return the sibling parent
   */
  ContentPane getSiblingParent(Stack<Parent> stack, Node sibling);

  /**
   * Gets children list.
   *
   * @return the children list
   */
  List<Node> getChildrenList();

  /**
   * Replace the previous element with new one
   *
   * @param sibling the sibling
   * @param node the node
   */
  void set(Node sibling, Node node);

  /**
   * Replace the previous element with new one by index id
   *
   * @param idx the idx
   * @param node the node
   */
  void set(int idx, Node node);

  /**
   * Gets content parent.
   *
   * @return the content parent
   */
  ContentPane getContentParent();

  /**
   * Sets content parent.
   *
   * @param pane the pane
   */
  void setContentParent(ContentPane pane);
}
