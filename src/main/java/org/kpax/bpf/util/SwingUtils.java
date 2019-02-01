/*******************************************************************************
 * Copyright (c) 2018 Eugen Covaci.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * Contributors:
 *     Eugen Covaci - initial design and implementation
 *******************************************************************************/

package org.kpax.bpf.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.UIManager;
import javax.swing.text.DefaultFormatter;

/**
 * Various Swing related methods.
 * @author Eugen Covaci
 */
public class SwingUtils {

	private static final String DLG_ERR_TITLE = "Error";

	private static final String DLG_INFO_TITLE = "Info";

	private static final String DLG_WARN_TITLE = "Warning";

	public static void setFont(Component component, Font font) {
		component.setFont(font);
		if (component instanceof JMenu) {
			JMenu menu = (JMenu) component;
			for (int i = 0; i < menu.getItemCount(); i++) {
				setFont(menu.getItem(i), font);
			}
		} else if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				setFont(child, font);
			}
		}
	}

	public static void setFontSettings(Font font) {
		UIManager.put("OptionPane.messageFont", font);
		UIManager.put("OptionPane.buttonFont", font);
		UIManager.put("ToolTip.font", font);
	}

	public static void setEnabled(Component component, boolean enabled) {
		component.setEnabled(enabled);
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				setEnabled(child, enabled);
			}
		}
	}

	public static void commitsOnValidEdit(JSpinner spinner) {
		JComponent comp = spinner.getEditor();
		JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
		((DefaultFormatter) field.getFormatter()).setCommitsOnValidEdit(true);
	}

	public static void showMessage(String title, String message, int type) {
		JOptionPane.showMessageDialog(null, message, title, type);
	}

	public static void showErrorMessage(String message) {
		showErrorMessage(DLG_ERR_TITLE, message);
	}

	public static void showErrorMessage(String title, String message) {
		showMessage(title, message, JOptionPane.ERROR_MESSAGE);
	}

	public static void showInfoMessage(String message) {
		showInfoMessage(DLG_INFO_TITLE, message);
	}

	public static void showInfoMessage(String title, String message) {
		showMessage(title, message, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void showWarningMessage(String message) {
		showWarningMessage(DLG_WARN_TITLE, message);
	}

	public static void showWarningMessage(String title, String message) {
		showMessage(title, message, JOptionPane.WARNING_MESSAGE);
	}

}
