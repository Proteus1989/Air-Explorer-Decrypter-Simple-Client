package com.suapp.airexplorerfiledecrypter.userinterfaces.command;

import com.suapp.airexplorerfiledecrypter.AirExplorerFileDecrypterMain;
import com.suapp.airexplorerfiledecrypter.streamwrapper.AirExplorerInputStreamWrapper;
import com.suapp.airexplorerfiledecrypter.utils.Document;
import com.suapp.airexplorerfiledecrypter.processor.Task;
import com.suapp.airexplorerfiledecrypter.processor.AirExplorerDecrypter;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.LinkedList;

public class CommandInterpreter
{

    AirExplorerDecrypter processor = new AirExplorerDecrypter();
    String password = null;
    String file = null;
    String outputPath = null;

    public CommandInterpreter()
    {
        processor.addOnStartListener((srcFilepath, srcName, destFilepath, destName) -> System.out.println("Decrypting " + destName + " in " + destFilepath));
        processor.addOnFinishListener((srcFilepath, srcName, destFilepath, destName) -> System.out.println("Decrypted " + destName + " in " + destFilepath));
    }

    public CommandInterpreter(AirExplorerDecrypter processor)
    {
        this.processor = processor;
    }

    public void process(String[] args)
    {
        LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
        extract(list);
        if (password == null)
        {
            System.out.println("Password (-p option) is a required field");
            System.exit(-1);
        }
        if (file == null)
        {
            System.out.println("Filepath (-f option) is a required field");
            System.exit(-1);
        }
        
        Task task = getTask(file, password);
        
        if(outputPath != null)
        {
            File folder = new File(outputPath);
            if(!folder.exists())
            {
                System.out.println("Custom output folder does not exist");
                System.exit(-1);
            }
            if(!folder.isDirectory())
            {
                System.out.println("Custom output folder is not a directory");
                System.exit(-1);
            }
            if(!folder.canWrite())
            {
                System.out.println("Custom output folder is not writable");
                System.exit(-1);
            }
        }
        else if(!new File(task.getOutputPath()).canWrite())
        {
            System.out.println("Origin file directory is not writable");
            System.exit(-1);
        }

        task.setOutputPath(outputPath);

        

        processor.executeSyncTask(task);

    }

    private void extract(LinkedList<String> list)
    {
        if (list.isEmpty())
            return;
        if (list.get(0).startsWith("-"))
            extractPair(list);
        else
        {
            System.out.println("Unexpected value: " + list.get(0));
            System.exit(-1);
        }
    }

    private void extractPair(LinkedList<String> list)
    {
        String command = list.poll();

        switch (command)
        {
            case "-p":
            case "--password":
            {
                if (list.isEmpty())
                {
                    System.out.println("Missing password value");
                    System.exit(-1);
                }
                password = list.poll();
                break;
            }
            case "-f":
            case "--file":
            {
                if (list.isEmpty())
                {
                    System.out.println("Missing file value");
                    System.exit(-1);
                }
                file = list.poll();
                break;
            }
            case "-o":
            case "--output":
            {
                if (list.isEmpty() || list.get(0).startsWith("-"))
                {
                    System.out.println("Missing output value");
                    System.exit(-1);
                }
                outputPath = list.poll();
                break;
            }
            case "-v":
            case "--version":
            {
                System.out.println("Air Explorer File Decripter v" + AirExplorerFileDecrypterMain.version);
                System.exit(0);
            }
            case "-h":
            case "--help":
            {
                System.out.println("Air Explorer File Decripter v" + AirExplorerFileDecrypterMain.version);
                System.out.println("Use example: java -jar AirExplorerFileDecripter.jar -f /home/user/file.cloudencoded -p password [-o /home/user/decrypted_folder]");
                System.out.println();
                System.out.println("Commands:");
                System.out.println("\t-f\t--file\t\t[Required] Encrypted filepath");
                System.out.println("\t-p\t--password\t[Required] File password");
                System.out.println("\t-o\t--output\tCustom output folder. Origin file folder by default");
                System.out.println("\t-v\t--version\tPrint current program version");
                System.exit(0);
            }
        }

        extract(list);
    }

    private Task getTask(String filePath, String pass)
    {
        File file = new File(filePath);

        if (!file.exists())
        {
            System.out.println("File does not exist");
            System.exit(-1);
        }

        String name = file.getName();
        if (!(name.toLowerCase().endsWith(".cloudencoded2") || name.toLowerCase().endsWith(".cloudencoded")))
        {
            System.out.println("File extension must finish in .cloudencoded or ");
            System.exit(-1);
        }

        try 
        {
            FileInputStream fis = new FileInputStream(file);
            AirExplorerInputStreamWrapper stream = new AirExplorerInputStreamWrapper(fis, new Document("filename", name).append("password", pass));
            if (name.toLowerCase().endsWith(".cloudencoded2"))
                System.out.println(name + " (" + AirExplorerInputStreamWrapper.decryptName(name, pass) + ") verified");
            else
                System.out.println(name + " (" + name.replace(".cloudencoded", "") + ") verified");

            return new Task(stream, file);

        } catch (Exception e)
        {
            System.out.println("Missmatching password for " + name + ". Skipping it...");
            System.exit(-1);
        }

        System.out.println("An error occurred");
        System.exit(-1);
        return null;

    }
}
