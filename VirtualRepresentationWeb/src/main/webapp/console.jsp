<%-- 
    Document   : httpRequestCreator
    Created on : 23.02.2018, 08:10:32
    Author     : Jan-Peter.Schmidt
--%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@page import="core.controller.avatars.Avatar"%>
<%@page import="core.controller.avatars.AvatarManager"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

        <!-- Optional theme -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
        
        <!-- JQueryUI theme -->
        <link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        
        <!-- Own theme -->
        <link rel="stylesheet" href="style.css">

        <!--JQuery -->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
        
        <!-- JQuery UI -->
        <script src="http://code.jquery.com/ui/1.12.1/jquery-ui.js" 
                integrity="sha256-T0Vest3yCU7pafRw9r+settMBX6JkKN06dqBnpQ8d30="
                crossorigin="anonymous"></script>     
        
        <!--FontAwesome-->
        <script defer src="https://use.fontawesome.com/releases/v5.0.6/js/all.js"></script>
        
        <!-- Latest compiled and minified JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>		                       
        
        <!-- Own JS Script -->
        <script src="sendFunctions.js"></script>
        
        <script>
            
            $(document).ready(function() { 
                setInterval(function() {
                    refreshAvatarTable();
                },120000);
                
                //Tab functionality like: https://jqueryui.com/tabs/
                $( function() {
                    $( "#tabs" ).tabs({
                        active: 0
                    });
                });                
                
            });            
        </script>
        
        <title>Console</title>
    </head>
    <body>
        <div class="container" style="width:90%">
            <div class="row">
                <div id="tabs">
                    <h1 style="text-align:center">Console</h1>
                    <ul>
                      <li><a href="#http-tab">HTTP</a></li>
                      <li><a href="#websocket-tab">WebSocket</a></li>
                      <li><a href="#opc-ua-tab">OPC UA</a></li>
                    </ul>                    
                    <div id="http-tab"> 
                        <div class="col-md" style="padding:2.5%; flex-basis: 0;">
                            <h3>HTTP Request</h3>
                            <form class="bd-example" enctype="multipart/form-data" id="form">
                                <div class="form-group">
                                    <label for="validationCustomUsername">Username</label>
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <div class="input-group-text" id="inputGroupPrepend">http://${pageContext.request.serverName}:${pageContext.request.localPort}/avatars/</div>
                                            <input type="text" class="form-control" id="avaName" name="avaName" value="10011234"
                                               placeholder="Avatarname" aria-describedby="inputGroupPrepend" required>                                            
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="method">Method:</label>
                                    <select id="method" name="method" class="form-control">
                                        <option value="GET">GET</option>
                                        <option selected="selected" value="POST">POST</option>
                                        <option value="PUT">PUT</option>
                                        <option value="DELETE">DELETE</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label for="accept">Accept:</label>
                                    <select id="accept" name="accept" class="form-control">
                                        <option value="text/html">HTML</option>
                                        <option value="application/rdf+xml">RDFXML</option>
                                        <!--<option value="application/x-turtle">TURTLE</option>                       
                                        <option value="text/rdf+n3">N3</option>
                                        <option value="text/plain">NTriples</option>-->
                                    </select>
                                </div> 
                                <div class="form-group">
                                    <label for="file">Dateiupload</label>
                                    <p class=".help-block" style="font-size:10px">Dataacquisition (.n3), Dataaggregation (.rq)</p>
                                    <input type="file" name="file" id="file" class="form-control-file">
                                </div>
                                <div class="form-group">
                                    <button type="button" class="btn btn-primary btn-lg " id="load-http" 
                                            data-loading-text="<i class='fa fa-circle-o-notch fa-spin'></i> warte auf Antwort..." 
                                            onClick="sendHTTP();">send</button>
                                </div>
                            </form>
                        </div>
                    </div>
                    <div id="websocket-tab"> 
                        <div class="col-md" style="padding:2.5%; flex-basis: 0;">
                            <h3>Websocket</h3>
                            <form class="bd-example" enctype="multipart/form-data" id="form">
                                <div class="form-group">
                                    <label for="validationCustomUsername">Username</label>
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <div class="input-group-text" id="inputGroupPrepend">ws://${pageContext.request.serverName}:${pageContext.request.localPort}/avatars/</div>
                                            <input type="text" class="form-control" id="avaName" name="avaName" value="10011234"
                                               placeholder="Avatarname" aria-describedby="inputGroupPrepend" required>                                            
                                        </div>
                                    </div>                                </div>
                                <div class="form-group">
                                    <label for="method">Operation</label>
                                    <select id="method" name="method" class="form-control">
                                        <option value="CREATE">CREATE</option>
                                        <option value="READ">READ</option>
                                        <option value="UPDATE">UPDATE</option>
                                        <option value="DELETE">DELETE</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label for="accept">Accepted Dataformat:</label>
                                    <select id="accept" name="accept" class="form-control">
                                        <option value="text/html">HTML</option>
                                        <option value="application/rdf+xml">RDFXML</option>
                                    </select>
                                </div> 
                                <div class="form-group">
                                    <label for="file">Dateiupload <i class="fas fa-upload" id="iconUpload" style="display:none;"></i></label>
                                    <p class=".help-block" style="font-size:10px">Datenbeschaffung (.n3), Datenaggregation (.rq)</p>
                                    <input type="file" name="file" id="file" class="form-control-file">                                    
                                </div>
                                <div class="form-group">
                                    <button type="button" class="btn btn-primary btn-lg " id="connect-ws" 
                                            data-loading-text="<i class='fas fa-plug'></i> connecting..." 
                                            onClick="openWebSockets();">Connect</button>                               
                                    <button type="button" class="btn btn-warning btn-lg " id="disconnect-ws" 
                                            data-loading-text="<i class='fas fa-plug'></i> disconnecting..." 
                                            onClick="closeWebSockets();" style="display:none;">Disconnect</button>                                     
                                    <button type="button" class="btn btn-secondary-own btn-lg " id="load" 
                                            data-loading-text="<i class='fa fa-circle-o-notch fa-spin'></i> waiting for answer..." 
                                            onClick="sendWS();" style="float:right;">sendMsg</button>                                    
                                </div>
                            </form>
                        </div>
                    </div>                    
                    <div id="opc-ua-tab"> 
                        <div class="col-md" style="padding:2.5%; flex-basis: 0;">
                            <h3>OPC UA</h3>
                            <form class="bd-example" enctype="multipart/form-data" id="form">
                                <div class="form-group">
                                    <label for="validationCustomUsername">Username</label>
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <div class="input-group-text" id="inputGroupPrepend">http://${pageContext.request.serverName}:${pageContext.request.localPort}/avatars/</div>
                                            <input type="text" class="form-control" id="avaName" name="avaName" value="10011234"
                                               placeholder="Avatarname" aria-describedby="inputGroupPrepend" required>                                            
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="method">Method:</label>
                                    <select id="method" name="method" class="form-control">
                                        <option value="POST">CREATE</option>
                                        <option value="GET">READ</option>
                                        <option value="PUT">UPDATE</option>
                                        <option value="DELETE">DELETE</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label for="file">Dateiupload</label>
                                    <p class=".help-block" style="font-size:10px">Dataacquisition (.n3), Dataaggregation (.rq)</p>
                                    <input type="file" name="file" id="file" class="form-control-file">
                                </div>
                                <div class="form-group">
                                    <button type="button" class="btn btn-primary btn-lg " id="load-opc-ua" 
                                            data-loading-text="<i class='fa fa-circle-o-notch fa-spin'></i> warte auf Antwort..." 
                                            onClick="sendOPCUA();">send</button>
                                </div>
                            </form>
                        </div>
                    </div>                    
                </div>
                <div class="col" id="containerManager">
                    <h1>AvatarManager</h1>              
                    <button type="button" class="btn btn-secondary btn-xs" style="margin:1.5% 0 1.5% 1.5%;" onClick="refreshAvatarTable();" id="reloadButtonManager" 
                            data-loading-text="<i class='fa fa-circle-o-notch fa-spin'></i> loading..."><i class="fas fa-sync"></i> Reload</button>
                    <div id="avatarTable"> <%@ include file="avatarOverviewTable.jsp" %></div>
                </div>
            </div>
            <div id="respondContainer"></div>
        </div>
    </body>
</html>