import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MyGenBankGUI {
    private static JFrame loginFenster;
    private static MyGenBankClient client;
    private JTabbedPane Panel_Conversion;
    private JTree tree1;
    private JPanel MainMenu;
    private JTextField textFieldInputDatei;
    private JButton EMBLToMyGenBankButton;
    private JButton genBankToMyGenbankButton;
    private JTable genTable;
    private JButton getDescriptionButton;
    private JTextPane panelMyGenBank;
    private JTextField textFieldHeader;
    private JTextArea textSequenz;
    private JButton checkButton;
    private JButton sendToDatabaseButton;
    private JTextField textDefinition;
    private JTextField textOrganism;
    private JTextField textMoleculeType;
    private JTextField textKeywords;
    private JButton sendMyGenBankToServerButton;
    private JTable dotTable1;
    private JTable dotTable2;
    private JButton getDotplotButton;
    private JPanel box;
    private JButton button1;
    private JButton button2;
    private JScrollPane scrollDotplot;
    private JTextArea textManipulation;
    private JButton transkriptionButton;
    private JButton umdrehenButton;
    private JButton komplementierenButton;
    private JButton translationButton;
    private JButton clearButton;
    private JButton mutationButton;
    private JSlider sliderMutation;
    private JScrollPane tab;
    private JButton distanceButton;
    private JSlider sliderLev;
    private JTable tableLevinIn;
    private JTable tableLevinOut;
    private javax.swing.JPanel JPanelDot;
    private JScrollPane ScrollPanelDot;
    private JPanel DotPlotLeven;
    private JPanel Distance;
    private JButton ButtonDistanceDotplot;
    private JSpinner spinner1;
    private JButton buttonPlusDistance;
    private JButton buttonMinusDistance;
    private int scale;
    private char[][] dotplot;
    private char[][] dotplotDistance;

    public MyGenBankGUI() {
        EMBLToMyGenBankButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertEMBL("EMBL");
            }
        });
        genBankToMyGenbankButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertEMBL("GenBank");
            }
        });
        getDescriptionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDescription();
            }
        });
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendString(false);
            }
        });
        sendToDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendString(true);
            }
        });

        sendMyGenBankToServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendFile();
            }
        });
        getDotplotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getDotPlot();
            }
        });
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                verkleingroessern(true);
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verkleingroessern(false);
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                textManipulation.setText(null);
                umdrehenButton.setEnabled(true);
                komplementierenButton.setEnabled(true);
                mutationButton.setEnabled(true);
                transkriptionButton.setEnabled(true);
                textManipulation.setEditable(true);
                translationButton.setEnabled(false);
            }
        });
        umdrehenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DnaDrehen();
            }
        });
        komplementierenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                complement();
            }
        });
        transkriptionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transkription();
            }
        });
        mutationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mutation();
            }
        });
        translationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                translation();
            }
        });
        distanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getDistance();
            }
        });
        ButtonDistanceDotplot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getDotPlotDistance();
            }
        });
        buttonPlusDistance.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verkleingroessernDistance(true);
            }
        });
        buttonMinusDistance.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verkleingroessernDistance(false);
            }
        });
    }

    private static boolean loginFenster() {

        loginFenster = new JFrame("MyGenBank");
        try {
            loginFenster.setContentPane(new MyGenBankGUILogin().LoginMenu);
            loginFenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFenster.pack();
            loginFenster.setVisible(true);
            client = new MyGenBankClient("localhost", 1234);
            MyGenBankGUILogin.setClient(client);
            MyGenBankGUILogin.setRun(MyGenBankGUI::guiAnzeigen);
        } catch (IOException e) {

            JOptionPane.showMessageDialog(null, "keine Verbindung zum Server");
            loginFenster.setVisible(false);
            loginFenster.dispose();
            return false;


        }
        return true;
    }
    private void mutation(){

        String s = this.client.mutation(this.textManipulation.getText(),(double)this.sliderMutation.getValue()/100);

        if(s == null){

            JOptionPane.showMessageDialog(null, "ungültige Eingabe");
            return;
        }

        this.textManipulation.setText(s);


    }

    private void translation(){


        String s = this.client.translation(this.textManipulation.getText());

        if(s == null){

            JOptionPane.showMessageDialog(null, "ungültige Eingabe");
            return;
        }

        this.textManipulation.setText(s);
        this.translationButton.setEnabled(false);







    }

    private void complement(){

        String s = this.client.complement(this.textManipulation.getText().replaceAll(" ",""));

        if(s == null){

            JOptionPane.showMessageDialog(null, "ungültige Eingabe");
            return;
        }

        this.textManipulation.setText(s);


    }

    private void transkription(){

        String s = this.client.transkription(this.textManipulation.getText());

        if(s == null){

            JOptionPane.showMessageDialog(null, "ungültige Eingabe");
            return;
        }

        this.textManipulation.setText(s);

        this.umdrehenButton.setEnabled(false);
        this.komplementierenButton.setEnabled(false);
        this.mutationButton.setEnabled(false);
        this.transkriptionButton.setEnabled(false);
        this.textManipulation.setEditable(false);
        this.translationButton.setEnabled(true);



    }

    private void DnaDrehen(){

        String s = this.client.drehen(this.textManipulation.getText());

        if(s == null){

            JOptionPane.showMessageDialog(null, "ungültige Eingabe");
            return;
        }

        this.textManipulation.setText(s);

    }


    private void getDistance() {
        int selRow1 = this.tableLevinIn.getSelectedRow();


        if (selRow1 == -1) {
            JOptionPane.showMessageDialog(null, "Kein Eintrag gewählt");
            return;
        }

        int id1 = Integer.parseInt((String)this.dotTable1.getValueAt(selRow1, 0));

        Object[][] table = this.client.getLevDist(id1,(int)this.spinner1.getValue());

        DefaultTableModel model = new DefaultTableModel(table, new String[]{"ID", "Header","Distance"}
        ){
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return String.class;
                    case 2:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };

        this.tableLevinOut.setModel(model);







    }


    private static void guiAnzeigen() {
        loginFenster.setVisible(false);
        loginFenster.dispose();
        JFrame gui = new JFrame("MyGenBankGUI");
        gui.setContentPane(new MyGenBankGUI().MainMenu);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.pack();
        gui.setVisible(true);

    }

    public static void main(String[] args) {

        if (!loginFenster()) {

            return;
        }


    }

    private static void updateTree(JTree tree1, String name) {
        DefaultTreeModel m = (DefaultTreeModel) tree1.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) m.getRoot();
        root.add(new DefaultMutableTreeNode(name));
        m.reload(root);


    }

    private void getDotPlot() {


        int selRow1 = this.dotTable1.getSelectedRow();
        int selRow2 = this.dotTable2.getSelectedRow();

        if (selRow1 == -1 || selRow2 == -1) {
            JOptionPane.showMessageDialog(null, "Kein Eintrag gewählt");
            return;
        }

        String id1 = (String) this.dotTable1.getValueAt(selRow1, 0);
        String id2 = (String) this.dotTable2.getValueAt(selRow2, 0);

        this.dotplot = client.getDotplot(id1, id2);

        this.scale = 20;

        repaintDotplot();


    }

    private void getDotPlotDistance() {


        int selRow1 = this.tableLevinIn.getSelectedRow();
        int selRow2 = this.tableLevinOut.getSelectedRow();

        if (selRow1 == -1 || selRow2 == -1) {
            JOptionPane.showMessageDialog(null, "Kein Eintrag gewählt");
            return;
        }

        String id1 = (String) this.tableLevinIn.getValueAt(selRow1, 0);
        String id2 = (String) this.tableLevinOut.getValueAt(selRow2, 0);

        this.dotplotDistance = client.getDotplot(id1, id2);

        this.scale = 20;

        repaintDotplotDistance();


    }

    private void verkleingroessern(boolean b) {

        if (b) {
            this.scale += 3;

            repaintDotplot();
        } else {

            this.scale = this.scale < 5 ? 5 : this.scale - 3;
            repaintDotplot();

        }


    }

    private void verkleingroessernDistance(boolean b) {

        if (b) {
            this.scale += 3;

            repaintDotplotDistance();
        } else {

            this.scale = this.scale < 5 ? 5 : this.scale - 3;
            repaintDotplotDistance();

        }


    }

    private void repaintDotplot() {
        this.box.removeAll();


        this.box.add(new Dotplot(this.dotplot, this.scale));

        this.box.revalidate();
        this.box.repaint();
        this.scrollDotplot.revalidate();
        this.scrollDotplot.repaint();
    }

    private void repaintDotplotDistance() {
        this.DotPlotLeven.removeAll();


        this.DotPlotLeven.add(new Dotplot(this.dotplotDistance, this.scale));

        this.DotPlotLeven.revalidate();
        this.DotPlotLeven.repaint();
        this.ScrollPanelDot.revalidate();
        this.ScrollPanelDot.repaint();
    }



    private void updateTable() {

        DefaultTableModel model = new DefaultTableModel(client.showDescriptions(), new String[]{"ID", "Header", "Description"}
        );
        genTable.setModel(model);
        genTable.getColumnModel().getColumn(0).setMaxWidth(40);
        genTable.getColumnModel().getColumn(1).setMinWidth(70);
        genTable.getColumnModel().getColumn(2).setMinWidth(700);


    }

    private void sendFile() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree1.getLastSelectedPathComponent();


        String inputDatei;

        if (node == null) {

            JOptionPane.showMessageDialog(null, "keine datei ausgewählt");
            return;
        } else {

            inputDatei = (String) node.getUserObject();


        }
        String data = client.stringFromFile(inputDatei);

        checkSendToDatabase(true, data);

    }

    private void sendString(boolean send) {

        String data = String.format("id:%s\n" +
                "definition:%s\n" +
                "sequenzLength:\n" +
                "organism:%s\n" +
                "moleculeType:%s\n" +
                "keywords:%s\n" +
                "sequenz:\n" +
                "%s\n" +
                "//", textFieldHeader.getText(), textDefinition.getText(), textOrganism.getText(), textMoleculeType.getText(), textKeywords.getText(), textSequenz.getText());

        checkSendToDatabase(send, data);
    }

    private void checkSendToDatabase(boolean send, String data) {


        String returnMessage = client.sendCheckToDatabase(send, data);
        if (returnMessage == null) {

            if (send) {
                JOptionPane.showMessageDialog(null, "Senden erfolgreich");
                updateTable();
                updateDotPlotTable();
            } else {
                JOptionPane.showMessageDialog(null, "Überprüfung erfolgreich");
            }
        } else {


            JOptionPane.showMessageDialog(null, "Fehler\n" + returnMessage);


        }


    }

    private void showDescription() {

        int selRow = genTable.getSelectedRow();

        if (selRow == -1) {
            JOptionPane.showMessageDialog(null, "Kein Eintrag gewählt");
            return;
        }

        panelMyGenBank.setText(client.getDescription((String) genTable.getValueAt(selRow, 0)));
        panelMyGenBank.setCaretPosition(0);


    }

    private void createUIComponents() {

        createFileTree("root");
        createTable();
        createTableDotplot();



    }

    private void createFileTree(String root) {


        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root);


        File[] dir = new File("resources").listFiles();

        for (File f : dir) {

            if (f.isFile()) {

                rootNode.add(new DefaultMutableTreeNode(f.getName()));

            }

        }
        tree1 = new JTree(rootNode);


    }

    private void convertEMBL(String format) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree1.getLastSelectedPathComponent();


        String inputDatei;

        if (node == null) {

            JOptionPane.showMessageDialog(null, "keine datei ausgewählt");
            return;
        } else {

            inputDatei = (String) node.getUserObject();


        }

        String ergebnis = client.toMyGenBankFormat(format, inputDatei, textFieldInputDatei.getText().equals("") ? "default" : textFieldInputDatei.getText());

        if (ergebnis == null) {

            JOptionPane.showMessageDialog(null, "Umwandlung erfolgreich");
            updateTree(tree1, textFieldInputDatei.getText().equals("") ? "default" : textFieldInputDatei.getText());
        } else {
            JOptionPane.showMessageDialog(null, "Fehler bei der Umwandlung:\n" + ergebnis);
        }


    }

    private void createTableDotplot() {

        String[][] data = client.showDescriptions();

        String[] columnNames = {"ID", "Header"};
        String[] columnNamesOut = {"ID", "Header","Distance"};
        this.dotTable2 = new JTable(data, columnNames);
        this.dotTable1 = new JTable(data, columnNames);
        this.tableLevinIn = new JTable(data,columnNames);
        //this.tableLevinOut = new JTable(new String[][]{ {"","",""}},columnNamesOut);
        this.tableLevinOut = new JTable(data,columnNamesOut);
    }



    private void updateDotPlotTable() {

        DefaultTableModel model = new DefaultTableModel(client.showDescriptions(), new String[]{"ID", "Header"}
        );
        this.dotTable1.setModel(model);
        this.dotTable2.setModel(model);
        this.tableLevinIn.setModel(model);

    }


    private void createTable() {

        String[][] data = client.showDescriptions();

        String[] columnNames = {"ID", "Header", "Description"};



        genTable = new JTable(data, columnNames);
        genTable.getColumnModel().getColumn(0).setMaxWidth(40);
        genTable.getColumnModel().getColumn(1).setMinWidth(70);
        genTable.getColumnModel().getColumn(2).setMinWidth(700);




    }


    class Dotplot extends JPanel {


        private final char[][] dotplot;
        int scale = 10;

        public Dotplot(char[][] dotplot, int scale) {

            this.scale = scale;
            this.dotplot = dotplot;
            this.setBackground(new Color(219, 216, 156));


            this.setPreferredSize(new Dimension(scale * this.dotplot[0].length, scale * this.dotplot.length));
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);
            drawMatches(g);
            drawDNA(g);
            drawGitter(g);

        }


        private void drawDNA(Graphics g) {

            g.setFont(new Font("TimesRoman", Font.PLAIN, (int) (scale * 0.75)));
            g.setColor(new Color(26, 48, 65));
            for (int i = 1; i < dotplot[0].length; i++) {

                g.drawString(String.valueOf(dotplot[0][i]), (int) (this.scale * i + scale * 0.25), (int) (this.scale * 0.75));

            }
            g.setColor(new Color(88, 44, 33));
            for (int j = 1; j < dotplot.length; j++) {

                g.drawString(String.valueOf(dotplot[j][0]), (int) (this.scale * 0.25), (int) (this.scale * j + scale * 0.75));

            }


        }

        private void drawMatches(Graphics g) {

            g.setColor(new Color(53, 65, 11));

            for (int i = 1; i < this.dotplot.length; i++) {

                for (int j = 1; j < this.dotplot[0].length; j++) {

                    if (this.dotplot[i][j] == '*') {

                        g.fillRect(j * scale, i * scale, scale, scale);

                    }

                }


            }


        }

        private void drawGitter(Graphics g) {

            int lenx = this.dotplot[0].length;
            int leny = this.dotplot.length;
            g.setColor(Color.BLACK);

            for (int i = 0; i < lenx + 1; i++) {

                g.drawLine(i * 1 * scale, 0, i * 1 * scale, scale * leny);


            }
            for (int j = 0; j < leny + 1; j++) {

                g.drawLine(0, j * scale, scale * lenx, j * scale);


            }

        }
    }

}
