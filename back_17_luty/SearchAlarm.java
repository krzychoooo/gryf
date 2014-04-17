
package gryf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

/**
 *
 * @author Monika Waleczek
 */
class SearchAlarm extends JFrame implements ActionListener{
    private Connection connDBase;
    private final JTextField startTimeField, endTimeField;
    private final JComboBox<String> alarmIdComboBox, alarmDescriptionComboBox, abonamentNameComboBox, abonamentAddressComboBox, rs485AddressComboBox;
    private final JLabel alarmIdLabel, alarmDescriptionLabel,
            abonamentNameLabel, abonamentAddressLabel, rs485AddressLabel, 
            startTimeLabel, endTimeLabel;
    private final JButton searchButton;
    private final JButton clearButton;
    private final JButton saveButton;
    private final JTextArea notatnik;
    
    private Statement st;
    //private final Calendar calendar;
    
    

    public SearchAlarm() {
        
        setLayout(null);
        this.setSize(640, 600);
        
        try {
            connDBase = DriverManager.getConnection("jdbc:mysql://localhost/gryf?characterEncoding=utf8", "user_gryf", "gryf");
            st = connDBase.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(GryfDataBaseEdit.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<String> lista = getArray("alarm_id", "alarm", "time");
        alarmIdComboBox = new JComboBox(lista.toArray());
        
        lista = getArray("description", "alarm_description", "id");
        alarmDescriptionComboBox = new JComboBox(lista.toArray());
        
        alarmIdComboBox.addActionListener(this);
        alarmDescriptionComboBox.addActionListener(this);
        
        lista = getArray("name", "abonaments", "name");
        abonamentNameComboBox = new JComboBox(lista.toArray());
        
        lista = getArray("address", "abonaments", "address");
        abonamentAddressComboBox = new JComboBox(lista.toArray());
        
        lista = getArray("rs485address", "abonaments", "rs485address");
        rs485AddressComboBox = new JComboBox(lista.toArray());
        
        
        startTimeField = new JTextField(22);
        endTimeField = new JTextField(22);
        
        notatnik = new JTextArea();
        JScrollPane nsp = new JScrollPane(notatnik);
        
        
        alarmIdLabel = new JLabel("kod zdarzenia");
        alarmDescriptionLabel = new JLabel("opis");
        abonamentNameLabel = new JLabel("user");
        abonamentAddressLabel = new JLabel("adres");
        rs485AddressLabel = new JLabel("rs485adres");
        startTimeLabel = new JLabel("od");
        endTimeLabel = new JLabel("do");
        
        GregorianCalendar calendar = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        endTimeField.setText(dateFormat.format(calendar.getTime()));
        calendar.add(GregorianCalendar.DAY_OF_MONTH, -1);
        startTimeField.setText(dateFormat.format(calendar.getTime()));
        
        searchButton = new JButton("Szukaj");
        clearButton = new JButton("Czyść");
        saveButton = new JButton("Zapisz");
        
        this.add(alarmIdComboBox);
        this.add(alarmDescriptionComboBox);
        this.add(abonamentNameComboBox);
        this.add(abonamentAddressComboBox);
        this.add(rs485AddressComboBox);
        this.add(startTimeField);
        this.add(endTimeField);
        this.add(searchButton);
        this.add(clearButton);
        this.add(saveButton);
        this.add(nsp);
        
        this.add(alarmIdLabel);
        this.add(alarmDescriptionLabel);
        this.add(abonamentNameLabel);
        this.add(abonamentAddressLabel);
        this.add(rs485AddressLabel);
        this.add(startTimeLabel);
        this.add(endTimeLabel);
        
        alarmIdLabel.setBounds(5, 30, 100, 25);
        alarmIdComboBox.setBounds(100, 30, 100, 25);
        alarmDescriptionLabel.setBounds(5, 60, 100, 25);
        alarmDescriptionComboBox.setBounds(100, 60, 100, 25);
        abonamentNameLabel.setBounds(5, 90, 100, 25);
        abonamentNameComboBox.setBounds(100, 90, 100, 25);
        abonamentAddressLabel.setBounds(5, 120, 100, 25);
        abonamentAddressComboBox.setBounds(100, 120, 100, 25);
        rs485AddressLabel.setBounds(5, 150, 100, 25);
        rs485AddressComboBox.setBounds(100, 150, 100, 25);
        startTimeLabel.setBounds(5, 180, 100, 25);
        startTimeField.setBounds(100, 180, 150, 25);
        endTimeLabel.setBounds(255, 180, 100, 25);
        endTimeField.setBounds(300, 180, 150, 25);
        searchButton.setBounds(100, 240, 100, 25);
        clearButton.setBounds(210, 240, 100, 25);
        saveButton.setBounds(320, 240, 100, 25);
        
        nsp.setBounds(5, 300, 600, 250);
        
        searchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                find();
            }
        });
           
        clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                notatnik.setText("");
            }
        });
        
        saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        this.setVisible(true);
    }
    
    
    private void save() {
        final JFileChooser SaveAs = new JFileChooser();
        SaveAs.setApproveButtonText("Save");
        int actionDialog = SaveAs.showOpenDialog(this);
        if (actionDialog != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileName = new File(SaveAs.getSelectedFile()+ ".txt");
        BufferedWriter outFile = null;
        try {
            outFile = new BufferedWriter(new FileWriter(fileName));
            notatnik.write(outFile);   // *** here: ***

        } catch (IOException ex) {
            //ex.printStackTrace();
            Logger.getLogger(SearchAlarm.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        finally {
            if (outFile != null) {
                try {
                    outFile.close();
                } catch (IOException e) {
                    Logger.getLogger(SearchAlarm.class.getName()).log(Level.SEVERE, null, e);
               // one of the few times that I think that it's OK
                    // to leave this blank
                }
            }
        }

    }
    
    
    private void find(){
        
        String alarmId = (String) alarmIdComboBox.getSelectedItem();
        String alarmDescription = (String) alarmDescriptionComboBox.getSelectedItem();
        String abonamentName = (String) abonamentNameComboBox.getSelectedItem();
        String abonamentAddress = (String) abonamentAddressComboBox.getSelectedItem();
        String rs485Address = (String) rs485AddressComboBox.getSelectedItem();
        String startTime = startTimeField.getText();
        String endTime = endTimeField.getText();
        
      
        String query = "SELECT * FROM `abonaments` WHERE `name`LIKE'" + abonamentName + "' AND `address`LIKE'" + abonamentAddress + "' AND `rs485address`LIKE'" + rs485Address + "'";
        try {
            ResultSet rs = st.executeQuery(query);
            String abonamentId;
            String name;
            String address;
            String kodZd;
            String alarmTime;
            String alarmAcceptTime;
            while(rs.next()){
                abonamentId = rs.getString("id");
                name = rs.getString("name");
                address = rs.getString("address");
                
                //notatnik.append("abonament id=" + abonamentId + "\n");
                
                query = "SELECT * FROM `alarm` WHERE `client_id`LIKE '" + abonamentId + "' AND `alarm_id` LIKE '" + alarmId + "' AND `time`>'"+ startTime + "' AND `time`<'" + endTime + "' ORDER BY `time` ASC";
                Statement stat = connDBase.createStatement();
                ResultSet rst = stat.executeQuery(query);
                while(rst.next()){
                    alarmId = rst.getString("alarm_id");
                    kodZd = rst.getString("alarm_id");
                    alarmTime = rst.getString("time");
                    alarmAcceptTime = rst.getString("accept");
                    if(alarmAcceptTime.equals("1970-01-02 00:00:02.0"))alarmAcceptTime = "brak";
                    query = "SELECT `description` FROM `alarm_description` WHERE `alarm_id`=" + kodZd + ";";
                    Statement statement = connDBase.createStatement();
                    ResultSet result = statement.executeQuery(query);
                    result.next();
                    kodZd = result.getString("description");
                
                    notatnik.append(" " + kodZd + " " + name + " " + address + "  CZAS: " +alarmTime + "  AKCEPTACJA: " + alarmAcceptTime + "\n");
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(SearchAlarm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    private List getArray(String columnName, String tableName, String order){
        
        String query = "SELECT `" + columnName + "` FROM `" + tableName + "` group by `" + columnName + "` ORDER BY `" + order + "` ASC";
        try {
            ResultSet rs = st.executeQuery(query);
            ArrayList<String> lista = new ArrayList<String>();
            while(rs.next()){
                lista.add(rs.getString(columnName));
            }
            lista.add(0, "%");
            return lista;
        } catch (SQLException ex) {
            Logger.getLogger(SearchAlarm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == alarmIdComboBox){
            try {
                String selid =(String) alarmIdComboBox.getSelectedItem();
                String query = "SELECT `description` FROM `alarm_description` WHERE `alarm_id`=" + selid + ";";
                ResultSet rs = st.executeQuery(query);
                rs.next();
                alarmDescriptionComboBox.setSelectedItem(rs.getString("description"));
            } catch (SQLException ex) {
                Logger.getLogger(SearchAlarm.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
            if(e.getSource() == alarmDescriptionComboBox){
                try {
                String selid =(String) alarmDescriptionComboBox.getSelectedItem();
                String query = "SELECT `alarm_id` FROM `alarm_description` WHERE `description`='" + selid + "';";
                ResultSet rs = st.executeQuery(query);
                rs.next();
                alarmIdComboBox.setSelectedItem(rs.getString("alarm_id"));
            } catch (SQLException ex) {
                Logger.getLogger(SearchAlarm.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
    }
}
