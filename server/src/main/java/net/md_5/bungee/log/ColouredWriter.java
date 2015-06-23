package net.md_5.bungee.log;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import jline.console.ConsoleReader;
import org.fusesource.jansi.Ansi;

public class ColouredWriter extends Handler
{

    private final ConsoleReader console;

    public ColouredWriter(ConsoleReader console)
    {
        this.console = console;
    }

    public void print(String s)
    {
        try
        {
            console.print( ConsoleReader.RESET_LINE + s + Ansi.ansi().reset().toString() );
            console.drawLine();
            console.flush();
        } catch ( IOException ex )
        {
        }
    }

    @Override
    public void publish(LogRecord record)
    {
        print( getFormatter().format( record ) );
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close() throws SecurityException
    {
    }
}
