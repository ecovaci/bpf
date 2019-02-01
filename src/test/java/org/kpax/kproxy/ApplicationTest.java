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

package org.kpax.kproxy;

import java.awt.EventQueue;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.UIManager;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.kpax.bpf.UiConfig;
import org.kpax.bpf.ui.AppFrame;
import org.kpax.bpf.util.LocalIOUtils;
import org.kpax.bpf.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eugen Covaci
 */
@Configuration
@ComponentScan(basePackages = "org.kpax.bpf")
public class ApplicationTest {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationTest.class);

	@Bean
	FileBasedConfigurationBuilder<PropertiesConfiguration> userConfigurationBuilder() {
		return new Configurations()
				.propertiesBuilder(LocalIOUtils.toPath(System.getProperty("user.home"), ".bpf", "config",
						"user.properties"));
	}

	@Bean
	Font globalFont(UiConfig uiConfig) {
		return new Font("Dialog", Font.BOLD,
				uiConfig.getFontSize());
	}

	public static void main(String[] args) {

		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to set Nimbus L&F, let the default look and feel.", e);
		}

		UIManager.put("OptionPane.messageFont", new Font("Dialog", Font.BOLD, 13));

		// Set jaas.conf file
		String jaasConfigPath = new File("config/jaas.conf").getAbsolutePath();
		logger.info("Set java.security.auth.login.config property={}", jaasConfigPath);
		System.setProperty("java.security.auth.login.config", jaasConfigPath);

		// Check for krb5.conf (mandatory)
		File krbConfigFile = new File("config/krb5.conf");
		if (krbConfigFile.exists()) {
			String krbConfigPath = krbConfigFile.getAbsolutePath();
			logger.info("Set java.security.krb5.conf={}", krbConfigPath);
			System.setProperty("java.security.krb5.conf", krbConfigPath);
		} else {
			logger.info("'krb5.conf' file is missing from 'config' directory!");
		}

		File userProperties = new File(LocalIOUtils.toPath(System.getProperty("user.home"), ".bpf", "config",
				"user.properties"));

		// Create user.properties if not exists, otherwise check version
		if (userProperties.exists()) {
			try {
				PropertiesConfiguration systemConfiguration = new Configurations()
						.propertiesBuilder("config/user.properties").getConfiguration();
				FileBasedConfigurationBuilder<PropertiesConfiguration> currentPropertiesBuilder = new Configurations()
						.propertiesBuilder(userProperties);
				PropertiesConfiguration currentConfiguration = currentPropertiesBuilder
						.getConfiguration();
				if (!systemConfiguration.getString("release.version").equals(currentConfiguration.getString("release.version"))) {
					logger.info("Merge the existent user.properties with the new one");
					currentConfiguration.setProperty("release.version", systemConfiguration.getString("release.version"));
					LocalIOUtils.mergeProperties(systemConfiguration, currentConfiguration);
					currentPropertiesBuilder.save();
				}
			} catch (ConfigurationException e) {
				logger.error("Error on checking version", e);
				SwingUtils.showErrorMessage("Cannot check the version of 'user.properties' file from your home directory.");
				return;
			}
		} else {
			userProperties.getParentFile().mkdirs();
			try {
				Files.copy(new File("config/user.properties").toPath(), userProperties.toPath());
			} catch (IOException e) {
				logger.error("Error on copying user.properties file to user's home directory", e);
				SwingUtils.showErrorMessage("Cannot copy 'user.properties' file to your home directory. "
						+ "\nCheck whether 'user.properties' file does exist in 'config' directory!");
				return;
			}
		}

		try {
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationTest.class);
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				logger.info("Close Spring's context");
				context.close();
				logger.info("The application is shut down!");
			}));
			Font globalFont = context.getBean(Font.class);
			// Some global font settings
			SwingUtils.setFontSettings(globalFont);

			final AppFrame frame = context.getBean(AppFrame.class);
			EventQueue.invokeLater(() -> {
				try {
					SwingUtils.setFont(frame, globalFont);
					frame.pack();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
					frame.handleFocus();
				} catch (Exception e) {
					logger.error("GUI error", e);
				}
			});
		} catch (Exception e) {
			logger.error("Error on starting Spring context", e);
			SwingUtils.showErrorMessage("Cannot initialize application's context!" +
					"\nSee the log file for details");
		}

	}

}