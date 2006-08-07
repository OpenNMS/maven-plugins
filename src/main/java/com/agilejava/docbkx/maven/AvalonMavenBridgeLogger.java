package com.agilejava.docbkx.maven;

import org.apache.avalon.framework.logger.Logger;
import org.apache.maven.plugin.logging.Log;

public class AvalonMavenBridgeLogger implements Logger {

    private Log mavenLog;

    private boolean errorIsDebug = false;

    private boolean warnIsDebug = false;

    public AvalonMavenBridgeLogger(Log mavenLog) {
        this.mavenLog = mavenLog;
    }

    public AvalonMavenBridgeLogger(Log mavenLog, boolean errorIsDebug,
            boolean warnIsDebug) {
        this(mavenLog);
        this.errorIsDebug = errorIsDebug;
        this.warnIsDebug = warnIsDebug;
    }

    public void debug(String arg0) {
        mavenLog.debug(arg0);
    }

    public void debug(String arg0, Throwable arg1) {
        mavenLog.debug(arg0, arg1);
    }

    public void error(String arg0) {
        if (errorIsDebug) {
            debug(arg0);
        } else {
            mavenLog.error(arg0);
        }
    }

    public void error(String arg0, Throwable arg1) {
        if (errorIsDebug) {
            debug(arg0, arg1);
        } else {
            mavenLog.error(arg0, arg1);
        }
    }

    public void fatalError(String arg0) {
        mavenLog.error(arg0);
    }

    public void fatalError(String arg0, Throwable arg1) {
        mavenLog.error(arg0, arg1);
    }

    public Logger getChildLogger(String arg0) {
        return null;
    }

    public void info(String arg0) {
        mavenLog.info(arg0);
    }

    public void info(String arg0, Throwable arg1) {
        mavenLog.info(arg0, arg1);
    }

    public boolean isDebugEnabled() {
        return mavenLog.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return mavenLog.isErrorEnabled();
    }

    public boolean isFatalErrorEnabled() {
        return mavenLog.isErrorEnabled();
    }

    public boolean isInfoEnabled() {
        return mavenLog.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return mavenLog.isWarnEnabled();
    }

    public void warn(String arg0) {
        if (warnIsDebug) {
            debug(arg0);
        } else {
            mavenLog.warn(arg0);
        }
    }

    public void warn(String arg0, Throwable arg1) {
        if (warnIsDebug) {
            debug(arg0, arg1);
        } else {
            mavenLog.warn(arg0, arg1);
        }
    }

}
