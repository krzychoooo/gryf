/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gryf;

import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Monika Waleczek
 */
public class GryfDataBaseEdit extends JFrame {
    
    private final TextField addressRS485Field, addressField, nameField, telefonField, passwordField;
    private Connection connDBase;
    SpinnerNumberModel spinnerModel;
    final JButton addButton;
    private int maxId;
    private int id;
    private boolean addOperation = true;

    public GryfDataBaseEdit() {
        try {
            connDBase = DriverManager.getConnection("jdbc:mysql://localhost/gryf?characterEncoding=utf8", "user_gryf", "gryf");
        } catch (SQLException ex) {
            Logger.getLogger(GryfDataBaseEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        setLayout(null);
        
        final JLabel idLabel = new JLabel("Id");
        add(idLabel);
        idLabel.setBounds(5, 5, 100, 25);
       
        maxId = getMaxID();
        
        spinnerModel = new SpinnerNumberModel(maxId, 1, maxId, 1);
        JSpinner spiner = new JSpinner(spinnerModel);
        add(spiner);
        spiner.setBounds(100, 5, 50, 25);
        spiner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                changeSpiner(e);
            }

        });
        
        nameField = new TextField(30);
        addressField = new TextField(80);
        addressRS485Field = new TextField(30);
        telefonField = new TextField(12);
        passwordField = new TextField(15);
        
        
        add(nameField);
        add(addressField);
        add(addressRS485Field);
        add(telefonField);
        add(passwordField);
        
        nameField.setBounds(100, 50, 200, 25);
        addressField.setBounds(100, 100, 200, 25);
        addressRS485Field.setBounds(100, 150, 200, 25);
        telefonField.setBounds(100, 200, 200, 25);
        passwordField.setBounds(100, 250, 200, 25);
        
        final JLabel nameLabel = new JLabel("Name");
        add(nameLabel);
        nameLabel.setBounds(5, 50, 100, 25);
        
        final JLabel addressLabel = new JLabel("Adres");
        add(addressLabel);
        addressLabel.setBounds(5, 100, 100, 25);
        
        final JLabel address485Label = new JLabel("RS485 adres");
        add(address485Label);
        address485Label.setBounds(5, 150, 100, 25);
        
        final JLabel telefonLabel = new JLabel("Telefon");
        add(telefonLabel);
        telefonLabel.setBounds(5, 200, 100, 25);
        
        final JLabel passwordLabel = new JLabel("Hasło");
        add(passwordLabel);
        passwordLabel.setBounds(5, 250, 100, 25);
        
        addButton = new JButton("Dodaj abonamenta");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewAbonament(nameField.getText(), addressField.getText(), addressRS485Field.getText(), telefonField.getText(), passwordField.getText());
            }
        });
        add(addButton);
        addButton.setBounds(100, 300, 200, 50);
        
        this.setSize(400, 400);
        if(maxId<250)fillSample();
        
        setVisible(true);
    }

    private void fillSample() {
        File file = new File("us-500.csv");
        Scanner in;
        try {
            String name, adres, telefon, haslo;
            String zdanie;
            String[] rekord;
            in = new Scanner(file);
            int i;
            for (i = maxId; i != 250; i++) {
                zdanie = in.nextLine();
                rekord = zdanie.split("\",\"");
                name = rekord[1];
                adres = rekord[3];
                telefon = rekord[7];
                haslo = rekord[8];
                addNewAbonament(name, adres, Integer.toString(i), telefon, haslo);
            }
            maxId=i;
            JOptionPane.showMessageDialog(null, "Dodano przykładowych abonamentów", "Finish", JOptionPane.INFORMATION_MESSAGE);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GryfDataBaseEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
 
    private void addNewAbonament(String name, String address, String address485, String telefon, String password) {
        try {
            String query;
            if(addOperation)query = "INSERT INTO `gryf`.`abonaments` (`id`, `name`, `address`, `rs485address`, `telefon`, `password`) VALUES (NULL, '" + name + "', '" + address + "', '" + address485 + "', '" + telefon + "', '" + password + "');";
            else query = "UPDATE `gryf`.`abonaments` SET `name` = '" + name +"', `address` = '" + address + "', `rs485address` = '" + address485 + "', `telefon` = '" + telefon +  "', `password` = '" + password + "' WHERE `abonaments`.`id` = " + id + ";";
            Statement st = connDBase.createStatement();
            //st.execute("SET NAMES 'UTF8'");
            st.execute(query);
            if (addOperation) {
                maxId++;
                spinnerModel.setMaximum(maxId);
                spinnerModel.setValue(maxId);
                nameField.setText(null);
                addressField.setText(null);
                addressRS485Field.setText(null);
                telefonField.setText(null);
                passwordField.setText(null);
            }
        } catch (SQLException ex) {
            Logger.getLogger(GryfDataBaseEdit.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Błąd bazy w addNewAbonament. Sprawdź dane", "Finish", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getMaxID() {
        String row = "1";
        try {
            String query = "SELECT COUNT(*) FROM abonaments";
            Statement st = connDBase.createStatement();
            ResultSet rs = st.executeQuery(query);
            rs.next();
            row = rs.getString("COUNT(*)");
            
        } catch (SQLException ex) {
            Logger.getLogger(GryfDataBaseEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (Integer.parseInt(row)+1);
    }
    
    
    private void changeSpiner(ChangeEvent e) {
        id = (int) spinnerModel.getValue();
        if (id < maxId) {
            addButton.setText("Uaktualnij");
            addOperation = false;
            try {
                String query = "SELECT * FROM abonaments WHERE id=" + id;
                Statement st = connDBase.createStatement();
                ResultSet rs = st.executeQuery(query);
                rs.next();
                nameField.setText(rs.getString("name"));
                addressField.setText(rs.getString("address"));
                addressRS485Field.setText(rs.getString("rs485address"));
                telefonField.setText(rs.getString("telefon"));
                passwordField.setText(rs.getString("password"));

            } catch (SQLException ex) {
                Logger.getLogger(GryfDataBaseEdit.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            addButton.setText("Dodaj");
            addOperation = true;
            nameField.setText("");
            addressField.setText("");
            addressRS485Field.setText("");
            telefonField.setText("");
            passwordField.setText("");
        }

    }

            
}
