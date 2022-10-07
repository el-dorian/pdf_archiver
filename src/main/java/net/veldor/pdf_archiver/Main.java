package net.veldor.pdf_archiver;

import net.veldor.pdf_archiver.utils.Parser;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            Parser.parse(args[0], args[1]);
        } else {
            System.out.println("Нужно передать имя файла и адрес папки назначения");
        }
    }
}