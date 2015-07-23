package com.netflix.karyon.admin;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JarsAdminResource {
    private static final Logger LOG = LoggerFactory.getLogger(JarsAdminResource.class);
    
    private static final String JAR_PATTERN = "^jar:file:(.+)!/META-INF/MANIFEST.MF$";
    
    public List<JarInfo> get() {
        List<JarInfo> jarInfos = new ArrayList<>();

        Pattern pattern = Pattern.compile(JAR_PATTERN);
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> urls = cl.getResources("META-INF/MANIFEST.MF");
            int id = 0;
            while (urls.hasMoreElements()) {
                URL manifestURL = urls.nextElement();
                InputStream is = manifestURL.openStream();
                String key = manifestURL.toString();
                Matcher matcher = pattern.matcher(key);
                if (matcher.matches()) {
                    key = matcher.group(1);
                }
                jarInfos.add(new JarInfo(id, key, new Manifest(is).getMainAttributes()));
                is.close();
                id++;
            }
        } catch (Exception e) {
            LOG.error("Failed to load environment jar information.", e);
        }

        return jarInfos;
    }

    private static class JarInfo {
        public static final String LIBRARY_OWNER = "Library-Owner";
        public static final String BUILD_DATE = "Build-Date";
        public static final String STATUS = "Status";
        public static final String IMPLEMENTATION_VERSION = "Implementation-Version";
        public static final String IMPLEMENTATION_TITLE = "Implementation-Title";
        public static final String SPECIFICATION_VERSION = "Specification-Version";
        public static final String UNAVAILABLE = "-";

        private final int id;
        private final String name;
        private final String libraryOwner;
        private final String buildDate;
        private final String status;
        private final String implementationVersion;
        private final String implementationTitle;
        private final String specificationVersion;

        private JarInfo(int id, String jar, Attributes mainAttributes) {
            this.id = id;
            this.name = jar;
            libraryOwner = valueOf(mainAttributes, LIBRARY_OWNER);
            buildDate = valueOf(mainAttributes, BUILD_DATE);
            status = valueOf(mainAttributes, STATUS);
            implementationTitle = valueOf(mainAttributes, IMPLEMENTATION_TITLE);
            implementationVersion = valueOf(mainAttributes, IMPLEMENTATION_VERSION);
            specificationVersion = valueOf(mainAttributes, SPECIFICATION_VERSION);
        }

        public String getStatus() {
            return status;
        }

        public String getLibraryOwner() {
            return libraryOwner;
        }

        public String getBuildDate() {
            return buildDate;
        }

        public String getName() {
            return name;
        }

        public String getImplementationVersion() {
            return implementationVersion;
        }

        public String getImplementationTitle() {
            return implementationTitle;
        }

        public String getSpecificationVersion() {
            return specificationVersion;
        }

        private static String valueOf(Attributes mainAttributes, String tag) {
            String value = mainAttributes.getValue(tag);
            return value == null ? UNAVAILABLE : value;
        }
    }
}