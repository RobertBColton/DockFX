package floating;

import javafx.scene.control.Label;
import org.dockfx.DockNode;
import org.junit.Rule;
import org.junit.Test;
import utils.JavaFXThreadingRule;

import static org.junit.Assert.assertTrue;

public class FloatingDock {
    @Rule
    public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();

    @Test
    public void canCreateFloatingDock() {
        final DockNode testDock = new DockNode(new Label(), "Some Label");
        testDock.setFloating(true);
        assertTrue(testDock.isFloating());
    }
}
