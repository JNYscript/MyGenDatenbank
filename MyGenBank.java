
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;

public class MyGenBank {

    protected Fasta fasta;
    protected String definition;
    protected int sequenzLenght;
    protected String organism;
    protected String[] keywords;
    protected String moleculeType;


    public MyGenBank(String header, String sequenz, String definition, String organism, String keywords,
                     String moleculeType) {
        super();
        try {
            this.fasta = new Fasta(header, sequenz);
            this.definition = definition;
            this.organism = organism;
            this.keywords = keywords.split(",");
            this.moleculeType = moleculeType;
            this.sequenzLenght = fasta.getDNAlength();
        } catch (Fasta.IllegalHeaderException | Fasta.IllegalSequenceException e) {
            this.fasta = new Fasta(">ERROR", "ATCG");
            e.printStackTrace();

        }

    }

    public MyGenBank(Fasta fasta, String definition, String organism, String keywords, String moleculeType) {
        super();
        try {
            this.fasta = fasta;
            this.definition = definition;
            this.organism = organism;
            this.keywords = keywords.split(",");
            this.moleculeType = moleculeType;
            this.sequenzLenght = fasta.getDNAlength();
        } catch (Fasta.IllegalHeaderException | Fasta.IllegalSequenceException e) {
            this.fasta = new Fasta(">ERROR", "ATCG");
            e.printStackTrace();

        }
    }

    public MyGenBank(String filename, String filetype) throws IllegalArgumentException,Fasta.IllegalSequenceException,Fasta.IllegalHeaderException{

        if (filetype.equals("MyGenBank")) {
            this.readFromFileMyGenBank(filename);
        } else if (filetype.equals("EMBL")) {
            this.EMBLToMyGenBank(filename);
        } else if (filetype.equals("GenBank")) {
            this.GenBankToMyGenBank(filename);
        } else {
            throw new IllegalArgumentException("ungültiger Datentyp");
        }

    }

    public void writeToFile(String filename) {

        File datei = new File("resources/" + filename);

        if (!datei.exists()) {

            try {
                datei.createNewFile();
            } catch (IOException e) {
                System.out.println("Datei konnte nicht erstellt werden");
                e.printStackTrace();
            }
        }

        try {

            FileWriter writer = new FileWriter("resources/" + filename);

            writer.write(this.toString());

            writer.close();
            System.out.println("Schreiben der Datei erfolgreich");

        } catch (IOException e) {
            System.out.println("SChreiben der Datei nicht erfolgreich");
            e.printStackTrace();

        }

    }

    @Override
    public String toString() {

        String forSquenz = this.fasta.getDna().replaceAll("(.{80})", "$1\n");

        return "id:" + this.fasta.getHeader() + "\n" + "definition:" + this.definition + "\n" + "sequenzLength:"
                + this.sequenzLenght + "\n" + "organism:" + this.organism + "\n" + "moleculeType:" + this.moleculeType
                + "\n" + "keywords:"
                + Arrays.toString(this.keywords).replace("[", "").replace("]", "").replace(" ", "").trim() + "\n"
                + "sequenz:\n" + forSquenz + "\n//";

    }

    protected String regexMatcher(String search, String pattern) {

        java.util.regex.Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(search);

        return matcher.find() ? matcher.group(1) : "";

    }

    public void readFromFileMyGenBank(String file) {
        try {
            String fileString = this.fileToString(file);

            String id = this.regexMatcher(fileString, "id:(.*?)\n");
            String sequenz = this.regexMatcher(fileString, "sequenz:(.*?)//").replace("\n", "");

            this.fasta = new Fasta(id, sequenz);
            this.definition = this.regexMatcher(fileString, "definition:(.*?)\n");
            this.sequenzLenght = fasta.getDNAlength();
            this.organism = this.regexMatcher(fileString, "organism:(.*?)\n");
            this.moleculeType = this.regexMatcher(fileString, "moleculeType:(.*?)\n");
            this.keywords = this.regexMatcher(fileString, "keywords:(.*?)\n").split(",");
        } catch (Fasta.IllegalHeaderException | Fasta.IllegalSequenceException e) {
            this.fasta = new Fasta(">ERROR", "ATCG");
            e.printStackTrace();

        }
    }

