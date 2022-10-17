import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MyGenBankGUILogin {

    private static Runnable run;
    private static MyGenBankClient client;
    private JButton buttonRegister;
    public JPanel LoginMenu;
    private JButton ButtonLogin;
    private JTextField textFieldUsername;
    private JPasswordField passwordField;

    public MyGenBankGUILogin() {
        ButtonLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                signIn();
            }
        });
        buttonRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
    }

    private void signIn(){



        boolean b = client.userEinloggen(textFieldUsername.getText(),passwordField.getText());

        if(b){

            JOptionPane.showMessageDialog(null, "Login erfolgreich");
            run.run();
        }else{

            JOptionPane.showMessageDialog(null, "Login nicht erfolgreich");

        }

    }

    private void register(){

        if(client.userRegistrieren( textFieldUsername.getText(),passwordField.getText())){

            JOptionPane.showMessageDialog(null, "Registrierung erfolgreich");

        }else{

            JOptionPane.showMessageDialog(null, "Registrierung nicht erfolgreich");
        }


    }

    public static void setClient(MyGenBankClient client) {
        MyGenBankGUILogin.client = client;
    }

    public static void setRun(Runnable run) {
        MyGenBankGUILogin.run = run;
    }

    public static void main(String[] args){

        try {
            JFrame test = new JFrame("MyGenBank");
            test.setContentPane(new MyGenBankGUILogin().LoginMenu);
            test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            test.pack();
            test.setVisible(true);
            MyGenBankGUILogin.client = new MyGenBankClient("localhost",1234);
        }catch (IOException e){

            JOptionPane.showMessageDialog(null, "keine Verbindung zum Server");
            return;



        }





    }


}
