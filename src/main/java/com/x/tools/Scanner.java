package com.x.tools;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * class文件扫描
 * 
 * @author 
 */
public class Scanner {
	
    private static final String WIN_FILE_SEPARATOR = "\\";
    // 文件分隔符"\"
    private static final String FILE_SEPARATOR = "/";
    // package扩展名分隔符
    private static final String PACKAGE_SEPARATOR = ".";
    // java类文件的扩展名
    private static final String CLASS_FILE_EXT = ".class";
    // jar类文件的扩展名
    private static final String JAR_FILE_EXT = ".jar";
    // 目录
    private static final String APP_CLASS_END = "classes/";

    /**
     * 获取项目的所有classpath ，包括 APP_CLASS_PATH 和所有的jar文件
     */
    private static Set<String> getClassPathes() throws Exception {
        Set<String> set = new LinkedHashSet<String>();
        URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        while(set.isEmpty() && loader != null) {
            getClassPathes(set, loader);
            ClassLoader parent = loader.getParent();
            loader = (parent instanceof URLClassLoader) ? (URLClassLoader) parent : null;
        }
        return set;
    }

    protected static void getClassPathes(Set<String> set, URLClassLoader classLoader) {
		URL[] urlAry = classLoader.getURLs();
        for (URL url : urlAry) {
            set.add(url.getPath());
        }
    }

    /**
     * 获取文件下的所有文件(递归)
     */
    private static Set<File> getFiles(File file) {
        Set<File> files = new LinkedHashSet<File>();
        if (!file.isDirectory()) {
            files.add(file);
        } else {
            File[] subFiles = file.listFiles();
            for (File f : subFiles) {
                files.addAll(getFiles(f));
            }
        }
        return files;
    }

    /**
     * 获取文件下的所有.class文件
     */
    private static Set<File> getClassFiles(File file) {
        // 获取所有文件
        Set<File> files = getFiles(file);
        Set<File> classes = new LinkedHashSet<File>();
        // 只保留.class 文件
        for (File f : files) {
            if (f.getName().endsWith(CLASS_FILE_EXT)) {
                classes.add(f);
            }
        }
        return classes;
    }

    /**
     * 得到文件夹下所有class的全包名
     */
    private static Set<ClassEntry> getFromDir(File file) {
        Set<File> files = getClassFiles(file);
        Set<ClassEntry> classes = new LinkedHashSet<ClassEntry>();
        for (File f : files) {
        	classes.add(new ClassEntry(f));
        }
        return classes;
    }

    /**
     * 获取jar文件里的所有class文件名
     */
    private static Set<ClassEntry> getFromJar(JarFile jarFile) throws Exception {
        Enumeration<JarEntry> entries = jarFile.entries();
        Set<ClassEntry> classes = new LinkedHashSet<ClassEntry>();
        while (entries.hasMoreElements()) {
            JarEntry entry = (JarEntry) entries.nextElement();
            if (entry.getName().endsWith(CLASS_FILE_EXT)) {
            	classes.add(new ClassEntry(entry));
            }
        }
        return classes;
    }
    
    public static List<String> getClassPathes(String includes, String excludes) {
        return getClassPathes(new ScanMatcher(includes), new ScanMatcher(excludes));
    }

	public static List<String> getClassPathes(ScanMatcher includes, ScanMatcher excludes) {
		List<String> ret = new ArrayList<>();
        try {
            Set<String> allclassPath = getClassPathes();
            for (String path : allclassPath) {
                if (!path.endsWith(JAR_FILE_EXT) || (includes.match(path) && !excludes.match(path))) ret.add(path);
            }
        } catch (Exception ex) {
            // ignore
        }
        return ret;
	}

    public static List<ClassEntry> scan(String includes, String excludes) {
        return scan(new ScanMatcher(includes), new ScanMatcher(excludes));
    }

	public static List<ClassEntry> scan(ScanMatcher includes, ScanMatcher excludes) {
		List<ClassEntry> ret = new ArrayList<ClassEntry>();
        try {
            Set<String> allclassPath = getClassPathes();
            for (String path : allclassPath) {
                if (path.endsWith(JAR_FILE_EXT)) {
                    if(!includes.match(path) || excludes.match(path)) continue;
                    Set<ClassEntry> classeNames = getFromJar(new JarFile(new File(path)));
                    for (ClassEntry clazz : classeNames) {
                        if(!includes.match(clazz.name) || excludes.match(clazz.name)) continue;
                        ret.add(clazz);
                    }
                } else {
                    Set<ClassEntry> classNames = getFromDir(new File(path));
                    for (ClassEntry clazz : classNames) {
                        if(!includes.match(clazz.name) || excludes.match(clazz.name)) continue;
                        ret.add(clazz);
                    }
                }
            }
        } catch (Exception ex) {
            // ignore
        }
        return ret;
	}
    
    public static class ClassEntry {
    	public final String name;
    	public final long size;
    	public final long modifyTime;
    	public ClassEntry(File file) {
    		String fileName = file.getAbsolutePath().replace(WIN_FILE_SEPARATOR, FILE_SEPARATOR);
            this.name = fileName.substring(fileName.indexOf(APP_CLASS_END) + APP_CLASS_END.length(), fileName.indexOf(CLASS_FILE_EXT)).replace(FILE_SEPARATOR, PACKAGE_SEPARATOR);
            this.size = file.length();
            this.modifyTime = file.lastModified();
		}
    	public ClassEntry(JarEntry entry) {
    		this.name = entry.getName().substring(0, entry.getName().indexOf(CLASS_FILE_EXT)).replace(FILE_SEPARATOR, PACKAGE_SEPARATOR);
            this.size = entry.getSize();
            this.modifyTime = entry.getTime();
    	}
    	public ClassEntry(String name, long size, long modifyTime) {
    		this.name = name;
    		this.size = size;
    		this.modifyTime = modifyTime;
    	}
    }
    
    public static class ScanMatcher {
        Pattern[] patterns;
        public ScanMatcher(String res) {
            if(res == null || res.length() == 0) {
                patterns = new Pattern[0];
                return;
            }
            
            String[] regs = res.split(";");
            patterns = new Pattern[regs.length];
            for (int i = 0; i < regs.length; i++) {
                String reg = regs[i];
                reg = reg.endsWith("*") ? reg : reg + "$";
                reg = reg.replace(".", "\\.");
                reg = reg.replace("*", ".*");
                patterns[i] = Pattern.compile(reg);
            }
        }
        public boolean match(String path) {
            for (Pattern p : patterns) {
                if(p.matcher(path).find()) {
                    return true;
                }
            }
            return false;
        }
    }

}
