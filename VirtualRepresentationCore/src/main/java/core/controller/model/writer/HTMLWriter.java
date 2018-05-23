/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.model.writer;

import core.controller.virtualrepresentations.VirtualRepresentationManager;
import core.controller.utils.VRProp;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.writer.WriterGraphRIOTBase;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDFS;

/**
 *
 * @author Jan-Peter.Schmidt
 */
public class HTMLWriter extends WriterGraphRIOTBase
    {
        // Ignore externally provided prefix map and baseURI
        @Override
        public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
        {
            HTMLWriter.write(out, graph);
        }

        @Override
        public Lang getLang()   { return  RDFLanguages.contentTypeToLang("text/html") ; }

        @Override
        public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
        {
            // Writers are discouraged : just hope the charset is UTF-8.
            IndentedWriter x = RiotLib.create(out) ;
            HTMLWriter.write(x, graph);
        }
        
        private static void write(OutputStream out, Graph graph) {
            
            try {
                out.write(getHTML(graph).getBytes());
            } catch (IOException ex) {
                Logger.getLogger(HTMLWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        private static void write(IndentedWriter writer, Graph graph) {
            
            writer.write(getHTML(graph));
            
        }
        
        private static String getHTML(Graph graph) {
            
            Model model = ModelFactory.createModelForGraph(graph);
            
            System.out.println("HTML");
            
            String headline = "Undefined";
            
            Property hasValue = VRProp.HAS_VALUE;
            Property unitOfMeasure = ResourceFactory.createProperty("https://w3id.org/saref/", "Unit_of_measure");
            
            try {
                headline = model.listObjectsOfProperty(RDFS.label).next().asLiteral().getString();
            } catch(Exception e) {
                //e.printStackTrace();
            }
            
            String s = getHTMLTemplateTop(headline) +
                "<h1>" + headline + "</h1>" +
                "<table class=\"table table-striped\">"
                    + " <tr>"
                    + "  <th>Property</th>"
                    + "  <th>Value</th>"
                    + "  <th>Unit</th>"
                    + "</tr>";

            //Print all properties of representation
            List<Statement> statements = new ArrayList<>();
            
            StmtIterator iteratorVal = model.listStatements(new SimpleSelector(null, hasValue, (RDFNode) null));            
            StmtIterator iteratorChildren = model.listStatements(new SimpleSelector(null, VRProp.HAS_CHILDREN, (RDFNode) null));
            StmtIterator iteratorParent = model.listStatements(new SimpleSelector(null, VRProp.HAS_PARENT, (RDFNode) null));
            StmtIterator iteratorRegistered = model.listStatements(new SimpleSelector(null, VRProp.REGISTERED, (RDFNode) null));
            
            statements.addAll(iteratorVal.toList());
            statements.addAll(iteratorChildren.toList());
            statements.addAll(iteratorParent.toList());
            statements.addAll(iteratorRegistered.toList());
            
            Iterator<Statement> iterator = statements.iterator();
            
            while(iterator.hasNext()) {

                Statement stmt = iterator.next();                
                String[] unit = {null};
                
                model.listObjectsOfProperty(stmt.getSubject(), unitOfMeasure)
                        .toList().forEach((RDFNode unitObject) -> {
                    
                    unit[0] = unitObject.toString();

                });            
                        
                if(unit[0]==null) {
                    unit[0] = "";
                }
                
                String property = stmt.getSubject().getLocalName();
                if(property==null || property.equals("") && 
                        !stmt.getSubject().toString().equals(VRProp.HAS_VALUE.toString())) {
                    property = stmt.getPredicate().getLocalName();
                }
                
                s = s + "<tr>"
                        + "<td>" + checkForLinks(property) + "</td>"
                        + "<td>" + checkForLinks(stmt.getObject().isLiteral() ? stmt.getObject().asLiteral().getString() : stmt.getObject().asResource().toString()) + "</td>"
                        + "<td>" + checkForLinks(unit[0]) + "</td>"
                + "</tr>";
                
            }
            
            s = s + "</table>";
            s = s + getHTMLTemplateBottom();
            
            return s;
            
        }
        
        private static String getHTMLTemplateTop(String headline) {
            
            return "<!DOCTYPE html>\n" +
                    "<html lang=\"de\">\n" +
                    "	<head>\n" +
                    "            <meta charset=\"utf-8\">\n" +
                    "		<title>Representation &Uuml;bersicht" + ((headline!=null && !headline.equals("") ? (" - " + headline) : (""))) + "</title>\n" +
                    "		<!-- JQuery integration -->\n" +
                    "		<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js\"></script> \n" +
                    "		\n" +
                    "		<!-- Latest compiled and minified CSS -->\n" +
                    "		<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" "
                    + "integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\">\n" +
                    "\n" +
                    "		<!-- Optional theme -->\n" +
                    "		<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css\" "
                    + "integrity=\"sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp\" crossorigin=\"anonymous\">\n" +
                    "\n" +
                    "		<!-- Latest compiled and minified JavaScript -->\n" +
                    "		<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\" "
                    + "integrity=\"sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa\" crossorigin=\"anonymous\"></script>		\n" +
                    "	</head>\n" +
                    "	<body>\n" +
                    "		<div class=\"container\">";
            
        }
        
        private static String getHTMLTemplateBottom() {
            
            return "		</div>\n" +
                    "	</body>\n" +
                    "</html>";
            
        }
        
        private static String checkForLinks(String toBeChecked) {
            
            if(toBeChecked.contains("http")) {
                
                return "<a href=\"" + toBeChecked + "\">" + toBeChecked + "</a>" ;
               
            }
            
            return toBeChecked;
            
        }
    }

