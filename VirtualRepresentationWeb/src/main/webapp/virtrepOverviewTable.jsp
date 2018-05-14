<%-- 
    Document   : virtrepOverviewTable
    Created on : 02.03.2018, 11:41:53
    Author     : Jan-Peter.Schmidt
--%>
<%@page import="org.apache.jena.rdf.model.Model"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.SortedSet"%>
<%@page import="java.util.TreeSet"%>
<%@page import="core.controller.virtualrepresentations.VirtualRepresentation"%>
<%@page import="core.controller.virtualrepresentations.VirtualRepresentationManager"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
            <table class="table table-striped">
                <tr>
                    <th>Type</th>
                    <th>Dataacquisition</th>
                    <th>Dataaggregation</th>
                    <th>Datamodel</th>
                    <th>URI</th>
                </tr>
                <%
                    HashMap<String, VirtualRepresentation> map = VirtualRepresentationManager.getRegisteredRepresentations();
                    
                    if(map.size()==0) {
                        
                        out.write("<tr><td colspan=\"5\"><div class=\"alert alert-warning\">No representations registered in RepresentationManager.</div></td></tr>");
                        
                    }
                    
                    //Sorting taken from: https://stackoverflow.com/a/14936963
                    SortedSet<String> sortedKeys = new TreeSet<>(map.keySet());
                    Iterator<String> iterator = sortedKeys.iterator();
                    
                    while(iterator.hasNext()) {
                        
                        String key = iterator.next();
                        VirtualRepresentation representation = map.get(key);
                        String name = representation.getClass().getName();
                        String host = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                        String dataacquisition = " - ";
                        String dataaggregation = " - ";
                        String datamodel = " - ";
                        
                        
                        if(representation!=null && representation.getDataAcquisition()!=null) {
                            
                            dataacquisition = representation.getDataAcquisition().getGraph().size() + " Triples";
                            
                        }                        
                        
                        if(representation!=null && representation.getDataAggregation()!=null) {
                            
                            switch(representation.getDataAggregation().getQueryType()) {
                                case 111:
                                    dataaggregation = "SELECT";
                                    break;
                                case 222:
                                    dataaggregation = "CONSTRUCT";
                                    break;                                    
                                case 333:
                                    dataaggregation = "DESCRIBE";
                                    break;                                    
                                case 444:
                                    dataaggregation = "ASK";
                                    break;
                                default:
                                    dataaggregation = "UNKNOWN";
                                 
                            }
                            
                        }
                        
                        
                        if(representation!=null) {
                            
                            datamodel = String.valueOf(representation.getModelSize());
                            
                        }                        
                        
                        out.write("<tr>"
                                + "<td>" + name + "</td>"
                                + "<td>" + dataacquisition + "</td>"
                                + "<td>" + dataaggregation + "</td>"
                                + "<td>" + datamodel + "</td>"
                                + "<td><a href=\"" + host + "/representations/" + key +"\" target=\"_blank\">" + host + "/representations/" + key +"</a></td>"
                                + "</tr>");                                                
                    }                    
                %>
            </table>
