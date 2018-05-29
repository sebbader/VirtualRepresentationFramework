package core.controller.model.writer;

import org.apache.jena.riot.adapters.RDFWriterRIOT;

/**
 * This creates a new HTMLWriter for RDF
 * @author Jan-Peter.Schmidt
 */
public class RDFWriterHTML extends RDFWriterRIOT {
    
    public RDFWriterHTML() {
        super("HTML");
    }
    
}
