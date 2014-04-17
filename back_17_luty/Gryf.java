/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gryf;

import aePlayWave.AePlayWave;
import blinklabel.BlinkLabel;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.ArrayList;
import gryf_abonament.Gryf_abonament;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Random;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import klimas.FileLoader;
import klimas.ManifestLoader;
import klimas.NewSoftwareInfo;

public class Gryf extends JFrame implements  SerialPortEventListener{
    
    private final boolean DEBUG = false;
    private final ArrayList<Gryf_abonament> abonaments;
    private final int maxAbonament=255;
    Connection connDBase;
    DefaultTableModel dm;
    JTable table;
    
    private final JMenuBar menuBar;
    private final JMenu toolsMenu, helpMenu;
    private final JMenuItem abonamentMenuItem, alarmMenuItem, enableMenuItem;
    private final JMenuItem comMenuItem, connectItem, aboutItem;

    JCheckBox speedCheckBox;
    boolean speedSimulate;
    
    BlinkLabel blinkLabel;
    //rs232
    private CommPortIdentifier portIdentifier = null;
    private SerialPort serialPort;
//    private int baudRate;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean stanPortu = false;
    int rx_len = 1024;
    char rx_buffer[] = new char[rx_len];
    int rx_buffer_wr_index, rx_buffer_rx_index,rx_counter;
    private boolean endFrame;
    private Frame frame;
    
    private Options options;
    private String DefaultFolder;
    private int armedCode, disArmedCode;

    public Gryf() {
        
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
        setDatabase();
        
        if(DEBUG)
            DefaultFolder = "C:\\Users\\Monika Waleczek\\Dysk Google\\JAVA\\gryf";
        else{
            DefaultFolder = (new JFileChooser().getFileSystemView().getDefaultDirectory().toString()) + "\\gryf";
            System.out.println("DefaultFolder=" + DefaultFolder);
        }
            
        
        
        
        getOptions();
        
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        toolsMenu = new JMenu("Narzędzia");
        menuBar.add(toolsMenu);
        
        abonamentMenuItem = new JMenuItem("Abonament");
        toolsMenu.add(abonamentMenuItem);
        abonamentMenuItem.setEnabled(false);
        abonamentMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new GryfDataBaseEdit();
            }
        });
        
        alarmMenuItem = new JMenuItem("Alarmy");
        toolsMenu.add(alarmMenuItem);
        alarmMenuItem.setEnabled(false);
        alarmMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new AlarmEdit();
            }
        });
        
        comMenuItem = new JMenuItem("Porty");
        toolsMenu.add(comMenuItem);
        comMenuItem.setEnabled(false);
        comMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ComPortEdit();
            }
        });
        
        connectItem = new JMenuItem("CONNECT");
        toolsMenu.add(connectItem);
        connectItem.setEnabled(false);
        connectItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                comInit();
            }
        });
        
        enableMenuItem = new JMenuItem("Odblokuj menu");
        toolsMenu.add(enableMenuItem);
        enableMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                enableMenu();
            }
        });
        
        helpMenu = new JMenu("HELP MENU");
        menuBar.add(helpMenu);
        aboutItem = new JMenuItem("ABOUT");
        helpMenu.add(aboutItem);
        
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSoftwareInfo(true);
            }
            
        });
        
        
        abonaments = new ArrayList<>();
        
        try {
            connDBase = DriverManager.getConnection("jdbc:mysql://localhost/gryf?characterEncoding=utf8", "user_gryf", "gryf");
            String query = "SELECT * FROM abonaments";
            Statement st = connDBase.createStatement();
            ResultSet rs = st.executeQuery(query);
            
            for (int i = 0; i != maxAbonament; i++) {
                abonaments.add(new Gryf_abonament(i));
                abonaments.get(i).setBounds((i % 17) * 55, (i / 17) * 45, 50, 40);

                if(rs.next()){
                    abonaments.get(i).setAddress(rs.getString("address"));
                    abonaments.get(i).setRs485Address(rs.getInt("rs485address"));
                    abonaments.get(i).setMyName(rs.getString("name"));
                    abonaments.get(i).setTelefon(rs.getString("telefon"));
                    abonaments.get(i).setPassword(rs.getString("password"));
                }else{
                    abonaments.get(i).setAddress("brak danych");
                    abonaments.get(i).setRs485Address(0);
                    abonaments.get(i).setMyName("brak danych");
                    abonaments.get(i).setTelefon("brak danych");
                    abonaments.get(i).setPassword("brak danych");
//                    break;
                }
                this.add(abonaments.get(i));
            }
            connDBase.close();
        } catch (SQLException e) {
            System.err.println(e);
        }
        
        dm = new DefaultTableModel();
        String[] columnNames = {"ID", "User ID", "Name", "Adres", "Czas", "Alarm", "Stan"};
        dm.setColumnIdentifiers(columnNames);
        table = new JTable(dm);
        table.setDefaultEditor(table.getColumnClass(1), null);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();
                    eventHandle(row);
            }
        });
        
        blinkLabel = new BlinkLabel("NIE OBSŁUŻONE ALARMY !");
        add(blinkLabel);
        blinkLabel.setBounds(1110-165, 0, 400, 100);
        blinkLabel.setForeground(Color.red);
        blinkLabel.setFont(new Font(blinkLabel.getFont().getName(), Font.PLAIN, 26));
        blinkLabel.setBlinking(false);

   
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(1110-165, 100, 365, 500);
        add(scrollPane);

        JButton search = new JButton("Szukaj alarmu");
        this.add(search);
        search.setBounds(1110-165, 620, 150, 30);
        search.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new SearchAlarm();
            }
        });
        
        speedCheckBox = new JCheckBox();
        this.add(speedCheckBox);
        speedCheckBox.setBounds(1120, 620, 20, 20);
        speedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(speedCheckBox.isSelected()) speedSimulate = true; else speedSimulate = false;
            }
        });
        
        JLabel speedlabel = new JLabel("szybka symulacja");
        this.add(speedlabel);
        speedlabel.setBounds(1150, 620, 150, 20);
        
        pack();
        setVisible(true);
                try {
//            connectCom(options.getComNumber());
//            connectItem.setText(bundle.getString("DISCONNECT") + " " + options.getComNumber());
            comInit();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "CAN NOT OPEN PORT" + " " + options.getComNumber(), "Finish", JOptionPane.INFORMATION_MESSAGE);
            connectItem.setText("CONNECT" + " " + options.getComNumber());
        }
    }

    
