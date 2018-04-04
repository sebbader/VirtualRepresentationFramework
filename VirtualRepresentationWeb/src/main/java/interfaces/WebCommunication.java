/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import core.controller.communication.ReadResponse;
import java.io.File;

/**
 *
 * @author Jan-Peter.Schmidt
 */
public interface WebCommunication {
    
    int executeCreate(String name, File file);
    ReadResponse executeRead(String name, String accept);
    int executeUpdate(String name, File file);
    int executeDelete(String name);
    
}