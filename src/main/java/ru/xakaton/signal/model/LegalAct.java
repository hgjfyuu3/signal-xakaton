package ru.xakaton.signal.model;

public record LegalAct(String numberOfTheLegalAct, String nameOfTheLegalAct, String pdfFileAddress) {

    private static final String URI_DIAMOND_REGION = "https://www.алмазный-край.рф";

    public String toString() {
        return numberOfTheLegalAct + "\n" +
                nameOfTheLegalAct + "\n" +
                URI_DIAMOND_REGION + pdfFileAddress + "\n";
    }

}