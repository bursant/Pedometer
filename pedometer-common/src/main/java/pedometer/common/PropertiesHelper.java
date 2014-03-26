package pedometer.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHelper {

    @SuppressWarnings("unused")
    public static Properties loadProperties(Properties properties, Class<?> baseClass, String... propertiesName) {
        if (properties == null) {
            properties = new Properties();
        }
        for (String propertyName : propertiesName) {
            properties = loadProperties(properties, baseClass, propertyName);
        }
        return properties;
    }

    @SuppressWarnings("unused")
    public static Properties loadProperties(Properties properties, String... paths) {
        if (properties == null) {
            properties = new Properties();
        }
        for (String path : paths) {
            properties = loadProperties(properties, path);
        }
        return properties;
    }

    public static Properties loadProperties(Properties properties, Class<?> clazz, String bundle) {
        if (properties == null) {
            properties = new Properties();
        }
        InputStream in = null;
        try {
            in = clazz.getResourceAsStream(bundle);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("Could not load properties for bundle " + bundle);
            e.printStackTrace();

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

        return properties;
    }

    public static Properties loadProperties(Properties properties, String path) {
        if (properties == null) {
            properties = new Properties();
        }
        InputStream in = null;
        try {
            in = new FileInputStream(path);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("Could not load properties for file " + path);
            e.printStackTrace();

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

        return properties;
    }

    public static String getProperty(Properties properties, String key, String defaultValue) {
        if (properties == null) return defaultValue;
        String property = properties.getProperty(key);
        return (property != null ? property : defaultValue);
    }
}
