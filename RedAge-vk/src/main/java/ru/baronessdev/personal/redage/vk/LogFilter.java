package ru.baronessdev.personal.redage.vk;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

public class LogFilter implements Filter {

    private final String pattern = "Response of getting updates";

    public Result filter(LogEvent record) {
        try {
            if (record != null && record.getMessage() != null) {
                String npe = record.getMessage().getFormattedMessage();
                return (npe.contains(pattern) ? Result.DENY : Result.NEUTRAL);
            } else {
                return Result.NEUTRAL;
            }
        } catch (NullPointerException var3) {
            return Result.NEUTRAL;
        }
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object... arg4) {
        try {
            if (message == null) {
                return Result.NEUTRAL;
            } else {
                return (message.contains(pattern) ? Result.DENY : Result.NEUTRAL);
            }
        } catch (NullPointerException var7) {
            return Result.NEUTRAL;
        }
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, Object message, Throwable arg4) {
        try {
            if (message == null) {
                return Result.NEUTRAL;
            } else {
                String npe = message.toString();
                return (npe.contains(pattern) ? Result.DENY : Result.NEUTRAL);
            }
        } catch (NullPointerException var7) {
            return Result.NEUTRAL;
        }
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, Message message, Throwable arg4) {
        try {
            if (message == null) {
                return Result.NEUTRAL;
            } else {

                String npe = message.getFormattedMessage();
                return (npe.contains(pattern) ? Result.DENY : Result.NEUTRAL);
            }
        } catch (NullPointerException var7) {
            return Result.NEUTRAL;
        }
    }

    public Result getOnMatch() {
        return Result.NEUTRAL;
    }

    public Result getOnMismatch() {
        return Result.NEUTRAL;
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }
}
