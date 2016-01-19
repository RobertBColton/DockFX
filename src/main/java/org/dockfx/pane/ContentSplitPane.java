package org.dockfx.pane;

import org.dockfx.DockPos;

import java.util.List;
import java.util.Stack;

import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;

/**
 * ContentSplitPane contains multiple SplitPane
 * @author Robert B. Colton
 * @author HongKee Moon
 */
public class ContentSplitPane extends SplitPane implements ContentPane {

  public Type getType() {
    return Type.SplitPane;
  }

  /**
   * Instantiates a new ContentSplitPane
   */
  public ContentSplitPane()
  {
  }

  /**
   * Instantiates a new ContentSplitPane.
   *
   * @param node the node
   */
  public ContentSplitPane(Node node)
  {
    getItems().add(node);
  }

  public ContentPane getSiblingParent(Stack<Parent> stack, Node sibling)
  {
    ContentPane pane = null;

    while (!stack.isEmpty()) {
      Parent parent = stack.pop();

      List<Node> children = parent.getChildrenUnmodifiable();

      if (parent instanceof ContentPane) {
        children = ((ContentPane) parent).getChildrenList();
      }

      for (int i = 0; i < children.size(); i++) {
        if (children.get(i) == sibling) {
          pane = (ContentPane) parent;
        } else if (children.get(i) instanceof Parent) {
          stack.push((Parent) children.get(i));
        }
      }
    }
    return pane;
  }


  public void removeNode(Stack<Parent> stack, Node node)
  {
    ContentPane pane = null;

    List<Node> children = getChildrenList();
    for(int i = 0; i < children.size(); i++)
    {
      if(children.get(i) == node) {
        getItems().remove(i);

        while (!stack.isEmpty()) {
          Parent parent = stack.pop();

          children = parent.getChildrenUnmodifiable();

          for (i = 0; i < children.size(); i++) {
            if (children.get(i) instanceof ContentPane) {
              pane = (ContentPane) children.get(i);
              if (pane.getChildrenList().size() < 1) {
                children.remove(i);
                continue;
              } else {
                stack.push((Parent) pane);
              }
            }
          }

        }
        return;
      } else if(children.get(i) instanceof Parent)
      {
        stack.push((Parent) children.get(i));
      }
    }

  }

  public List<Node> getChildrenList()
  {
    return getItems();
  }

  public void set(int idx, Node node)
  {
    getItems().set(idx, node);
  }

  public void set(Node sibling, Node node)
  {
    set( getItems().indexOf(sibling), node);
  }

  public void addNode(Node root, Node sibling, Node node, DockPos dockPos)
  {
    Orientation requestedOrientation = (dockPos == DockPos.LEFT || dockPos == DockPos.RIGHT)
                                       ? Orientation.HORIZONTAL : Orientation.VERTICAL;

    ContentSplitPane split = this;

    // if the orientation is different then reparent the split pane
    if (split.getOrientation() != requestedOrientation) {
      if (split.getItems().size() > 1) {
        ContentSplitPane splitPane = new ContentSplitPane();
        if (split == root && sibling == root) {
          this.set(root, splitPane);
          splitPane.getItems().add(split);
          root = splitPane;
        } else {
          split.set(sibling, splitPane);
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
}
