/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.communication;

import java.io.File;

/**
 * This class is a wrapper for read responses. It contains the status
 * of the request and optionally a file, e.g. the rdf-file that the user
 * requested.
 * 
 * @author Jan-Peter.Schmidt
 */
public class ReadResponse {
    
    private int status;
    private File file;
    
    /**
     * 
     * @param status Status of request
     * @param file Response file that should be returned to the user
     */

    public ReadResponse(int status, File file) {
        this.status = status;
        this.file = file;
    }
    
    /**
     * 
     * @return Status of request if set, else null.
     */

    public int getStatus() {
        return status;
    }

    /**
     * 
     * @param status Status of request
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 
     * @return Reponse file that should be returned to the user
     */
    public File getFile() {
        return file;
    }
    /**
     * 
     * @param file Reponse file that should be returned to the user
     */
    public void setFile(File file) {
        this.file = file;
    }
    
    
    
}
