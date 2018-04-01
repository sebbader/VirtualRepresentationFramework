<%-- 
    Document   : avatarOverviewTable
    Created on : 02.03.2018, 11:41:53
    Author     : Jan-Peter.Schmidt
--%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@page import="core.controller.avatars.Avatar"%>
<%@page import="core.controller.avatars.AvatarManager"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
            <table class="table table-striped">
                <tr>
                    <th>Name</th>
                    <th>Dataacquisition</th>
                    <th>Dataaggregation</th>
                    <th>Datamodel</th>
                    <th>URI</th>
                </tr>
                <%
                    HashMap<String, Avatar> map = AvatarManager.getRegisteredAvatars();
                    
                    if(map.size()==0) {
                        
                        out.write("<tr><td colspan=\"5\"><div class=\"alert alert-warning\">No avatars registered in AvatarManager.</div></td></tr>");
                        
                    }
                    
                    Iterator<Entry<String,Avatar>> iterator = map.entrySet().iterator();
                    while(iterator.hasNext()) {
                        
                        Entry<String, Avatar> entry = iterator.next();
                        String name = entry.getKey();
                        Avatar avatar = entry.getValue();
                        String host = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                        String dataacquisition = " - ";
                        String dataaggregation = " - ";
                        String datamodel = " - ";
                        
                        
                        if(avatar!=null && avatar.getDataAcquisition()!=null) {
                            
                            dataacquisition = avatar.getDataAcquisition().getGraph().size() + " Triples";
                            
                        }                        
                        
                        if(avatar!=null && avatar.getDataAggregation()!=null) {
                            
                            switch(avatar.getDataAggregation().getQueryType()) {
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
                        
                        if(avatar!=null && avatar.collectData()!=null) {
                            
                            datamodel = avatar.collectData().getGraph().size() + " Triples";
                            
                        }                        
                        
                        out.write("<tr>"
                                + "<td>" + name + "</td>"
                                + "<td>" + dataacquisition + "</td>"
                                + "<td>" + dataaggregation + "</td>"
                                + "<td>" + datamodel + "</td>"
                                + "<td><a href=\"" + host + "/avatars/" + name +"\" target=\"_blank\">" + host + "/avatars/" + name +"</a></td>"
                                + "</tr>");                                                
                    }                    
                %>
            </table>
