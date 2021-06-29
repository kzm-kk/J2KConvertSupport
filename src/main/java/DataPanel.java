import javax.swing.*;

public class DataPanel{
    private JTextArea dataArea;
    private JPanel datapanel;

    public DataPanel(String text){
        datapanel.setBounds(0,0,50,35);
        dataArea.setText(text);
    }

    public JPanel getDatapanel(){
        return datapanel;
    }

}
