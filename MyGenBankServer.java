import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyGenBankServer {

    private final int port;
    private final ServerSocket serverSocket;

    public MyGenBankServer(int port) throws IOException {
        super();
        this.port = port;

        this.serverSocket = new ServerSocket(port);
    }

    public static void main(String[] args) {

        try {
            MyGenBankServer testserver = new MyGenBankServer(1234);
            testserver.run();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public void run() {

        System.out.println("Server gestartet auf Port: " + this.port);

        while (true) {
            Socket socket;

            try {
                socket = this.serverSocket.accept();
                System.out.println("Neue Client Verbindung  " + socket);
                MyGenBankThread th = new MyGenBankThread(socket);
                th.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private final class MyGenBankThread extends Thread {

        private final Socket client;
        private final DataInputStream in;
        private final DataOutputStream out;
        private Map<String, String> transkriptionTable;

        /**
         * @param client
         * @throws IOException
         */
        private MyGenBankThread(Socket client) throws IOException {
            super();
            this.client = client;
            this.in = new DataInputStream(client.getInputStream());
            this.out = new DataOutputStream(client.getOutputStream());
            this.transkriptionTable = this.generateTableTranskription();
        }

        private void anfrageBearbeiten() {

            String eingabe;
            while (true) {
                try {
                    System.out.println("Warte auf Eingabe");
                    eingabe = in.readUTF();
                    System.out.println(eingabe);
                    if (eingabe.equals("login")) {
                        out.writeBoolean(true);
                        this.userLogin();
                    } else if (eingabe.equals("register")) {
                        out.writeBoolean(true);
                        this.userRegistrieren();
                    } else if (eingabe.equals("toMyGenBank")) {
                        out.writeBoolean(true);
                        this.returnMyGenBank();
                    } else if (eingabe.equals("sendToDatabase")) {
                        out.writeBoolean(true);
                        this.sendMyGenBankToSQL();
                    } else if (eingabe.equals("descriptions")) {
                        out.writeBoolean(true);
                        this.showDescriptions();
                    } else if (eingabe.equals("getDescription")) {
                        out.writeBoolean(true);
                        this.getDescriptionsString();
                    } else if (eingabe.equals("dotplot")) {
                        out.writeBoolean(true);
                        this.getdotplot();
                    } else if (eingabe.equals("drehen")) {
                        this.drehen();
                    } else if (eingabe.equals("transkription")) {
                        this.transkription();
                    } else if (eingabe.equals("komplementieren")) {
                        this.complement();
                    } else if (eingabe.equals("translation")) {
                        this.translation();
                    } else if (eingabe.equals("mutation")) {
                        this.mutation();
                    } else if (eingabe.equals("leven")) {
                        this.levenshtein();
                    } else {

                        out.writeBoolean(false);

                    }
                } catch (

                        IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }

            }
        }

        public int getLevenshteinDistance(String s, String t) {
            //Übernommen von Quelle: http://web.archive.org/web/20120526085419/http://www.merriampark.com/ldjava.htm

            if (s == null || t == null) {
                throw new IllegalArgumentException("Strings must not be null");
            }

            int n = s.length(); // length of s
            int m = t.length(); // length of t

            if (n == 0) {
                return m;
            } else if (m == 0) {
                return n;
            }

            int p[] = new int[n + 1]; //'previous' cost array, horizontally
            int d[] = new int[n + 1]; // cost array, horizontally
            int _d[]; //placeholder to assist in swapping p and d

            // indexes into strings s and t
            int i; // iterates through s
            int j; // iterates through t

            char t_j; // jth character of t

            int cost; // cost

            for (i = 0; i <= n; i++) {
                p[i] = i;
            }

            for (j = 1; j <= m; j++) {
                t_j = t.charAt(j - 1);
                d[0] = j;

                for (i = 1; i <= n; i++) {
                    cost = s.charAt(i - 1) == t_j ? 0 : 1;
                    // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                    d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
                }

                // copy current distance counts to 'previous row' distance counts
                _d = p;
                p = d;
                d = _d;
            }

            // our last action in the above loop was to switch d and p, so p now
            // actually has the most recent cost counts
            return p[n];
        }

        private void levenshtein() {

            try {
                int IDsearchDNA = this.in.readInt();
                int distance = this.in.readInt();


                ArrayList<Object[]> search = getFromDatabase("select sequenz from genes where id ="+IDsearchDNA, 1);

                if (search.isEmpty()) {

                    this.out.writeBoolean(false);
                    return;
                } else {

                    this.out.writeBoolean(true);
                }


                ArrayList<Object[]> resultArray = new ArrayList<Object[]>();
                ArrayList<Object[]> datenbank = getFromDatabase("select id,header,sequenz from genes where not id =" + IDsearchDNA, 3);

                for (Object[] o : datenbank) {

                    int levdist = getLevenshteinDistance((String) search.get(0)[0], (String) o[2]);
                    if (levdist <= distance) {

                        resultArray.add(new Object[]{o[0], o[1], levdist});

                    }
                }

                Object[][] ausgabe = new Object[resultArray.size()][3];

                for (int i = 0; i < resultArray.size(); i++) {
                    ausgabe[i][0] = Integer.toString((Integer) resultArray.get(i)[0]);
                    ausgabe[i][1] = resultArray.get(i)[1];
                    ausgabe[i][2] = resultArray.get(i)[2];

                }


                ObjectOutputStream o = new ObjectOutputStream(out);

                o.writeObject(ausgabe);

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        private String translationHelp(String s) {

            String ausgabe = ersetzen(s, "(...?)", this.transkriptionTable, true);

            if (ausgabe == null || ausgabe.length() <= 0) {

                return "FEHLER";
            }

            return ausgabe.substring(0, ausgabe.length() - 2);
        }


        private void translation() {

            try {
                String rna = this.in.readUTF();


                this.out.writeUTF(this.translationHelp(rna));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Map<String, String> generateTableTranskription() {

            HashMap<String, String> tabelle = new HashMap<String, String>();


            tabelle.put("UUU", "Phenylalanine-");
            tabelle.put("UUC", "Phenylalanine-");
            tabelle.put("UUA", "Leucine-");
            tabelle.put("UUG", "Leucine-");
            tabelle.put("UCU", "Serine-");
            tabelle.put("UCC", "Serine-");
            tabelle.put("UCA", "Serine-");
            tabelle.put("UCG", "Serine-");
            tabelle.put("UAU", "Tyrosine-");
            tabelle.put("UAC", "Tyrosine-");
            tabelle.put("UAA", "Ochre-");
            tabelle.put("UAG", "Amber-");
            tabelle.put("UGU", "Cysteine-");
            tabelle.put("UGC", "Cysteine-");
            tabelle.put("UGA", "Opal-");
            tabelle.put("UGG", "Tryptophan-");
            tabelle.put("CUU", "Leucine-");
            tabelle.put("CUC", "Leucine-");
            tabelle.put("CUA", "Leucine-");
            tabelle.put("CUG", "Leucine-");
            tabelle.put("CCU", "Proline-");
            tabelle.put("CCC", "Proline-");
            tabelle.put("CCA", "Proline-");
            tabelle.put("CCG", "Proline-");
            tabelle.put("CAU", "Histidine-");
            tabelle.put("CAC", "Histidine-");
            tabelle.put("CAA", "Glutamine-");
            tabelle.put("CAG", "Glutamine-");
            tabelle.put("CGU", "Arginine-");
            tabelle.put("CGC", "Arginine-");
            tabelle.put("CGA", "Arginine-");
            tabelle.put("CGG", "Arginine-");
            tabelle.put("AUU", "Isoleucine-");
            tabelle.put("AUC", "Isoleucine-");
            tabelle.put("AUA", "Isoleucine-");
            tabelle.put("AUG", "Methionine-");
            tabelle.put("ACU", "Threonine-");
            tabelle.put("ACC", "Threonine-");
            tabelle.put("ACA", "Threonine-");
            tabelle.put("ACG", "Threonine-");
            tabelle.put("AAU", "Asparagine-");
            tabelle.put("AAC", "Asparagine-");
            tabelle.put("AAA", "Lysine-");
            tabelle.put("AAG", "Lysine-");
            tabelle.put("AGU", "Serine-");
            tabelle.put("AGC", "Serine-");
            tabelle.put("AGA", "Arginine-");
            tabelle.put("AGG", "Arginine-");
            tabelle.put("GUU", "Valine-");
            tabelle.put("GUC", "Valine-");
            tabelle.put("GUA", "Valine-");
            tabelle.put("GUG", "Valine-");
            tabelle.put("GCU", "Alanine-");
            tabelle.put("GCC", "Alanine-");
            tabelle.put("GCA", "Alanine-");
            tabelle.put("GCG", "Alanine-");
            tabelle.put("GAU", "Aspartic acid-");
            tabelle.put("GAC", "Aspartic acid-");
            tabelle.put("GAA", "Glutamic acid-");
            tabelle.put("GAG", "Glutamic acid-");
            tabelle.put("GGU", "Glycine-");
            tabelle.put("GGC", "Glycine-");
            tabelle.put("GGA", "Glycine-");
            tabelle.put("GGG", "Glycine-");
            return tabelle;

        }

        private String mutate(String s, double rate) {

            StringBuilder ausgabe = new StringBuilder();
            String[] basen = {"A", "T", "C", "G"};

            for (int i = 0; i < s.length(); i++) {

                double rng = Math.random();

                if (rng < rate) {


                    double mrng = Math.random();

                    if (mrng < 0.66) {

                        int rngInt = ThreadLocalRandom.current().nextInt(0, 3 + 1);
                        ausgabe.append(basen[rngInt]);
                        if (mrng < 0.33) {

                            ausgabe.append(s.charAt(i));


                        }


                    }


                } else {

                    ausgabe.append(s.charAt(i));
                }
            }

            return String.valueOf(ausgabe);

        }

        private void mutation() {

            try {
                String dna = this.in.readUTF();
                double mutationRate = this.in.readDouble();

                boolean b = this.checkDNA(dna);
                this.out.writeBoolean(b);

                if (!b) {
                    return;
                }


                this.out.writeUTF(this.mutate(dna, mutationRate));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private String ersetzen(String s, String search, Map<String, String> ersetzen, boolean t) {

            Pattern pattern = Pattern.compile(search);
            Matcher matcher = pattern.matcher(s);
            StringBuilder ausgabe = new StringBuilder();
            int i = 0;
            while (matcher.find()) {
                String replacement = ersetzen.get(matcher.group(1));
                if (replacement != null) {

                    matcher.appendReplacement(ausgabe, "");
                    ausgabe.append(replacement);
                    if (((++i % 5) == 0) && t) {
                        ausgabe.append("\n");
                    }
                }

            }

            matcher.appendTail(ausgabe);
            return ausgabe.toString();
        }


        public void transkription() {

            try {
                String dna = this.in.readUTF();
                System.out.println(dna);
                boolean b = this.checkDNA(dna);
                this.out.writeBoolean(b);

                if (!b) {
                    return;
                }

                Map<String, String> complements = new HashMap<String, String>();

                complements.put("A", "U");
                complements.put("T", "A");
                complements.put("G", "C");
                complements.put("C", "G");


                this.out.writeUTF(this.ersetzen(dna, "(.?)", complements, false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void complement() {

            try {
                String dna = this.in.readUTF();

                boolean b = this.checkDNA(dna);
                this.out.writeBoolean(b);

                if (!b) {
                    return;
                }

                Map<String, String> complements = new HashMap<String, String>();

                complements.put("A", "T");
                complements.put("T", "A");
                complements.put("G", "C");
                complements.put("C", "G");


                this.out.writeUTF(this.ersetzen(dna, "(.?)", complements, false));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        private boolean checkDNA(String sequenz) {

            boolean b = false;

            try {

                Fasta f = new Fasta(">HEAD", sequenz);

                b = true;

            } catch (Fasta.IllegalSequenceException e) {

                b = false;

            }

            return b;

        }

        private void drehen() {

            try {
                String sequenz = this.in.readUTF();

                boolean b = this.checkDNA(sequenz);

                this.out.writeBoolean(b);

                if (!b) {
                    return;
                }

                this.out.writeUTF(this.turnString(sequenz));

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        private String turnString(String s) {
            StringBuilder ausgabe = new StringBuilder();

            for (int i = s.length() - 1; i >= 0; i--) {

                ausgabe.append(s.charAt(i));

            }

            return String.valueOf(ausgabe);

        }

        private String byteToHex(byte[] hasharray) {

            StringBuilder rueckgabe = new StringBuilder(hasharray.length * 2);
            for (byte b : hasharray) {
                rueckgabe.append(String.format("%02x", b));
            }
            return rueckgabe.toString();
        }

        private boolean checkLogin(String username, String passwort) {


            boolean b = false;

            ArrayList<Object[]> sqlData = this.getFromDatabase(
                    String.format("select passwd,salt from passwd where username = '%s'", username), 2);

            if (!sqlData.isEmpty()) {

                String passwdVergleich = (String) sqlData.get(0)[0];
                String passwdHash = this.createPasswd((byte[]) sqlData.get(0)[1], passwort);
                b = passwdVergleich.equals(passwdHash);

            }

            return b;

        }

        private String createPasswd(byte[] salt, String passwd) {

            String passwdHash = "";

            try {
                KeySpec spec = new PBEKeySpec(passwd.toCharArray(), salt, 5000, 512);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] passwdhasharray = factory.generateSecret(spec).getEncoded();
                passwdHash = byteToHex(passwdhasharray);

            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                e.printStackTrace();

            }
            return passwdHash;
        }

        private byte[] createSalt() {

            SecureRandom rng = new SecureRandom();
            byte[] salt = new byte[16];
            rng.nextBytes(salt);
            return salt;
        }

        private ArrayList<Object[]> getFromDatabase(String statement, int colAnzahl) {

            ArrayList<Object[]> sqlErgebnis = new ArrayList<Object[]>();

            try {

                Connection con = DriverManager.getConnection("jdbc:mysql://85.214.81.125:3306/MyGenBank", "MyGenBank",
                        "gU%J#ymQh2^a52Y!MBez%Fc&UVnRh%F%DVRrx*9e");

                Statement sqlEingabe = con.createStatement();

                ResultSet sqlResult = sqlEingabe.executeQuery(statement);

                System.out.println("Lesen erfolgreich");

                while (sqlResult.next()) {

                    Object[] row = new Object[colAnzahl];

                    for (int i = 1; i <= colAnzahl; i++) {

                        row[i - 1] = sqlResult.getObject(i);

                    }
                    sqlErgebnis.add(row);
                }
                con.close();

            } catch (java.sql.SQLException e) {

                System.out.println("Fehler beim Verbinden mit der Datenbank");
                e.printStackTrace();
            }

            return sqlErgebnis;

        }

        private boolean registerDatabase(String username, String passwort) {

            boolean b = false;

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://85.214.81.125:3306/MyGenBank", "MyGenBank",
                        "gU%J#ymQh2^a52Y!MBez%Fc&UVnRh%F%DVRrx*9e");
                PreparedStatement sqlEingabe = con
                        .prepareStatement("insert into passwd(username,passwd,salt) values(?,?,?)");

                byte[] salt = this.createSalt();
                System.out.println(Arrays.toString(salt));
                String passwortHash = this.createPasswd(salt, passwort);

                sqlEingabe.setString(1, username);
                sqlEingabe.setString(2, passwortHash);
                sqlEingabe.setBytes(3, salt);
                sqlEingabe.execute();
                b = true;
                System.out.println("Übertragung erfolgreich");

                con.close();

            } catch (SQLException e) {

                e.printStackTrace();
            }

            return b;

        }

        private void returnMyGenBank() {

            try {
                String type = this.in.readUTF();

                String data = this.in.readUTF();

                MyGenBankObject ob = new MyGenBankObject(data, type);

                this.out.writeBoolean(true);
                this.out.writeUTF(ob.toString());

            } catch (Exception e) {

                try {
                    this.out.writeBoolean(false);
                    this.out.writeUTF(e.toString());
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                return;

            }

        }

        public void run() {

            try {

                this.anfrageBearbeiten();

            } finally {

                if (this.client != null) {

                    try {
                        this.client.close();
                        System.out.println("Verbindung zu Client beendet  " + this.client);
                    } catch (IOException e) {

                        e.printStackTrace();
                    }

                }
            }

        }

        private boolean sendToServer(MyGenBankObject data) {

            boolean b = false;
            Connection con;
            try {
                con = DriverManager.getConnection("jdbc:mysql://85.214.81.125:3306/MyGenBank", "MyGenBank",
                        "gU%J#ymQh2^a52Y!MBez%Fc&UVnRh%F%DVRrx*9e");
                PreparedStatement sqlEingabe = con.prepareStatement(
                        "insert into genes(header,sequenz,definition,organism,keywords,moleculeType,sequenzLenght) values(?,?,?,?,?,?,?)");

                sqlEingabe.setString(1, data.fasta.getHeader());
                sqlEingabe.setString(2, data.fasta.getDna());
                sqlEingabe.setString(3, data.definition);
                sqlEingabe.setString(4, data.organism);
                sqlEingabe.setString(5,
                        Arrays.toString(data.keywords).replace("[", "").replace("]", "").replace(" ", "").trim()
                                + "\n");
                sqlEingabe.setString(6, data.moleculeType);
                sqlEingabe.setInt(7, data.sequenzLenght);

                sqlEingabe.execute();

                b = true;

                con.close();

            } catch (SQLException e) {

                e.printStackTrace();
            }

            return b;
        }

        private void userLogin() {

            try {
                String benutzername = in.readUTF();
                String passwort = in.readUTF();
                out.writeBoolean(this.checkLogin(benutzername, passwort));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        private void sendMyGenBankToSQL() {

            boolean b = false;

            try {

                String data = this.in.readUTF();

                MyGenBankObject obj = new MyGenBankObject(data, "MyGenBank");

                if (!this.in.readBoolean()) {
                    this.out.writeBoolean(true);
                    return;
                }
                b = this.sendToServer(obj);

                this.out.writeBoolean(b);

                if (!b) {

                    this.out.writeUTF("Fehler bei der Kommunikation mit der Datenbank");
                }

            } catch (IOException e) {

                e.printStackTrace();
                try {
                    this.out.writeBoolean(b);
                    this.out.writeUTF(e.toString());
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            } catch (Fasta.IllegalHeaderException | Fasta.IllegalSequenceException e) {

                try {
                    this.in.readBoolean();
                    this.out.writeBoolean(b);
                    this.out.writeUTF(e.toString());
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                return;

            }

        }

        private void userRegistrieren() {

            try {
                String username = in.readUTF();
                String passwort = in.readUTF();
                out.writeBoolean(this.registerDatabase(username, passwort));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        private void showDescriptions() {

            String s = "";

            ArrayList<Object[]> data = this.getFromDatabase("select id,header,definition from genes", 3);

            String[][] ausgabe = new String[data.size()][3];

            for (int i = 0; i < data.size(); i++) {
                ausgabe[i][0] = Integer.toString((Integer) data.get(i)[0]);
                for (int j = 1; j < 3; j++) {

                    ausgabe[i][j] = (String) data.get(i)[j];


                }
            }


            try {
                //this.out.writeInt(data.size());

                ObjectOutputStream o = new ObjectOutputStream(out);

                o.writeObject(ausgabe);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        private MyGenBankServer.MyGenBankThread.MyGenBankObject myGenBankFromDatabase(String bedingung) {

            ArrayList<Object[]> data = this.getFromDatabase(
                    "select header,sequenz,definition,organism,keywords,moleculeType from genes " + bedingung, 6);

            if (data.isEmpty()) {

                return null;

            }

            return new MyGenBankObject(data.get(0)[0].toString(), data.get(0)[1].toString(), data.get(0)[2].toString(),
                    data.get(0)[3].toString(), data.get(0)[4].toString(), data.get(0)[5].toString());

        }

        private void getDescriptionsString() {

            MyGenBankObject data;

            try {

                String eingabe = this.in.readUTF();
                data = formatSQL(eingabe);

                if (data == null) {

                    this.out.writeUTF("");
                } else {
                    // System.out.println(data.toString());
                    this.out.writeUTF(data.toString());
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        private MyGenBankObject formatSQL(String eingabe) {
            MyGenBankObject data;
            if (this.isNumber(eingabe)) {

                data = this.myGenBankFromDatabase(String.format(" where id = %d", Integer.parseInt(eingabe)));

            } else {

                data = this.myGenBankFromDatabase(String.format("where header = '%s'", eingabe));

            }
            return data;
        }

        private boolean isNumber(String s) {

            if (s == null) {

                return false;
            }

            return Pattern.compile("-?\\d+(\\.\\d+)?").matcher(s).matches();

        }

        private void getdotplot() {

            String eingabe;
            MyGenBankObject[] genes = new MyGenBankObject[2];
            try {

                for (int i = 0; i < 2; i++) {

                    eingabe = this.in.readUTF();
                    genes[i] = this.formatSQL(eingabe);


                }

                char[][] dotPlot = genes[0].fasta.printDotPlot(genes[1].fasta);

                ObjectOutputStream o = new ObjectOutputStream(out);

                o.writeObject(dotPlot);


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        private final class MyGenBankObject extends MyGenBank {

            protected MyGenBankObject(String filename, String filetype) throws IllegalArgumentException {
                super(filename, filetype);
                // TODO Auto-generated constructor stub
            }

            public MyGenBankObject(String header, String sequenz, String definition, String organism, String keywords,
                                   String moleculeType) {
                super(header, sequenz, definition, organism, keywords, moleculeType);

            }

            @Override
            public void EMBLToMyGenBank(String fileString)
                    throws Fasta.IllegalSequenceException, Fasta.IllegalHeaderException {

                String header = ">" + this.regexMatcher(fileString, "\nID *(.*?) ");
                String sequenz = this.regexMatcher(fileString, "\nSQ.*?\n(.*?)//").toUpperCase().replaceAll("[^ATCG]",
                        "");
                this.fasta = new Fasta(header, sequenz);
                this.definition = this.removeNonC(this.regexMatcher(fileString, "\nDE(.*?)\n[^DE]")).strip();
                this.sequenzLenght = fasta.getDNAlength();
                this.organism = this.removeNonC(this.regexMatcher(fileString, "\nOS(.*?)\n[OS]")).strip();
                this.moleculeType = this.regexMatcher(fileString, "\nID.*?;(.*?);").strip();
                this.keywords = this.removeNonC(this.regexMatcher(fileString, "\nKW(.*?)\n[^KW]")).strip().split(",");

            }

            @Override
            public void GenBankToMyGenBank(String fileString)
                    throws Fasta.IllegalSequenceException, Fasta.IllegalHeaderException {

                String header = ">" + this.regexMatcher(fileString, "LOCUS *(.*?) ");
                String sequenz = this.regexMatcher(fileString, "ORIGIN(.*?)//").toUpperCase().replaceAll("[^ATCG]", "");
                this.fasta = new Fasta(header, sequenz);
                this.definition = this.removeNonC(this.regexMatcher(fileString, "DEFINITION *(.*?)\n[A-Z]+"));
                this.sequenzLenght = fasta.getDNAlength();
                this.organism = this.removeNonC(this.regexMatcher(fileString, "ORGANISM(.*?)\n[A-Z]+"));
                this.moleculeType = this.regexMatcher(fileString, "bp *(.*?) ");
                this.keywords = this.removeNonC(this.regexMatcher(fileString, "KEYWORDS(.*?)\n[A-Z]+")).split(",");

            }

            @Override
            public void readFromFileMyGenBank(String fileString)
                    throws Fasta.IllegalSequenceException, Fasta.IllegalHeaderException {

                String id = this.regexMatcher(fileString, "id:(.*?)\n");
                String sequenz = this.regexMatcher(fileString, "sequenz:(.*?)//").replace("\n", "");

                this.fasta = new Fasta(id, sequenz);
                this.definition = this.regexMatcher(fileString, "definition:(.*?)\n");
                this.sequenzLenght = fasta.getDNAlength();
                this.organism = this.regexMatcher(fileString, "organism:(.*?)\n");
                this.moleculeType = this.regexMatcher(fileString, "moleculeType:(.*?)\n");
                this.keywords = this.regexMatcher(fileString, "keywords:(.*?)\n").split(",");

            }

        }

    }

}
