/**
 * @file DockArea.java
 * @brief Enumeration of dock area alignment constants.
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

/**
 * DockPos
 * 
 * @since DockFX 0.1
 */
public enum DockPos {
  /**
   * Dock to the center by stacking inside a TabPane. This should be considered the equivalent of
   * null.
   */
  CENTER,

  /**
   * Dock to the left using a splitter.
   */
  LEFT,

  /**
   * Dock to the right using a splitter.
   */
  RIGHT,

  /**
   * Dock to the top using a splitter.
   */
  TOP,

  /**
   * Dock to the bottom using a splitter.
   */
  BOTTOM;
}
