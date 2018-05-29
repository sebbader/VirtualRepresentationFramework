package core.controller.model.writer;

import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WriterGraphRIOT;
import org.apache.jena.riot.WriterGraphRIOTFactory;

/**
 * Ths class creates a new HTMLWriter on Registration
 * @author Jan-Peter.Schmidt
 */
public class HTMLWriterFactory implements WriterGraphRIOTFactory {
    
    @Override
    public WriterGraphRIOT create(RDFFormat syntaxForm) {
        return new HTMLWriter() ;
    }
}
