package com.suapp.airexplorerfiledecrypter;


import com.suapp.airexplorerfiledecrypter.userinterfaces.command.CommandInterpreter;
import com.suapp.airexplorerfiledecrypter.userinterfaces.gui.GUI;

public class AirExplorerFileDecrypterMain
{
    public static final String version = "1.2";
    public static final String name = "Air Explorer File Decrypter";

    public static void main(String[] args) throws Exception
    {
        if(args.length > 0)
        {
            new CommandInterpreter().process(args);
        }else
        {
            new GUI().initGUI();
        }
    }
}
