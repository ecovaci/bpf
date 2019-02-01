package org.kpax.bpf;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = "file:${user.dir}/config/user.properties", name = "userProperties")
public class UiConfig {

	@Autowired
	private FileBasedConfigurationBuilder<PropertiesConfiguration> propertiesBuilder;

	@Value("${font.size}")
	private Integer fontSize;

	public Integer getFontSize() {
		return fontSize;
	}

	public void setFontSize(Integer fontSize) {
		this.fontSize = fontSize;
	}

	public void save() throws ConfigurationException {
		Configuration config = propertiesBuilder.getConfiguration();
		config.setProperty("font.size", this.fontSize);
		propertiesBuilder.save();
	}

}