    protected String fileToString(String filename) {

        String ausgabe = "";
        File datei = new File("resources/" + filename);
        if (!datei.exists() || !datei.canRead()) {
            System.out.println("Datei kann nicht gelesen werden");
            return ausgabe;
        } else {

            try {

                ausgabe += new String(Files.readAllBytes(Paths.get("resources/" + filename)));

            } catch (IOException e) {

                e.printStackTrace();
            }

            return ausgabe;

        }

    }

    protected String removeNonC(String s) {

        return s.replaceAll("  ", "").replaceAll("\n", "").replaceAll("\r", "");

    }

    public void GenBankToMyGenBank(String inputFilename,String outputFilename) {
        this.GenBankToMyGenBank(inputFilename);
        this.writeToFile(outputFilename);
    }

    public void GenBankToMyGenBank(String filename) {

        try {
            String fileString = this.fileToString(filename);

            String header = ">" + this.regexMatcher(fileString, "LOCUS *(.*?) ");
            String sequenz = this.regexMatcher(fileString, "ORIGIN(.*?)//").toUpperCase().replaceAll("[^ATCG]", "");
            this.fasta = new Fasta(header, sequenz);
            this.definition = this.removeNonC(this.regexMatcher(fileString, "DEFINITION *(.*?)\n[A-Z]+"));
            this.sequenzLenght = fasta.getDNAlength();
            this.organism = this.removeNonC(this.regexMatcher(fileString, "ORGANISM(.*?)\n[A-Z]+"));
            this.moleculeType = this.regexMatcher(fileString, "bp *(.*?) ");
            this.keywords = this.removeNonC(this.regexMatcher(fileString, "KEYWORDS(.*?)\n[A-Z]+")).split(",");
        } catch (Fasta.IllegalHeaderException | Fasta.IllegalSequenceException e) {
            this.fasta = new Fasta(">ERROR", "ATCG");
            e.printStackTrace();

        }

    }


    public void EMBLToMyGenBank(String inputFilename,String outputFilename) {
        this.EMBLToMyGenBank(inputFilename);
        this.writeToFile(outputFilename);
    }

    public void EMBLToMyGenBank(String filename) {

        try {
            String fileString = this.fileToString(filename);

            String header = ">"+this.regexMatcher(fileString, "\nID *(.*?) ");
            String sequenz = this.regexMatcher(fileString, "\nSQ.*?\n(.*?)//").toUpperCase().replaceAll("[^ATCG]", "");
            this.fasta = new Fasta(header, sequenz);
            this.definition = this.removeNonC(this.regexMatcher(fileString, "\nDE(.*?)\n[^DE]")).strip();
            this.sequenzLenght = fasta.getDNAlength();
            this.organism = this.removeNonC(this.regexMatcher(fileString, "\nOS(.*?)\n[OS]")).strip();
            this.moleculeType = this.regexMatcher(fileString, "\nID.*?;(.*?);").strip();
            this.keywords = this.removeNonC(this.regexMatcher(fileString, "\nKW(.*?)\n[^KW]")).strip().split(",");
        } catch (Fasta.IllegalHeaderException | Fasta.IllegalSequenceException e) {

            this.fasta = new Fasta(">ERROR", "ATCG");
            e.printStackTrace();

        }

    }

    public static void main(String[] args) {

        String[] keywords = { "test", "test2" };

        MyGenBank test2 = new MyGenBank(">DNA kurz", "AGACCCAAAG", "Was Tolles", "defaultOrganism", "testkey1,testkey2",
                "DNA");

        test2.writeToFile("mygenbankshort2");



        /*
         * test2.EMBLToMyGenBank("EMBLSampleFile");
         *
         * System.out.println(test2);
         *
         * test2.writeToFile("ENBLTest");
         *
         * MyGenBank test1 = new MyGenBank("EMBLSampleFile", "EMBL");
         * test1.writeToFile("MyGenBankFromGenBank"); System.out.println(test1);
         *
         * MyGenBank test3 = new MyGenBank("MyGenBankFromGenBank", "MyGenBank");
         * System.out.println(test3);
         */


        //test2.GenBankToMyGenBank("GenBankSampleFile","TestFile");

    }

}
