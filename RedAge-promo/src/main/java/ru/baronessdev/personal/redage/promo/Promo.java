package ru.baronessdev.personal.redage.promo;

public class Promo {
    private String name;
    private String author;
    private int usages;
    private String command;

    public Promo(String name, String author, int usages, String command) {
        this.name = name;
        this.author = author;
        this.usages = usages;
        this.command = command;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getUsages() {
        return usages;
    }

    public void setUsages(int usages) {
        this.usages = usages;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
