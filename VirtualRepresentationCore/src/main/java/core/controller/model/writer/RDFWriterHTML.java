/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.model.writer;

import org.apache.jena.riot.adapters.RDFWriterRIOT;

/**
 *
 * @author Jan-Peter.Schmidt
 */
public class RDFWriterHTML extends RDFWriterRIOT {
    
    public RDFWriterHTML() {
        super("HTML");
    }
    
}
