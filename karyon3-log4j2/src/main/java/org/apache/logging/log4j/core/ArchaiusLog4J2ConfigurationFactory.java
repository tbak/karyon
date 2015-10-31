package org.apache.logging.log4j.core;

import java.util.Set;

import javax.inject.Inject;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.util.NameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.archaius.Config;

/**
 * Archaius based ConfigurationFactory for log4j2 which offers two key functionalities
 * 1.  Load all configuration from archaius (i.e application.properties, persisted properties, )
 * 2.  Update configuration at runtime by calling 
 *       ((LoggerContext) LogManager.getContext(false)).reconfigure();
 *
 * To enable in a Karyon application simply add ArchaiusLog4jConfigurationModule to the list
 * of modules.  
 * 
 * IMPORTANT:
 * While log4j2 does have a robust plugin architecture it's very tricky to configure an 
 * ConfigurationFactory plugin since the ConfigurationFactory must be initialized before the 
 * plugin manager.  To make this possible we make the ArchaiusLog4JConfigurationFactory look 
 * like one of the log4j2 internal factories by putting it in the org.apache.logging.log4j.core 
 * package and referencing it from META-INF/org/apach/logging/log4j/core/config/plugins/Log4j2Plugins.dat. 
 * 
 * @author elandau
 *
 */
@Plugin(name = "ArchaiusLog4j2ConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(10)
public class ArchaiusLog4J2ConfigurationFactory extends ConfigurationFactory {
	
    public static final String[] SUFFIXES = new String[] {".xml", "*"};
    
    private static Config config;
    private static Set<Log4jConfigurator> configurators;
    
    private static final Logger LOG = LoggerFactory.getLogger(ArchaiusLog4J2ConfigurationFactory.class);
    
    @Inject
    public static void initialize(Config _config, Set<Log4jConfigurator> _configurators) {
        config = _config;
        configurators = _configurators;
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.reconfigure();
    }
    
    @Override
    protected String[] getSupportedTypes() {
        return SUFFIXES;
    }

    @Override
    public Configuration getConfiguration(ConfigurationSource source) {

        XmlConfiguration result;

        if (config != null) {
            LOG.info("Creating archaius based Configuration");
            result = new ArchaiusLog4j2Configuration(source);
        } else {
            LOG.info("Creating default XmlConfiguration");
            result = new XmlConfiguration(source);
        }

        return result;
    }
    
    public static class ArchaiusLog4j2Configuration extends XmlConfiguration {
        private static final long serialVersionUID = 1L;
        
        public ArchaiusLog4j2Configuration(ConfigurationSource configSource) {
            super(configSource);
        }

        @Override
        public LoggerConfig getLoggerConfig(String name) {
            LoggerConfig loggerConfig = super.getLoggerConfig(name);
            String substr = name;
            while ((substr = NameUtil.getSubName(substr)) != null) {
                String value = config.getString("log4j.logger." + substr, null);
                if (value != null) {
                    loggerConfig.setLevel(Level.getLevel(value));
                }
            }
            return loggerConfig;
        }
        
        @Override
        protected void doConfigure() {
            super.doConfigure();
 
            // Call all injected Configurators to apply their configuration
            if (configurators != null) {
                for (Log4jConfigurator configurator : configurators) {
                    configurator.doConfigure(this);
                }
            }
            
            // On any change to the root configuration trigger a reconfigure of this log4j configuration
            // This will result in getLoggerConfig(name) getting called for each instance of Logger
            config.addListener(ConfigListeners.any((config) -> {
                LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
                ctx.updateLoggers(); 
            }));
        }
    }
}