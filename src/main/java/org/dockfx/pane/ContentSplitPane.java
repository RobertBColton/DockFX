package org.dockfx.pane;

import org.dockfx.DockNode;
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
 *
 * @author Robert B. Colton
 * @author HongKee Moon
 */
public class ContentSplitPane extends SplitPane implements ContentPane {

  /**
   * The Parent.
   */
  ContentPane parent;

  public Type getType() {
    return Type.SplitPane;
  }

  public void setContentParent(ContentPane pane) {
    parent = pane;
  }

  public ContentPane getContentParent() {
    return parent;
  }

  /**
   * Instantiates a new ContentSplitPane
   */
  public ContentSplitPane() {
  }

  /**
   * Instantiates a new ContentSplitPane.
   *
   * @param node the node
   */
  public ContentSplitPane(Node node) {
    getItems().add(node);
  }

  public ContentPane getSiblingParent(Stack<Parent> stack, Node sibling) {
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


  public boolean removeNode(Stack<Parent> stack, Node node) {
    ContentPane pane;

    List<Node> children = getChildrenList();

    for (int i = 0; i < children.size(); i++) {
      if (children.get(i) == node) {
        getItems().remove(i);
        return true;
      }
      else if (children.get(i) instanceof ContentPane) {
        pane = (ContentPane) children.get(i);
		  if(pane.removeNode(stack, node))
		  {
			  if( pane.getChildrenList().size() < 1) {
				  getItems().remove(i);
				  return true;
		  	  }
			  else if(pane.getChildrenList().size() == 1 &&
					  pane instanceof ContentTabPane &&
					  pane.getChildrenList().get(0) instanceof DockNode)
			  {
				  List<Node> childrenList = pane.getChildrenList();
				  Node sibling = childrenList.get(0);
				  ContentPane contentParent = pane.getContentParent();

				  contentParent.set((Node) pane, sibling);
				  return true;
			  }
		  }
      }
    }

    return false;
  }

  public List<Node> getChildrenList() {
    return getItems();
  }

  public void set(int idx, Node node) {
    getItems().set(idx, node);
  }

  public void set(Node sibling, Node node) {
    set(getItems().indexOf(sibling), node);
  }

  public void addNode(Node root, Node sibling, Node node, DockPos dockPos) {
    // finally dock the node to the correct split pane
    ObservableList<Node> splitItems = getItems();

    double magnitude = 0;

    if (splitItems.size() > 0) {
      if (getOrientation() == Orientation.HORIZONTAL) {
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
        if (getOrientation() == Orientation.HORIZONTAL) {
          setDividerPosition(relativeIndex,
                             node.prefWidth(0) / (magnitude + node.prefWidth(0)));
        } else {
          setDividerPosition(relativeIndex,
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
        if (getOrientation() == Orientation.HORIZONTAL) {
          setDividerPosition(relativeIndex - 1,
                             1 - node.prefWidth(0) / (magnitude + node.prefWidth(0)));
        } else {
          setDividerPosition(relativeIndex - 1,
                             1 - node.prefHeight(0) / (magnitude + node.prefHeight(0)));
        }
      }
    }
  }
}
