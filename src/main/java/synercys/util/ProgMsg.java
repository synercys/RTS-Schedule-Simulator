package synercys.util;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Formatter;

/**
 * Created by CY on 2/13/17.
 */
public class ProgMsg {
    private static ProgMsg instance = null;
    private static TextFlow outDoc = null;

    private ProgMsg() {
        if (instance == null) {
            instance = new ProgMsg();
        }
    }

//    public static synchronized ProgMsg getInstance()
//    {
//        if (instance == null) {
//            instance = new ProgMsg();
//        }
//
//        return instance;
//    }

    public static void setTargetDoc(TextFlow inDoc)
    {
        outDoc = inDoc;
        sysPutLine("Log messenger initialized.");
    }

    public static void putLine(String format, Object... args)
    {
          /* Uncomment the following code to print out the method caller. */
//        Throwable t = new Throwable();
//        StackTraceElement ste = t.getStackTrace()[1];
//        inStr = "[" + ste.getMethodName() + "] " + inStr;
        colorPutLineToDoc(new Formatter().format(format, args).toString() + "\r\n", Color.BLACK);

        Throwable t = new Throwable();
        StackTraceElement ste = t.getStackTrace()[1];
        format = "[" + ste.getMethodName() + "] " + format;
        System.out.format(format + "\r\n", args);
    }

    /* This function only outputs logs to system console. */
    public static void debugPutline(String format, Object... args)
    {
        Throwable t = new Throwable();
        StackTraceElement ste = t.getStackTrace()[1];
        format = "[" + ste.getMethodName() + "] " + format;
        System.err.format(format + "\r\n", args);
    }

    public static void errPutline(String format, Object... args)
    {
        colorPutLineToDoc(new Formatter().format(format, args).toString() + "\r\n", Color.RED);

        Throwable t = new Throwable();
        StackTraceElement ste = t.getStackTrace()[1];
        format = "[" + ste.getMethodName() + "] " + format;
        System.err.format(format + "\r\n", args);
    }

    public static void sysPutLine(String format, Object... args)
    {
        colorPutLineToDoc(new Formatter().format(format, args).toString() + "\r\n", Color.BLUE);

        Throwable t = new Throwable();
        StackTraceElement ste = t.getStackTrace()[1];
        format = "[" + ste.getMethodName() + "] " + format;
        System.out.format(format + "\r\n", args);
    }

    private static void colorPutLineToDoc(String inStr, Color inColor)
    {
        if (outDoc == null)
        {
            System.err.println("Error: outDoc in ProgramLogMessenger has not been initialized.");
            return;
        }

        Text newText = new Text(inStr);
        newText.setFill(inColor);
        outDoc.getChildren().add(newText);
    }
}
