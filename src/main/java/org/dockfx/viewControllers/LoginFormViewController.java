/**
 * @file DockFX.java
 * @brief Simple view controller demonstrating button action
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

package org.dockfx.viewControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * ViewController associated with LoginForm.fxml file.
 * Association created via Scene Builder.
 */
public class LoginFormViewController extends BaseViewController {

  @FXML
  private Button button;

  @Override
  protected void initialize() {
    button.setOnAction(e -> handleButtonClick());
  }

  private void handleButtonClick() {
    button.setText("Submitted!");
  }
}
