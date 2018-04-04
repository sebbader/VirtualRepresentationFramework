/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.model.writer;

import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WriterGraphRIOT;
import org.apache.jena.riot.WriterGraphRIOTFactory;

/**
 *
 * @author Jan-Peter.Schmidt
 */
public class HTMLWriterFactory implements WriterGraphRIOTFactory {
    
    @Override
    public WriterGraphRIOT create(RDFFormat syntaxForm) {
        return new HTMLWriter() ;
    }
}
