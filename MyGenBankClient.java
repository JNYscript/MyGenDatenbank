import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

//java -cp . MyGenBankClient
public class MyGenBankClient {

    private final Socket server;
    private final DataInputStream in;
    private final DataOutputStream out;
    private boolean verifiziert = false;
    private final boolean run = true;


    public MyGenBankClient(String ip, int port) throws IOException {


        this.server = new Socket(ip, port);
        this.in = new DataInputStream(server.getInputStream());
        this.out = new DataOutputStream(server.getOutputStream());


    }

    public static void main(String[] args) {


    }


    public Object[][] getLevDist(int id,int distance ){

        try {
            this.out.writeUTF("leven");

            this.out.writeInt(id);
            this.out.writeInt(distance);
            if(!this.in.readBoolean()){
                return null;

            }

            ObjectInputStream i = new ObjectInputStream(this.in);

            Object[][] ausgabe = (Object[][]) i.readObject();



            return ausgabe;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;

    }


    public String translation(String s){

        if(s.equals("")){
            return null;
        }
        try {
            this.out.writeUTF("translation");
            this.out.writeUTF(s);

            return this.in.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;


    }

    public String complement(String s){

        if(s.equals("")){
            return null;
        }

        try {
            this.out.writeUTF("komplementieren");
            this.out.writeUTF(s);


            if(this.in.readBoolean()){

                return this.in.readUTF();

            }else{
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String mutation(String s,Double d){

        if(s.equals("")){
            return null;
        }

        try {
            this.out.writeUTF("mutation");
            this.out.writeUTF(s);
            this.out.writeDouble(d);

            if(this.in.readBoolean()){

                return this.in.readUTF();

            }else{
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String transkription(String s){

        if(s.equals("")){
            return null;
        }

        try {
            this.out.writeUTF("transkription");
            this.out.writeUTF(s);


            if(this.in.readBoolean()){

                return this.in.readUTF();

            }else{
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public String drehen(String sqz){

        if(sqz.equals("")){
            return null;
        }

        try {
            this.out.writeUTF("drehen");
            this.out.writeUTF(sqz);

            if(this.in.readBoolean()){

                return this.in.readUTF();

            }else{
                return null;
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    boolean userEinloggen(String benutzername, String passwort) {

        try {
            out.writeUTF("login");
            if (!in.readBoolean()) {

                return false;

            }

            out.writeUTF(benutzername);

            out.writeUTF(passwort);
            if (in.readBoolean() == true) {
                this.verifiziert = true;

                return true;
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;

    }

    String toMyGenBankFormat(String format, String inFile, String outFile) {

        String data;
        try {
            this.out.writeUTF("toMyGenBank");
            if (!in.readBoolean()) {

                return "Fehler";

            }


            data = this.stringFromFile(inFile);
            this.out.writeUTF(format);
            this.out.writeUTF(data);

            if (this.in.readBoolean()) {


                this.writeToFile(outFile, this.in.readUTF());
                return null;

            } else {


                return this.in.readUTF();
            }

        } catch (IOException e) {
            //
            e.printStackTrace();
        }
        return "Fehler";
    }

    private String chooseFile(String s) {
        String data;
        do {
            System.out.println(s);
            String filePathin = System.console().readLine();
            data = this.stringFromFile(filePathin);

        } while (data.equals(""));
        return data;
    }

    String sendCheckToDatabase(boolean send, String data) {

        try {
            this.out.writeUTF("sendToDatabase");
            if (!in.readBoolean()) {
                System.out.println("fehler");
                return "Fehler mit Verbindung";

            }


            this.out.writeUTF(data);
            this.out.writeBoolean(send);

            if (this.in.readBoolean()) {
                return null;

            } else {


                return this.in.readUTF();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "Es ist ein Fehler aufgetreten";
    }

    private void writeToFile(String filename, String data) {

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

            writer.write(data);

            writer.close();
            System.out.println("Schreiben der Datei erfolgreich");

        } catch (IOException e) {
            System.out.println("SChreiben der Datei nicht erfolgreich");
            e.printStackTrace();

        }

    }

    String stringFromFile(String filename) {
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

    boolean userRegistrieren(String benutzer, String passwort) {

        try {
            out.writeUTF("register");
            if (!in.readBoolean()) {

                return false;

            }

            out.writeUTF(benutzer);

            out.writeUTF(passwort);
            return in.readBoolean();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;

    }

    String[][] showDescriptions() {
        String[][] ausgabe;
        try {
            out.writeUTF("descriptions");
            if (!in.readBoolean()) {
                System.out.println("fehler");
                return null;

            }

            ObjectInputStream oin = new ObjectInputStream(this.in);
            ausgabe = (String[][]) oin.readObject();
            return ausgabe;

        } catch (IOException | ClassNotFoundException e) {

            e.printStackTrace();
        }

        return null;
    }

    char[][] getDotplot(String gene1, String gene2) {

        char[][] ausgabe = null;

        try {
            out.writeUTF("dotplot");
            if (!in.readBoolean()) {
                System.out.println("fehler");
                return null;
            }


            this.out.writeUTF(gene1);
            this.out.writeUTF(gene2);


            ObjectInputStream oin = new ObjectInputStream(this.in);

            ausgabe = (char[][]) oin.readObject();


        } catch (IOException | ClassNotFoundException e) {

            e.printStackTrace();
        }
        return ausgabe;
    }

    String getDescription(String id) {

        try {
            out.writeUTF("getDescription");
            if (!in.readBoolean()) {
                System.out.println("fehler");
                return "";

            }


            this.out.writeUTF(id);

            String data = this.in.readUTF();


            return data;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return "";
    }

    private void descriptionToFile() {

        try {
            out.writeUTF("getDescription");
            if (!in.readBoolean()) {
                System.out.println("fehler");
                return;

            }
            System.out.println("Bitte Ausgabedatei angeben: ");

            String filepath = System.console().readLine();

            System.out.println("Bitte ID oder HEADER angeben:");

            this.out.writeUTF(System.console().readLine());

            String data = this.in.readUTF();

            if (data.equals("")) {

                System.out.println("keine Beschreibung gefunden");

            } else {

                System.out.println("Schreiben erfolgreich");
                this.writeToFile(filepath, data);

            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
