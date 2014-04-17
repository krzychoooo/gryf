/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gryf;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author Monika Waleczek
 */
public class ComPortEdit extends JFrame implements SerialPortEventListener {

    private CommPortIdentifier portIdentifier = null;
    private String[] portList;
    private JComboBox<String> jComboBox1, jComboBox2;
    private final JButton okButton;
    //private final JButton cancelButton;
    private Options options;
    private final String DefaultFolder;
    
    private final Boolean DEBUG=false;

    public ComPortEdit() {
        super();
        //setLayout(new GridLayout(2, 2));
        setLayout(new FlowLayout());

        if(DEBUG)
            DefaultFolder = "C:\\Users\\Monika Waleczek\\Dysk Google\\JAVA\\gryf";
        else
            DefaultFolder = (new JFileChooser().getFileSystemView().getDefaultDirectory().toString()) + "\\gryf";
        System.out.println(DefaultFolder);

        getPorts();

        jComboBox1 = new javax.swing.JComboBox<String>();
        jComboBox2 = new javax.swing.JComboBox<String>();
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<String>(portList));
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{
            "2400", "4800", "7200", "9600", "14400", "19200", "38400",
            "57600", "115200", "128000"}));
        add(new JLabel("SELECT COM"));
        add(jComboBox1);
        add(new JLabel("SELECT BOUDRATE"));
        add(jComboBox2);

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                options = new Options((jComboBox1.getSelectedItem().toString()), Integer.parseInt(jComboBox2.getSelectedItem().toString()), 1);
                File optionsFile = new File(DefaultFolder + "\\options.xml");
                try {
                    try (XMLEncoder xmlencoder = new XMLEncoder(new FileOutputStream(optionsFile))) {
                        xmlencoder.writeObject(options);
                    }
                    JOptionPane.showMessageDialog(null, "CHANGE AFTER RESET PROGRAM", "Finish", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        Runtime.getRuntime().exec("java -jar Gryf.jar");
                        System.exit(0);
                    } catch (IOException ex) {
                        Logger.getLogger(ComPortEdit.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ComPortEdit.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        this.add(okButton);
//        cancelButton = new JButton("Cancel");
//        this.add(cancelButton);

        this.setSize(130, 200);
        //pack();
        setVisible(true);
    }

    //------------------------------------------------------------------------------
    private void getPorts() {
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        String[] tempPortList = new String[20];
        int numports = -1;
        while (portEnum.hasMoreElements()) {
            portIdentifier = (CommPortIdentifier) portEnum.nextElement();
            numports++;
            tempPortList[numports] = portIdentifier.getName();
            System.out.println(numports);
            System.out.println(tempPortList[numports]);
        }
        portList = new String[numports + 1];
        System.arraycopy(tempPortList, 0, portList, 0, numports + 1);
    }

    @Override
    public void serialEvent(SerialPortEvent spe) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
