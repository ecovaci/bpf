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

package org.kpax.bpf.ui;

import java.awt.AWTException;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.GeneralSecurityException;

import javax.annotation.PostConstruct;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.kpax.bpf.ExitCodes;
import org.kpax.bpf.SystemConfig;
import org.kpax.bpf.UiConfig;
import org.kpax.bpf.UserConfig;
import org.kpax.bpf.exception.CommandExecutionException;
import org.kpax.bpf.exception.InvalidKdcException;
import org.kpax.bpf.exception.KdcNotFoundException;
import org.kpax.bpf.proxy.LocalProxyServer;
import org.kpax.bpf.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppFrame extends JFrame {
	private static final long serialVersionUID = 4009799697210970761L;

	private static final Logger logger = LoggerFactory.getLogger(AppFrame.class);

	@Autowired
	private UserConfig userConfig;
	
	@Autowired
	private SystemConfig systemConfig;

	@Autowired
	private UiConfig uiConfig;

	@Autowired
	private LocalProxyServer localProxyServer;
	
	private JLabel usernameLabel;
	private JTextField usernameJTextField;
	private JLabel passwordLabel;
	private JPasswordField passwordJPasswordField;
	private JLabel domainLabel;
	private JTextField domainJTextField;
	private JLabel proxyHostLabel;
	private JTextField proxyHostJTextField;
	private JLabel proxyPortLabel;
	private JSpinner proxyPortJSpinner;
	private JLabel localPortLabel;
	private JSpinner localPortJSpinner;
	private JButton btnStart;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmExit;
	private JMenu mnHelp;
	private JMenuItem mntmAbout;

	private final DirtyTextFieldDocumentListener dirtyTextFieldDocumentListener = new DirtyTextFieldDocumentListener();
	private final DirtyChangeListener dirtyChangeListener = new DirtyChangeListener();

	private volatile boolean dirtyForm;
	private volatile boolean dirtyFont;

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("serial")
	@PostConstruct
	public void init() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				shutdownApp();
			}
		});

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
				KeyEvent.CTRL_DOWN_MASK),
				"increaseFonts");
		getRootPane().getActionMap().put("increaseFonts",
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						increaseFonts(1);
					}
				});
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
				KeyEvent.CTRL_DOWN_MASK),
				"decreaseFonts");
		getRootPane().getActionMap().put("decreaseFonts",
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						increaseFonts(-1);
					}
				});

		Image iconImage = Toolkit.getDefaultToolkit().getImage("config/img/icon.png");
		setIconImage(iconImage);
		//
		if (SystemTray.isSupported()) {
			final SystemTray tray = SystemTray.getSystemTray();
			final TrayIcon trayIcon = new TrayIcon(iconImage, "Basic Proxy Facade for Kerberos");
			trayIcon.setImageAutoSize(true);
			trayIcon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					setVisible(true);
					setState(Frame.NORMAL);
				}
			});
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowIconified(WindowEvent e) {
					try {
						tray.add(trayIcon);
					} catch (AWTException ex) {
						logger.error("Cannot add icon to tray", ex);
					}
					setVisible(false);
				}

				@Override
				public void windowDeiconified(WindowEvent e) {
					tray.remove(trayIcon);
					setExtendedState(getExtendedState() & ~Frame.ICONIFIED);
					setVisible(true);
				}
			});
		}
		//
		setTitle("Basic Proxy Facade for Kerberos");
		setJMenuBar(getMainMenuBar());
		JPanel mainContentPane = new JPanel();
		setContentPane(mainContentPane);
		//
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 35, 35, 35, 35, 35, 35, 35, 35 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
		mainContentPane.setLayout(gridBagLayout);

		GridBagConstraints labelGbc0 = new GridBagConstraints();
		labelGbc0.insets = new Insets(5, 5, 5, 5);
		labelGbc0.gridx = 0;
		labelGbc0.gridy = 0;
		mainContentPane.add(getUsernameLabel(), labelGbc0);

		GridBagConstraints componentGbc0 = new GridBagConstraints();
		componentGbc0.insets = new Insets(5, 5, 5, 5);
		componentGbc0.fill = GridBagConstraints.HORIZONTAL;
		componentGbc0.gridx = 1;
		componentGbc0.gridy = 0;
		mainContentPane.add(getUsernameJTextField(), componentGbc0);

		GridBagConstraints labelGbc1 = new GridBagConstraints();
		labelGbc1.insets = new Insets(5, 5, 5, 5);
		labelGbc1.gridx = 0;
		labelGbc1.gridy = 1;
		mainContentPane.add(getPasswordLabel(), labelGbc1);

		GridBagConstraints componentGbc1 = new GridBagConstraints();
		componentGbc1.insets = new Insets(5, 5, 5, 5);
		componentGbc1.fill = GridBagConstraints.HORIZONTAL;
		componentGbc1.gridx = 1;
		componentGbc1.gridy = 1;
		mainContentPane.add(getPasswordJPasswordField(), componentGbc1);

		GridBagConstraints labelGbc2 = new GridBagConstraints();
		labelGbc2.insets = new Insets(5, 5, 5, 5);
		labelGbc2.gridx = 0;
		labelGbc2.gridy = 2;
		mainContentPane.add(getDomainLabel(), labelGbc2);

		GridBagConstraints componentGbc2 = new GridBagConstraints();
		componentGbc2.insets = new Insets(5, 5, 5, 5);
		componentGbc2.fill = GridBagConstraints.HORIZONTAL;
		componentGbc2.gridx = 1;
		componentGbc2.gridy = 2;
		mainContentPane.add(getDomainJTextField(), componentGbc2);

		GridBagConstraints labelGbc3 = new GridBagConstraints();
		labelGbc3.insets = new Insets(5, 5, 5, 5);
		labelGbc3.gridx = 0;
		labelGbc3.gridy = 3;
		mainContentPane.add(getProxyHostLabel(), labelGbc3);

		GridBagConstraints componentGbc3 = new GridBagConstraints();
		componentGbc3.insets = new Insets(5, 5, 5, 5);
		componentGbc3.fill = GridBagConstraints.HORIZONTAL;
		componentGbc3.gridx = 1;
		componentGbc3.gridy = 3;
		mainContentPane.add(getProxyHostJTextField(), componentGbc3);

		GridBagConstraints labelGbc4 = new GridBagConstraints();
		labelGbc4.insets = new Insets(5, 5, 5, 5);
		labelGbc4.gridx = 0;
		labelGbc4.gridy = 4;
		mainContentPane.add(getProxyPortLabel(), labelGbc4);

		GridBagConstraints componentGbc4 = new GridBagConstraints();
		componentGbc4.insets = new Insets(5, 5, 5, 5);
		componentGbc4.fill = GridBagConstraints.HORIZONTAL;
		componentGbc4.gridx = 1;
		componentGbc4.gridy = 4;
		mainContentPane.add(getProxyPortJSpinner(), componentGbc4);

		GridBagConstraints labelGbc5 = new GridBagConstraints();
		labelGbc5.insets = new Insets(5, 5, 5, 5);
		labelGbc5.gridx = 0;
		labelGbc5.gridy = 5;
		mainContentPane.add(getLocalPortLabel(), labelGbc5);

		GridBagConstraints componentGbc5 = new GridBagConstraints();
		componentGbc5.insets = new Insets(5, 5, 5, 5);
		componentGbc5.fill = GridBagConstraints.HORIZONTAL;
		componentGbc5.gridx = 1;
		componentGbc5.gridy = 5;
		mainContentPane.add(getLocalPortJSpinner(), componentGbc5);

		GridBagConstraints gbcBtnStart = new GridBagConstraints();
		gbcBtnStart.gridx = 0;
		gbcBtnStart.gridy = 6;
		gbcBtnStart.gridwidth = 2;
		gbcBtnStart.gridheight = 2;
		mainContentPane.add(getBtnStart(), gbcBtnStart);

		initDataBindings();

		// After data bindings, the form is dirty
		// so we reset the dirty state
		dirtyForm = false;
	}

	private JLabel getUsernameLabel() {
		if (usernameLabel == null) {
			usernameLabel = new JLabel("Username*:");
		}
		return usernameLabel;
	}

	private JTextField getUsernameJTextField() {
		if (usernameJTextField == null) {
			usernameJTextField = createTextField();
		}
		return usernameJTextField;
	}

	private JLabel getPasswordLabel() {
		if (passwordLabel == null) {
			passwordLabel = new JLabel("Password*:");
		}
		return passwordLabel;
	}

	private JPasswordField getPasswordJPasswordField() {
		if (passwordJPasswordField == null) {
			passwordJPasswordField = new JPasswordField();
			passwordJPasswordField.setPreferredSize(new Dimension(6, 30));
			passwordJPasswordField.setMinimumSize(new Dimension(6, 30));
		}
		return passwordJPasswordField;
	}

	private JLabel getDomainLabel() {
		if (domainLabel == null) {
			domainLabel = new JLabel("Domain*:");
		}
		return domainLabel;
	}

	private JTextField getDomainJTextField() {
		if (domainJTextField == null) {
			domainJTextField = createTextField();
		}
		return domainJTextField;
	}

	private JLabel getProxyHostLabel() {
		if (proxyHostLabel == null) {
			proxyHostLabel = new JLabel("Proxy Host*:");
		}
		return proxyHostLabel;
	}

	private JTextField getProxyHostJTextField() {
		if (proxyHostJTextField == null) {
			proxyHostJTextField = createTextField();
		}
		return proxyHostJTextField;
	}

	private JTextField createTextField() {
		JTextField textField = new JTextField();
		textField.setPreferredSize(new Dimension(300, 30));
		textField.setMinimumSize(new Dimension(6, 30));
		textField.getDocument().addDocumentListener(dirtyTextFieldDocumentListener);
		return textField;
	}

	private JLabel getProxyPortLabel() {
		if (proxyPortLabel == null) {
			proxyPortLabel = new JLabel("Proxy Port*:");
		}
		return proxyPortLabel;
	}

	private JSpinner getProxyPortJSpinner() {
		if (proxyPortJSpinner == null) {
			proxyPortJSpinner = createJSpinner();
		}
		return proxyPortJSpinner;
	}

	private JSpinner createJSpinner() {
		JSpinner jSpinner = new JSpinner();
		jSpinner.setPreferredSize(new Dimension(32, 30));
		jSpinner.setMinimumSize(new Dimension(32, 30));
		jSpinner.setEditor(new JSpinner.NumberEditor(jSpinner, "#"));
		SwingUtils.commitsOnValidEdit(jSpinner);
		jSpinner.addChangeListener(dirtyChangeListener);
		return jSpinner;
	}

	private JLabel getLocalPortLabel() {
		if (localPortLabel == null) {
			localPortLabel = new JLabel("Local Port*:");
		}
		return localPortLabel;
	}

	private JSpinner getLocalPortJSpinner() {
		if (localPortJSpinner == null) {
			localPortJSpinner = createJSpinner();
		}
		return localPortJSpinner;
	}

	private void initDataBindings() {
		BeanProperty<UserConfig, String> usernameProperty = BeanProperty.create("username");
		BeanProperty<JTextField, String> textProperty = BeanProperty.create("text");
		AutoBinding<UserConfig, String, JTextField, String> autoBinding0 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, userConfig, usernameProperty,
						getUsernameJTextField(), textProperty);
		autoBinding0.bind();
		//
		BeanProperty<UserConfig, String> passwordProperty = BeanProperty.create("password");
		BeanProperty<JPasswordField, String> textProperty_1 = BeanProperty.create("text");
		AutoBinding<UserConfig, String, JPasswordField, String> autoBinding1 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, userConfig, passwordProperty,
						getPasswordJPasswordField(), textProperty_1);
		autoBinding1.bind();
		//
		BeanProperty<UserConfig, String> domainProperty = BeanProperty.create("domain");
		BeanProperty<JTextField, String> textProperty_2 = BeanProperty.create("text");
		AutoBinding<UserConfig, String, JTextField, String> autoBinding2 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, userConfig, domainProperty,
						getDomainJTextField(), textProperty_2);
		autoBinding2.bind();
		//
		BeanProperty<UserConfig, String> proxyHostProperty = BeanProperty.create("proxyHost");
		BeanProperty<JTextField, String> textProperty_3 = BeanProperty.create("text");
		AutoBinding<UserConfig, String, JTextField, String> autoBinding3 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, userConfig, proxyHostProperty,
						getProxyHostJTextField(), textProperty_3);
		autoBinding3.bind();
		//
		BeanProperty<UserConfig, Integer> proxyPortProperty = BeanProperty.create("proxyPort");
		BeanProperty<JSpinner, Object> valueProperty = BeanProperty.create("value");
		AutoBinding<UserConfig, Integer, JSpinner, Object> autoBinding4 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, userConfig, proxyPortProperty,
						getProxyPortJSpinner(), valueProperty);
		autoBinding4.bind();
		//
		BeanProperty<UserConfig, Integer> localPortProperty = BeanProperty.create("localPort");
		BeanProperty<JSpinner, Object> valueProperty_1 = BeanProperty.create("value");
		AutoBinding<UserConfig, Integer, JSpinner, Object> autoBinding5 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, userConfig, localPortProperty,
						getLocalPortJSpinner(), valueProperty_1);
		autoBinding5.bind();
		//
		BindingGroup bindingGroup = new BindingGroup();
		bindingGroup.addBinding(autoBinding0);
		bindingGroup.addBinding(autoBinding1);
		bindingGroup.addBinding(autoBinding2);
		bindingGroup.addBinding(autoBinding3);
		bindingGroup.addBinding(autoBinding4);
		bindingGroup.addBinding(autoBinding5);
		//
	}

	private JButton getBtnStart() {
		if (btnStart == null) {
			btnStart = new JButton("Start");
			btnStart.addActionListener(e -> {
				if (startServer()) {
					disableInput();
				}
			});
		}
		return btnStart;
	}

	private JMenuBar getMainMenuBar() {
		if (menuBar == null) {
			menuBar = new JMenuBar();
			menuBar.add(getMnFile());
			menuBar.add(getMnHelp());
		}
		return menuBar;
	}

	private JMenu getMnFile() {
		if (mnFile == null) {
			mnFile = new JMenu("File");
			mnFile.add(getMntmExit());
		}
		return mnFile;
	}

	private JMenuItem getMntmExit() {
		if (mntmExit == null) {
			mntmExit = new JMenuItem("Exit");
			mntmExit.addActionListener(e -> shutdownApp());
		}
		return mntmExit;
	}

	private JMenu getMnHelp() {
		if (mnHelp == null) {
			mnHelp = new JMenu("Help");
			mnHelp.add(getMntmAbout());
		}
		return mnHelp;
	}

	private JMenuItem getMntmAbout() {
		if (mntmAbout == null) {
			mntmAbout = new JMenuItem("About");
			mntmAbout.addActionListener(e -> SwingUtils.showInfoMessage("About", "Basic Proxy Facade for Kerberos" +
					"\nVersion: " + systemConfig.getReleaseVersion()
					+ "\nProject home page: https://github.com/ecovaci/bpf"
					+ "\nLicense: Apache 2.0"));
		}
		return mntmAbout;
	}

	private void disableInput() {
		SwingUtils.setEnabled(getContentPane(), false);
	}

	private void increaseFonts(int delta) {
		Font font = new Font("Dialog", Font.BOLD, getFont().getSize() + delta);
		SwingUtils.setFont(this, font);
		SwingUtils.setFontSettings(font);
		uiConfig.setFontSize(font.getSize());
		dirtyFont = true;
	}

	private boolean isValidInput() {
		if (StringUtils.isBlank(usernameJTextField.getText())) {
			SwingUtils.showErrorMessage("Validation Error", "Fill in the username");
			return false;
		}
		if (passwordJPasswordField.getPassword() == null || passwordJPasswordField.getPassword().length == 0) {
			SwingUtils.showErrorMessage("Validation Error", "Fill in the password");
			return false;
		}
		if (StringUtils.isBlank(domainJTextField.getText())) {
			SwingUtils.showErrorMessage("Validation Error", "Fill in the domain");
			return false;
		}
		if (StringUtils.isBlank(proxyHostJTextField.getText())) {
			SwingUtils.showErrorMessage("Validation Error", "Fill in the proxy address");
			return false;
		}
		Integer proxyPort = (Integer) proxyPortJSpinner.getValue();
		if (proxyPort == null || proxyPort < 1) {
			SwingUtils.showErrorMessage("Validation Error", "Fill in a valid proxy port, bigger than 0");
			return false;
		}
		Integer localPort = (Integer) localPortJSpinner.getValue();
		if (localPort == null || localPort < 1024) {
			SwingUtils.showErrorMessage("Validation Error", "Fill in the local port, bigger than 1023");
			return false;
		}

		return true;
	}

	private boolean startServer() {
		if (isValidInput()) {
			try {
				localProxyServer.start();
				return true;
			} catch (GeneralSecurityException e) {
				logger.error("GeneralSecurityException error", e);
				SwingUtils.showErrorMessage("Authentication failed. Check if the remote proxy server is up, " +
						"\nthe krb5.conf file (if exists) is valid and the username/password are correct. " +
						"\nSee the log file for details.");
			} catch (CommandExecutionException e) {
				logger.error("CommandExecutionException error", e);
				SwingUtils.showErrorMessage("Error on retrieving Kerberos KDC configuration.\nSee the application's log for details.");
			} catch (KdcNotFoundException e) {
				logger.error("KdcNotFoundException error", e);
				SwingUtils.showErrorMessage("Cannot retrieve Kerberos KDC configuration, check the domain and try again!\nSee the application's log for details.");
			} catch (InvalidKdcException e) {
				logger.error("InvalidKdcException error", e);
				SwingUtils.showErrorMessage(
						"The Kerberos KDC configuration found seems to be invalid!\nTry using the 'krb5.conf' file provided by the proxy's administrator.\nSee the application's log for details.");
			} catch (Exception e) {
				logger.error("Error on starting proxy server", e);
				SwingUtils.showErrorMessage("Error on starting proxy server.\nSee the application's log for details.");
			}
		}
		return false;
	}

	private void shutdownApp() {
		logger.info("Now shutdown application");
		if (!localProxyServer.isStarted() || JOptionPane.showConfirmDialog(null,
				"The local proxy facade is started. \nDo you like to stop the proxy facade and leave the application?",
				"Warning", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
			dispose();

			if (dirtyForm) {
				try {
					logger.info("Save the user's settings");
					userConfig.save();
				} catch (Exception e) {
					logger.error("Error on saving the user's settings to the configuration file", e);
					SwingUtils.showErrorMessage("The configuration file cannot be saved in your user home directory!" +
							"\nCheck the log file for details.");
				}
			}
		}

		try {
			if (dirtyFont) {
				logger.info("Save the font size");
				uiConfig.save();
			}
		} catch (Exception e) {
			logger.error("Error on saving the font size to the configuration file", e);
			SwingUtils.showErrorMessage("The configuration file cannot be saved in your user home directory!" +
					"\nCheck the log file for details.");
		}
		
		System.exit(ExitCodes.OK);
	}

	private void handleDirtyForm() {
		if (!dirtyForm) {
			synchronized (this) {
				if (!dirtyForm) {
					dirtyForm = true;
					if (isVisible()) {
						logger.info("Now remove dirty listeners");
						removeDirtyListeners(getContentPane());
					}
				}
			}
		}
	}

	private void removeDirtyListeners(Container root) {
		for (java.awt.Component comp : root.getComponents()) {
			if (comp instanceof JTextField) {
				((JTextField) comp).getDocument().removeDocumentListener(dirtyTextFieldDocumentListener);
			} else if (comp instanceof JSpinner) {
				((JSpinner) comp).removeChangeListener(dirtyChangeListener);
			} else if (comp instanceof Container) {
				removeDirtyListeners((Container) comp);
			}
		}
	}

	public void handleFocus() {
		if (StringUtils.isEmpty(getUsernameJTextField().getText())) {
			getUsernameJTextField().requestFocus();
		} else {
			getPasswordJPasswordField().requestFocus();
		}
	}

	private class DirtyTextFieldDocumentListener implements DocumentListener {

		public void changedUpdate(DocumentEvent e) {
			handleDirtyForm();
		}

		public void removeUpdate(DocumentEvent e) {
			handleDirtyForm();
		}

		public void insertUpdate(DocumentEvent e) {
			handleDirtyForm();
		}

	}

	private class DirtyChangeListener implements ChangeListener {

		public void stateChanged(ChangeEvent e) {
			handleDirtyForm();
		}

	}
}
