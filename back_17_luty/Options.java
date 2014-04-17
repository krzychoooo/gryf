/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gryf;

import java.io.Serializable;

/**
 *
 * @author krzysztofklimas@interia.pl
 */
public class Options implements Serializable{
    String comNumber;
    int boudRate;
    int typFrame;
    final int TEXTFRAME = 1;

    public Options() {
    }
    
    public Options(String comNumber, int boudRate, int typFrame) {
        this.comNumber = comNumber;
        this.boudRate = boudRate;
        this.typFrame = typFrame;
    }

    public int getBoudRate() {
        return boudRate;
    }

    public void setBoudRate(int boudRate) {
        this.boudRate = boudRate;
    }

    public String getComNumber() {
        return comNumber;
    }

    public void setComNumber(String comNumber) {
        this.comNumber = comNumber;
    }

    public int getTypFrame() {
        return typFrame;
    }

    public void setTypFrame(int typFrame) {
        this.typFrame = typFrame;
    }

    
    
}
