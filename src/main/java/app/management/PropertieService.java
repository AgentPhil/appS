package app.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.Properties;

import logging.Logger;
import logging.LoggerFactory;

public class PropertieService extends ApplicationService {
	
    private static final String DEFAULT_CONFIG_FILE = "default.properties";
    
    private static final String CUSTOM_CONFIG_FILE = "config.properties";
    
    private static Properties props;

    private Logger logger = LoggerFactory.getInstance().getLogger(this.getClass());
    
    private boolean unsavedChanges = false;

    
    /*
     * TODO cache all loaded Fields and their object per property, so that changing those properties becomes possible from a central point
     */
    
    
    
    public PropertieService() {
    	logger.info("instanced properties");
    	props = new Properties();
        loadProperties(DEFAULT_CONFIG_FILE);
        loadProperties(CUSTOM_CONFIG_FILE);      
        for (Entry<Object, Object> entry: props.entrySet()) {
        	logger.info(entry.getKey() + " = " + entry.getValue());
        }
    }

    private void loadProperties(String configFile) {
    	logger.info("loading " + configFile);
        File file = new File(configFile);
        if (!file.exists()) {
        	logger.info("properties file does not exist, creating new one");
        	try {
				file.createNewFile();
			} catch (IOException e) {
				logger.error("failed creating properties file " + configFile, e);
			}
        }
        try (InputStream input = new FileInputStream(file)) {
            props.load(input);
        } catch (IOException e) {
        	logger.error("failed loading properties from " + configFile, e);
        }
    }
    
    public void saveProperties() {
    	logger.info("storing properties");
        try (OutputStream output = new FileOutputStream(CUSTOM_CONFIG_FILE)) {
        	props.store(output, "Custom Properties");
        } catch (IOException e) {
            logger.error("failed storing properties to " + CUSTOM_CONFIG_FILE, e);
        }
    }
    
    public int getPropertyAsInt(String key) {
    	return Integer.parseInt(props.getProperty(key));
    }
    
    public int getPropertyAsInt(String key, int defaultValue) {
    	return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
    }
    
    public Object setProperty(String key, String value) {
    	logger.debug("setting property '" + key + "' to value '" + value + "'");
    	Object previousValue = props.setProperty(key, value);
    	if (previousValue == null || !previousValue.equals(value)) {
    		unsavedChanges = true;
    	}
    	return previousValue;
    }
    
    public boolean isUnsavedChanges() {
		return unsavedChanges;
	}
    
    public void restoreDefaultProperties() {
        loadProperties(DEFAULT_CONFIG_FILE);
        saveProperties();
    }

	/**
	 * @return null if not defined
	 */
	public String getProperty(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	/**
	 * @return null if not defined
	 */
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	
    public void loadProperties(ApplicationService service) {
        Class<?> clazz = service.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Property.class)) {
                Property propertyAnnotation = field.getAnnotation(Property.class);
                String propertyName = propertyAnnotation.name();
                String propertyValue = getProperty(propertyName);
                logger.debug("assigning property '" + propertyName + "' with value '" + propertyValue + "' to class " + clazz.getName());
                field.setAccessible(true);
                try {
                	if (field.getType().isAssignableFrom(long.class)) {
                        field.set(service, Long.parseLong(propertyValue));
                    } else if (field.getType().isAssignableFrom(int.class)) {
                        field.set(service, Integer.parseInt(propertyValue));
                    } else if (field.getType().isAssignableFrom(double.class)) {
                        field.set(service, Double.parseDouble(propertyValue));
                    } else if (field.getType().isAssignableFrom(float.class)) {
                        field.set(service, Float.parseFloat(propertyValue));
                    } else if (field.getType().isAssignableFrom(boolean.class)) {
                        field.set(service, Boolean.parseBoolean(propertyValue));
                    } else if (field.getType().isAssignableFrom(char.class)) {
                        if (propertyValue.length() == 1) {
                            field.set(service, propertyValue.charAt(0));
                        } else {
                            throw new IllegalArgumentException("Invalid character value: " + propertyValue);
                        }
                    } else if (field.getType().isAssignableFrom(String.class)) {
                        field.set(service, propertyValue);
                    } else {
                        throw new IllegalArgumentException("Unsupported field type: " + field.getType().getName());
                    }
                } catch (IllegalAccessException e) {
                    logger.error("Failed to assign property " + propertyName + " with value " + propertyValue, e);
                }
            }
        }
    }
}