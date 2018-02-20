package synercys.util.file;

import java.io.*;

/**
 * Created by jjs on 2/13/17.
 */
public class FileHandler {
    protected static final int OPEN_FILE = 0;
    protected static final int SAVE_FILE = 1;

    protected String filePath = "";
    protected BufferedReader fileReader = null;
    protected BufferedWriter fileWriter = null;

    /**
     * Open dialog for users to select a file.
     * @return 'null' if the dialog is canceled by the user, 'BufferReader' if it opens the file successfully.
     * @exception  IOException if the file is unable to be opened.
     */
//    protected BufferedReader openFileFromDialog() throws IOException
//    {
//        filePath = openFileChooserDialog(OPEN_FILE);
//        if (filePath == null)
//            return null;
//
//        fileReader = openFile(filePath);
//        if (fileReader == null)
//            throw new IOException("IOException @ openFileFromDialog(): File path is incorrect.");
//        else
//            return fileReader;
//    }

    /**
     * Open a dialog for users to select a file to open/create and write.
     * @return 'null' if the dialog is canceled by the user, 'BufferWriter' if it opens/creates the file successfully.
     * @exception  IOException if the file is unable to be opened.
     */
//    protected BufferedWriter openWriteFileFromDialog() throws IOException
//    {
//        filePath = openFileChooserDialog(SAVE_FILE);
//        if (filePath == null)
//            return null;
//
//        fileWriter = openToWriteFile(filePath);
//        if (fileWriter == null)
//            throw new IOException("IOException @ openWriteFileFromDialog(): File path is incorrect.");
//        else
//            return fileWriter;
//    }

    /**
     * Open dialog for users to select a file.
     * @param dialogType OPEN_FILE or SAVE_FILE.
     * @return The absolute path of the selected file from the dialog.
     */
//    private String openFileChooserDialog(int dialogType)
//    {
//        JFileChooser fileChooser = new JFileChooser();
//        //fileChooser.setFont(new Font("TimesRoman", Font.PLAIN, 32));//;setPreferredSize(new Dimension(800, 600));
//        recursivelySetFonts(fileChooser, new Font("TimesRoman", Font.PLAIN, 18));
//        fileChooser.setPreferredSize(new Dimension(800, 600));
//
//        int dialogReturnValue = 0;
//        if (dialogType == OPEN_FILE)
//        {
//            dialogReturnValue = fileChooser.showOpenDialog(null);
//        }
//        else if (dialogType == SAVE_FILE)
//        {
//            dialogReturnValue = fileChooser.showSaveDialog(null);
//        }
//
//        if (dialogReturnValue == JFileChooser.APPROVE_OPTION)
//        {
//            return fileChooser.getSelectedFile().getAbsolutePath();
//        }
//        else
//        {
//            // User cancels the dialog.
//            return null;
//        }
//    }

    /**
     * Load file as a BufferReader.
     * @param filePath The path of a selected log file to be opened.
     * @return a BufferedReader of the opened file.
     */
    protected BufferedReader openFile(String filePath)
    {
        try {
            fileReader = new BufferedReader(new FileReader(filePath));
            return fileReader;
        }
        catch (IOException x)
        {
            System.err.format("IOException @ openFile() while reading file: %s%n", x);
            //throw new InvalidDataException("Can't load file.");
            return null;
        }
    }

    /**
     * Load file as a BufferWriter (to write a file).
     * @param filePath The path of a selected log file to be opened/created and written.
     * @return a BufferedWriter of the opened file.
     */
    protected BufferedWriter openToWriteFile(String filePath)
    {
        try {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath));
            return fileWriter;
        }
        catch (IOException x)
        {
            System.err.format("IOException @ openWriteFile() while reading file: %s%n", x);
            //throw new InvalidDataException("Can't load file.");
            return null;
        }
    }

    public Boolean isFileReaderOpened() {
        return fileReader==null ? false : true;
    }

}
