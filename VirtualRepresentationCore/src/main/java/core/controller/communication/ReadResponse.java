/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.communication;

import java.io.File;

/**
 *
 * @author Jan-Peter.Schmidt
 */
public class ReadResponse {
    
    private int status;
    private File file;

    public ReadResponse(int status, File file) {
        this.status = status;
        this.file = file;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    
    
}
