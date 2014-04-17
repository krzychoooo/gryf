/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gryf;

import java.awt.Button;
import java.awt.Color;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Monika Waleczek
 */
public class AlarmEdit extends JFrame {
    
    private final JTextField    colorField, descriptionField, 
                                alarmIdField, alarmIdHexField, 
                                armedField, disArmedField,
                                colorArmedField, colordisArmedField;
    private JLabel colorLabel;
    private Connection connDBase;
    SpinnerNumberModel spinnerModel;
    final JButton addButton, colorButton, armedButtom, colorArmedButton, colorDisArmedButton;
    private int maxId;
    private int id;
    private boolean addOperation = true;
    private final JCheckBox hotAlarm;

    public AlarmEdit() {
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
        
        
        alarmIdField = new JTextField(3);
        alarmIdHexField = new JTextField(3);
        descriptionField = new JTextField(30);
        colorField = new JTextField(80);
        hotAlarm = new JCheckBox();
        
        
        add(alarmIdField);
        add(alarmIdHexField);
        add(descriptionField);
        add(colorField);
        add(hotAlarm);
        
        alarmIdField.setBounds(130, 50, 25, 25);
        alarmIdHexField.setBounds(210, 50, 25, 25);
        descriptionField.setBounds(100, 100, 200, 25);
        colorField.setBounds(100, 150, 200, 25);
        hotAlarm.setBounds(100, 200, 200, 25);
        
        alarmIdHexField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                alarmIdField.setText(Integer.toString(Integer.parseInt(alarmIdHexField.getText(), 16)));
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                alarmIdField.setText(Integer.toString(Integer.parseInt(alarmIdHexField.getText(), 16)));
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                alarmIdField.setText(Integer.toString(Integer.parseInt(alarmIdHexField.getText(), 16)));
            }
        });
        
        
        final JLabel alarmIdLabel = new JLabel("Kod zdarzenia");
        add(alarmIdLabel);
        alarmIdLabel.setBounds(5, 50, 100, 25);
        
        final JLabel alarmIdLabeldec = new JLabel("dec");
        add(alarmIdLabeldec);
        alarmIdLabeldec.setBounds(100, 50, 100, 25);
        
        final JLabel alarmIdLabelhex = new JLabel("hex");
        add(alarmIdLabelhex);
        alarmIdLabelhex.setBounds(180, 50, 100, 25);
        
        final JLabel descriptionLabel = new JLabel("Opis");
        add(descriptionLabel);
        descriptionLabel.setBounds(5, 100, 100, 25);
        
        colorLabel = new JLabel("Kolor");
        add(colorLabel);
        colorLabel.setBounds(5, 150, 100, 25);
        
        final JLabel hotLabel = new JLabel("Alarm gorący");
        add(hotLabel);
        hotLabel.setBounds(5, 200, 100, 25);
        
        colorButton = new JButton("Kolor");
        add(colorButton);
        colorButton.setBounds(300, 150, 80, 25);
        colorButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                changeAlarmColor(colorField);
            }
        });
        
        addButton = new JButton("Dodaj alarm");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewAlarm(alarmIdField.getText(), descriptionField.getText(), colorField.getText(), hotAlarm.isSelected());
            }
        });
        add(addButton);
        addButton.setBounds(100, 250, 200, 50);
        
        final JSeparator line1 = new JSeparator(SwingConstants.HORIZONTAL);
        add(line1);
        line1.setBounds(5, 310, 380, 5);
        
        final JLabel armedLabel = new JLabel("Kod uzbrojenia");
        add(armedLabel);
        armedLabel.setBounds(5, 350, 100, 25);
        
        final JLabel disArmedLabel = new JLabel("Kod rozbrojenia");
        add(disArmedLabel);
        disArmedLabel.setBounds(5, 400, 100, 25);
        
        armedField = new JTextField(3);
        add(armedField);
        armedField.setBounds(100, 350, 40, 25);
        
        disArmedField = new JTextField(3);
        add(disArmedField);
        disArmedField.setBounds(100, 400, 40, 25);
        
        JLabel colorArmedLabel = new JLabel("Kolor uzbrojenia");
        add(colorArmedLabel);
        colorArmedLabel.setBounds(140, 350, 100, 25);
        
        JLabel colorDisArmedLabel = new JLabel("Kolor rozbrojenia");
        add(colorDisArmedLabel);
        colorDisArmedLabel.setBounds(140, 400, 100, 25);
        
        colorArmedField = new JTextField();
        add(colorArmedField);
        colorArmedField.setBounds(240, 350, 60, 25);
        
        colordisArmedField = new JTextField();
        add(colordisArmedField);
        colordisArmedField.setBounds(240, 400, 60, 25);
        
        colorArmedButton = new JButton("Kolor");
        add(colorArmedButton);
        colorArmedButton.setBounds(310, 350, 70, 25);
        colorArmedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeAlarmColor(colorArmedField);
            }
        });
        
        colorDisArmedButton = new JButton("Kolor");
        add(colorDisArmedButton);
        colorDisArmedButton.setBounds(310, 400, 70, 25);
        colorDisArmedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeAlarmColor(colordisArmedField);
            }
        });
                
        fillArmedField();
        
        armedButtom = new JButton("Zapisz");
        add(armedButtom);
        armedButtom.setBounds(100, 450, 150, 25);
        armedButtom.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveArmedCode();
            }
        });
        
        this.setSize(400, 550);
        //pack();
        setVisible(true);
    }
    
    private void saveArmedCode(){
        try {
            String query = "UPDATE `gryf`.`armdisarm` SET `code` = '" + Integer.parseInt(armedField.getText()) + "', `color`='" + colorArmedField.getText() + "' WHERE `description`='uzbrojenie' ";
            Statement st = connDBase.createStatement();
            st.execute(query);
            query = "UPDATE `gryf`.`armdisarm` SET `code` = '" + Integer.parseInt(disArmedField.getText()) + "', `color`='" + colordisArmedField.getText() + "' WHERE `description`='rozbrojenie' ";
            st = connDBase.createStatement();
            st.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(AlarmEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void changeAlarmColor(JTextField textField) {
        Color newColor = JColorChooser.showDialog(rootPane, null, Color.yellow);
        textField.setBackground(newColor);
        int colorInt = newColor.getRGB();
        textField.setText("0x" + Integer.toHexString(colorInt).substring(2));
    }
    

 
    private void addNewAlarm(String alarmId, String description, String color, boolean hotAlarm) {
        try {
            String query = null;
            if(addOperation)query = "INSERT INTO `gryf`.`alarm_description` (`id`,`alarm_id`, `description`, `color`, `hot`) VALUES (NULL, '" + alarmId + "', '" + description + "', '" + color + "', '" + (hotAlarm? 1 : 0) + "');";
            else query = "UPDATE `gryf`.`alarm_description` SET `alarm_id` = '" + alarmId +"', `description` = '" + description + "', `color` = '" + color + "', `hot` = '" + (hotAlarm? 1 : 0) + "' WHERE `id` = " + id + ";";
            Statement st = connDBase.createStatement();
            st.execute(query);
            if(addOperation){
                maxId++;
                spinnerModel.setMaximum(maxId);
                spinnerModel.setValue(maxId);
                descriptionField.setText(null);
                colorField.setText(null);
            }
            

        } catch (SQLException ex) {
            Logger.getLogger(GryfDataBaseEdit.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Błąd bazy w addNewAlarm. Sprawdź dane", "Finish", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getMaxID() {
        String row = "1";
        try {
            String query = "SELECT COUNT(*) FROM alarm_description";
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
                String query = "SELECT * FROM alarm_description WHERE id=" + id;
                Statement st = connDBase.createStatement();
                ResultSet rs = st.executeQuery(query);
                rs.next();
                alarmIdField.setText(rs.getString("alarm_id"));
                descriptionField.setText(rs.getString("description"));
                colorField.setText(rs.getString("color"));
                colorField.setBackground(Color.decode(colorField.getText()));
                hotAlarm.setSelected(rs.getBoolean("hot"));

            } catch (SQLException ex) {
                Logger.getLogger(GryfDataBaseEdit.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            addButton.setText("Dodaj");
            addOperation = true;
            descriptionField.setText("");
            colorField.setText("");
        }

    }

    private void fillArmedField() {
        try {
            String query = "SELECT * FROM armdisarm WHERE description='uzbrojenie'";
            Statement st = connDBase.createStatement();
            ResultSet rs = st.executeQuery(query);
            rs.next();
            armedField.setText(rs.getString("code"));
            colorArmedField.setText(rs.getString("color"));
            colorArmedField.setBackground(Color.decode(colorArmedField.getText()));
            query = "SELECT * FROM armdisarm WHERE description='rozbrojenie'";
            st = connDBase.createStatement();
            rs = st.executeQuery(query);
            rs.next();
            disArmedField.setText(rs.getString("code"));
            colordisArmedField.setText(rs.getString("color"));
            colordisArmedField.setBackground(Color.decode(colordisArmedField.getText()));
        } catch (SQLException ex) {
            Logger.getLogger(AlarmEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

            
}
