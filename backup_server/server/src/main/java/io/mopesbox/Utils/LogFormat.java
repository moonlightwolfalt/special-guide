package io.mopesbox.Utils;

import java.util.logging.*;

class LogFormat extends Formatter {
    
    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getLevel()).append(':');
        sb.append(record.getMessage()).append('\n');
        return sb.toString();
    }    
}