/************************* END CONSTRUCTOR  **********************************/    
    
    
   void insertEvent(int rs485address, int alarm_id){
       
       //TODO test parametrów
        try {
            connDBase = DriverManager.getConnection("jdbc:mysql://localhost/gryf?characterEncoding=utf8", "user_gryf", "gryf");
            String query = "SELECT * FROM `abonaments` WHERE `rs485address`=" + rs485address;
            Statement st = connDBase.createStatement();
            ResultSet rs = st.executeQuery(query);
            boolean hot = false;
            if(rs.next()){
                String address = rs.getString("address");
                String name = rs.getString("name");
                String client_id = rs.getString("id");
                
                query = "SELECT * FROM `alarm_description` WHERE `alarm_id`=" + alarm_id;
                st = connDBase.createStatement();
                rs = st.executeQuery(query);
                String alarmColor = "0xffffff";
                String alarmDescription = null;
                
                if(rs.next()){
                    alarmColor = rs.getString("color");
                    alarmDescription = rs.getString("description");
                    hot = rs.getBoolean("hot");
                }else{
                    JOptionPane.showMessageDialog(null, "nie znaleziono rekordu w alarm_description", "błąd w insertAlarm", JOptionPane.ERROR_MESSAGE);
                }

                query = "INSERT INTO `alarm` (`id`, `client_id`, `time`, `alarm_id`) VALUES (NULL, '" + client_id + "', CURRENT_TIMESTAMP, '" + alarm_id +"');";
                //st = connDBase.createStatement();
                st.execute(query);

                if((alarm_id != armedCode) && (alarm_id != disArmedCode)){
                    abonaments.get(rs485address-1).setLabel(Color.decode(alarmColor), Integer.toString(alarm_id));
                    blink(abonaments.get(rs485address-1), Color.red);
                }
                abonaments.get(rs485address-1).setStatus(alarm_id);
                if(alarm_id == armedCode){
                    abonaments.get(rs485address-1).setPanel(Color.RED);
                    blink(abonaments.get(rs485address-1), Color.yellow);
                }
                if(alarm_id == disArmedCode){
                    abonaments.get(rs485address-1).setPanel(Color.GREEN);
                    blink(abonaments.get(rs485address-1), Color.yellow);
                }
                
                
                query = "SELECT * FROM `alarm` ORDER BY id DESC LIMIT 1";
                st = connDBase.createStatement();
                rs = st.executeQuery(query);
                String alarmTimeStamp = null;
                String alarmId = null;
                String clientId = null;
                String eventCode = null;

                if(rs.next()){
//                    System.out.println("rs-" + rs.toString());
                    alarmTimeStamp = rs.getString("time");
                    alarmId = rs.getString("id");
                    clientId = rs.getString("client_id");
                    eventCode = rs.getString("alarm_id");
                }
                Boolean accept = false;       
                Object[] data = {alarmId, clientId, name, address,alarmTimeStamp, alarmDescription, new Boolean(false)};
                if(hot){
                    dm.insertRow(0, data);
                    AePlayWave aw = new AePlayWave( "emergency008.wav" );
                    aw.start();
                    blinkLabel.setBlinking(true);
                } else{
                    AePlayWave aw = new AePlayWave( "plum.wav" );
                    aw.start();
                }
                
            }else{
                JOptionPane.showMessageDialog(null, "nie znaleziono rekordu z adresem 485=", "błąd w insertAlarm", JOptionPane.INFORMATION_MESSAGE);
            }
            connDBase.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(Gryf.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex, "błąd w insertAlarm", JOptionPane.ERROR);

        }
   }
    
   
    private void comInit() {
        if(portIdentifier == null)try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(options.getComNumber());
        } catch (NoSuchPortException ex) {
            Logger.getLogger(Gryf.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (portIdentifier.isCurrentlyOwned()) {
        connectItem.setText("CONNECT" + " " + options.getComNumber());
        if (inputStream != null) {
        try {
            inputStream.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            }
        }
        if (outputStream != null) {
        try {
            outputStream.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            }
        }
        serialPort.removeEventListener();//usunięcie listenera do obslugi zdarzeń na RS232
        if (serialPort != null) {
            serialPort.close();
        }

        stanPortu = false;
    } else { //-------------------------------------------------------------
        connectItem.setText("DISCONNECT" + " " + options.getComNumber());
        
        try {
            connectCom(options.getComNumber());

        }
        catch ( Exception ex ) {
            ex.printStackTrace();
        }
        try {
            serialPort.addEventListener(this); //inicjalizacja listenera do obslugi zdarzeń na RS232
        } catch (TooManyListenersException ex) {
            ex.printStackTrace();
        }
        // ---------------------------------------
        serialPort.notifyOnDataAvailable(true); // aktywacja zanacznika DATA_AVAILABLE (odczyt z portu)
        // ---------------------------------------
    }
    }

    
        private void zapiszDoPortu(String tekst) {
         if (stanPortu == true) {    // port otwarty
            try {
                outputStream.write(tekst.getBytes()); 
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
     }
   
   
    void connectCom(String portName) throws Exception
    { 
        if(portIdentifier == null)portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() ) {

        }
        else { 
            CommPort commPort;
            commPort = portIdentifier.open(this.getTitle(),2000);
            serialPort = (SerialPort) commPort;
            // ustawiene parametrów połaczenia
            // domyślnie -> 9600, 8, 1, N
            serialPort.setSerialPortParams(options.getBoudRate(),SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
            inputStream = serialPort.getInputStream(); //inicjalizacja strumienia wejsciowego
            outputStream = serialPort.getOutputStream(); //inicjalizacja strumienia wyjsciowego
            stanPortu = true;
            }
        }
    
    
    //    Odczyt z portu
    @Override
    public void serialEvent(SerialPortEvent event) {
        switch(event.getEventType()) {
            case SerialPortEvent.BI:                    // int =10
            case SerialPortEvent.OE:                    // int =7
            case SerialPortEvent.FE:                    // int =9
            case SerialPortEvent.PE:                    // int =8
            case SerialPortEvent.CD:                    // int =6
            case SerialPortEvent.CTS:                   // int =3
            case SerialPortEvent.DSR:                   // int =4
            case SerialPortEvent.RI:                    // int =5
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:   // int =2
                {

                break;
                }
            case SerialPortEvent.DATA_AVAILABLE:        // int =1
                        try {
                            endFrame = false;
                            int nb = inputStream.available();// ilość bajtów odczytana ze strumienia
                            while (nb > 0) {
				byte[] readBuffer = new byte[nb];
				inputStream.read(readBuffer);
				String str = new String(readBuffer);
                                char[] chars = str.toCharArray();
                                // str = str.replace("\r","\n");
				//jTextArea1.append(str + "\n");
                                for (int i=0; i!=nb;i++){
                                    char data = chars[i];
                                    if(data == '\n') {
                                        rx_buffer_wr_index = 0;
                                        rx_buffer_rx_index = 0;
                                    }
                                    if (data == '\r') endFrame = true;
                                    rx_buffer[rx_buffer_wr_index] = data;
                                    rx_buffer_wr_index++;
                                    if(rx_buffer_wr_index == rx_len)rx_buffer_wr_index = 0;
                                    rx_counter++;
                                }
				nb = inputStream.available();
                            }
                            if(endFrame){
                                endFrame = false;
                                frame = new Frame();
//                                if(testFrame()){
//                                    setNewValue();
//                                }
                            }
                        } catch (IOException ex) {
                        }
                break;
            }
    }

    private void setDatabase() {
        JdbcTest dbt = new JdbcTest("jdbc:mysql://localhost", "root", "");
        dbt.createAccount("user_gryf", "gryf");
        
        dbt = new JdbcTest("jdbc:mysql://localhost", "user_gryf", "gryf");
        dbt.createDB("gryf");
        dbt.createTable("alarm", " (id int(11) AUTO_INCREMENT PRIMARY KEY, client_id int, time TIMESTAMP, alarm_id int, accept timestamp NOT NULL DEFAULT '1970-01-01 00:00:01') ", "gryf");
        dbt.createTable("abonaments", " (id int(11) AUTO_INCREMENT PRIMARY KEY, name varchar(30) CHARACTER SET utf8 COLLATE utf8_polish_ci, address varchar(255)  CHARACTER SET utf8 COLLATE utf8_polish_ci, rs485address int, telefon varchar(255), password varchar(255))ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_polish_ci ", "gryf");
        dbt.createTable("alarm_description", " (id int(11) AUTO_INCREMENT PRIMARY KEY, alarm_id int, description varchar(11) CHARACTER SET utf8 COLLATE utf8_polish_ci, color text ,hot BOOLEAN) ", "gryf");
        dbt.createTable("armdisarm", "(id int(11) AUTO_INCREMENT PRIMARY KEY, description varchar(255), code int)", "gryf");
        dbt.fillArmedTable();
        
        System.out.println("jest gryf? " + dbt.isDbExist("gryf"));
        System.out.println("jest alarm table? " + dbt.isTableExist("gryf", "alarm"));
    }

    private void blink(final Gryf_abonament get, final Color color) {

        SwingWorker<Void, Boolean> worker = new SwingWorker<Void, Boolean>() {

            @Override
            protected Void doInBackground() throws Exception {
                boolean chunks = false;
                for(int i=0; i!=31; i++){
                    publish(chunks);
                    chunks = !chunks;
                    Thread.sleep(200);
                }
                
                return null;
            }
            
            @Override
            protected void process(List chunks) {
                // Aktualizujemy status zadania wykorzystując fakt, że metoda
                // process() wykonywana jest w EDT.
                int size = chunks.size();
                    if((boolean) chunks.get(size-1))get.setButtonColor(color, true);
                    else get.setButtonColor(color, false);
                
            }
        };
        worker.execute ();   
    }
    
    
    private class Frame {
    int address;
    String dataFrame;

        public Frame() {
        }

        public int getAddress() {
            return address;
        }

        public void setAddress(int address) {
            this.address = address;
        }

        public String getDataFrame() {
            return dataFrame;
        }

        public void setDataFrame(String dataFrame) {
            this.dataFrame = dataFrame;
        }
            
}
    
    
    private void eventHandle(int row) {
        if (row >= 0) {
            String[] options = {"Tak", "Nie"};
            String alarmId = (String) table.getValueAt(row, 0);

            int abonamentId = Integer.parseInt((String) table.getValueAt(row, 1));
            int choice = JOptionPane.showOptionDialog(null, //Component parentComponent
                    "Zatwierdzić zdarzenie", //Object message,
                    "Obsługa zdarzeń", //String title
                    JOptionPane.YES_NO_OPTION, //int optionType
                    JOptionPane.QUESTION_MESSAGE, //int messageType
                    null, //Icon icon,
                    options, //Object[] options,
                    "Metric");//Object initialValue 
            if (choice == 0) {
                try {
                    connDBase = DriverManager.getConnection("jdbc:mysql://localhost/gryf?characterEncoding=utf8", "user_gryf", "gryf");
                    String query = "UPDATE `alarm` SET `accept` = CURRENT_TIMESTAMP WHERE `id` =" + alarmId + ";";
                    Statement st = connDBase.createStatement();
                    st.execute(query);
                } catch (SQLException ex) {
                    Logger.getLogger(Gryf.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, ex, "błąd bazy w eventHandle", JOptionPane.ERROR);
                }
                ((DefaultTableModel) table.getModel()).removeRow(row);
                abonaments.get(abonamentId - 1).setLabel(Color.decode("0xffffff"), Integer.toString(0));
                abonaments.get(abonamentId - 1).setStatus(0);
            }
            if(table.getRowCount() == 0){
                blinkLabel.setBlinking(false);
            } 
        }
    }
    
    
    private void getOptions() {
        options = new Options();
        File file = new File(DefaultFolder + "\\options.xml");
        try {
            try (XMLDecoder xmldecoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)))) {
                options = (Options) xmldecoder.readObject();
            }

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "NOT EXIST OPTIONS FILE", "Finish", JOptionPane.INFORMATION_MESSAGE);
        }
        

        try {
            connDBase = DriverManager.getConnection("jdbc:mysql://localhost/gryf?characterEncoding=utf8", "user_gryf", "gryf");
            String query = "SELECT * FROM armdisarm WHERE `description` = 'uzbrojenie'";
            Statement st = connDBase.createStatement();
            ResultSet rs = st.executeQuery(query);
            rs.next();
            armedCode = (rs.getInt("code"));
            query = "SELECT * FROM armdisarm WHERE description='rozbrojenie'";
            st = connDBase.createStatement();
            rs = st.executeQuery(query);
            rs.next();
            disArmedCode = (rs.getInt("code"));
        } catch (SQLException ex) {
            Logger.getLogger(Gryf.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex, "błąd w getOption", JOptionPane.ERROR);

        }
        
    }

        private void showSoftwareInfo(boolean aboutClik) {

        ManifestLoader ml = new ManifestLoader();
        String verion = ml.getImplementationVersion();
        String compilDate = ml.getDataString();
        NewSoftwareInfo nsi = new NewSoftwareInfo("http://www.my-electronics.eu/gryf/soft.txt");
        if (nsi.isStatusOK()) {
            if (nsi.getNewSoftware().equals(verion)) {
                if(aboutClik)JOptionPane.showMessageDialog(aboutItem, "<HTML><H1>Gryf</H1><BR>" + "YOU ARE USING VERSION" + verion + "</HTML>", "Gryf", JOptionPane.INFORMATION_MESSAGE);
            } else {
                int loadNew = JOptionPane.showConfirmDialog(aboutItem, "<HTML><H1>Gryf</H1><H3><font color=\"red\">" + "TODEY IS A NEW VERSION" + "</font></H3><BR>" + "YOU ARE USING VERSION" + " " + verion + "<br>" + "NEW VERSION IS" + " " + nsi.getNewSoftware() + "<BR><BR><font color=\"red\">" + "DOWNLOAD CURRENT VERSION?" + "</font></HTML>", "Hex Manager", JOptionPane.INFORMATION_MESSAGE);
                if (loadNew == 0) {
//                    nsi.getSoftware();
                    FileLoader fl = new FileLoader(nsi.getZipAddress());
                    fl.execute();
                    JOptionPane.showMessageDialog(null, "Przed instalacją odinstaluj poprzednią wersję", "info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } else {
            if(aboutClik)JOptionPane.showMessageDialog(aboutItem, "<HTML><H1>Gryf</H1><BR><H3><font color=\"red\">" + "NOT CONNECTION TO THE SERVER" + "</font></H3><BR>" + "YOU ARE USING VERSION" + verion + "</HTML>", "Gryf", JOptionPane.INFORMATION_MESSAGE);
        }
    }
        
        private void enableMenu(){
            String[] options = {"Tak", "Anuluj"};
            JPasswordField pass = new JPasswordField(10);

            int choice = JOptionPane.showOptionDialog(null, //Component parentComponent
                    pass, //Object message,
                    "Odblokowanie menu", //String title
                    JOptionPane.YES_NO_OPTION, //int optionType
                    JOptionPane.QUESTION_MESSAGE, //int messageType
                    null, //Icon icon,
                    options, //Object[] options,
                    "Metric");//Object initialValue
            if(choice == 0){
                char[] password = pass.getPassword();
                String passwordString = new String(password);
                if(passwordString.equals("12345678")){
                    abonamentMenuItem.setEnabled(true);
                    alarmMenuItem.setEnabled(true);
                    comMenuItem.setEnabled(true);
                    connectItem.setEnabled(true);
                }
            }
        }
  
       public static void main(String[] args) {
        Gryf gryf = new Gryf();
        try {
            Random losKod = new Random();
            int speed = 100;
            while (true) {

                Thread.sleep(speed + losKod.nextInt(speed));
                gryf.insertEvent(1 + losKod.nextInt(9), 1 + losKod.nextInt(4));
                for (int i = 0; i != 25; i++) {
                    if (gryf.speedSimulate) {
                        speed = 1000;
                    } else {
                        speed = 10000;
                    }
                    Thread.sleep(1000 + losKod.nextInt(speed));
                    gryf.insertEvent(1 + losKod.nextInt(9), 1 + losKod.nextInt(2));
                }
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Gryf.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}

