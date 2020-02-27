package com.x.tools;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    private static Logger logger = getLogger(Log.class);

    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    private static Logger getLoggerByCaller() {
        return getLogger(ReflectionUtil.getCallerClass(2));
    }

    public static boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public static void trace(String msg) {
        try {
            getLoggerByCaller().trace(msg);
        } catch (Throwable arg1) {
            ;
        }

    }

    public static void trace(String format, Object arg) {
        try {
            getLoggerByCaller().trace(format, arg);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static void trace(String format, Object arg1, Object arg2) {
        try {
            getLoggerByCaller().trace(format, arg1, arg2);
        } catch (Throwable arg3) {
            ;
        }

    }

    public static void trace(String format, Object... arguments) {
        try {
            getLoggerByCaller().trace(format, arguments);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static void trace(String msg, Throwable t) {
        try {
            getLoggerByCaller().trace(msg, t);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public static void debug(String msg) {
        try {
            getLoggerByCaller().debug(msg);
        } catch (Throwable arg1) {
            ;
        }

    }

    public static void debug(String format, Object arg) {
        try {
            getLoggerByCaller().debug(format, arg);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static void debug(String format, Object arg1, Object arg2) {
        try {
            getLoggerByCaller().debug(format, arg1, arg2);
        } catch (Throwable arg3) {
            ;
        }

    }

    public static void debug(String format, Object... arguments) {
        try {
            getLoggerByCaller().debug(format, arguments);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static void debug(String msg, Throwable t) {
        try {
            getLoggerByCaller().debug(msg, t);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public static void info(String msg) {
        try {
            getLoggerByCaller().info(msg);
        } catch (Throwable arg1) {
            ;
        }

    }

    public static void info(String format, Object arg) {
        try {
            getLoggerByCaller().info(format, arg);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static void info(String format, Object arg1, Object arg2) {
        try {
            getLoggerByCaller().info(format, arg1, arg2);
        } catch (Throwable arg3) {
            ;
        }

    }

    public static void info(String format, Object... arguments) {
        try {
            getLoggerByCaller().info(format, arguments);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static void info(String msg, Throwable t) {
        try {
            getLoggerByCaller().info(msg, t);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public static void warn(String msg) {
        try {
            getLoggerByCaller().warn(msg);
        } catch (Throwable arg1) {
            ;
        }

    }

    public static void warn(String format, Object arg) {
        try {
            getLoggerByCaller().warn(format, arg);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static void warn(String format, Object... arguments) {
        try {
            getLoggerByCaller().warn(format, arguments);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static void warn(String format, Object arg1, Object arg2) {
        try {
            getLoggerByCaller().warn(format, arg1, arg2);
        } catch (Throwable arg3) {
            ;
        }

    }

    public static void warn(String msg, Throwable t) {
        try {
            getLoggerByCaller().warn(msg, t);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public static void error(String msg) {
        try {
            getLoggerByCaller().error(msg);
        } catch (Throwable arg1) {
            ;
        }

    }

    public static void error(String format, Object arg) {
        try {
            getLoggerByCaller().error(format, arg);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static void error(String format, Object arg1, Object arg2) {
        try {
            getLoggerByCaller().error(format, arg1, arg2);
        } catch (Throwable arg3) {
            ;
        }

    }

    public static void error(String format, Object... arguments) {
        try {
            getLoggerByCaller().error(format, arguments);
        } catch (Throwable arg2) {
            ;
        }

    }

    public static void error(String msg, Throwable t) {
        try {
            getLoggerByCaller().error(msg, t);
        } catch (Throwable arg2) {
            ;
        }
    }
}
