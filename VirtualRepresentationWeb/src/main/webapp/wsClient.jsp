<%-- 
    Document   : wsClient
    Created on : 20.02.2018, 15:33:50
    Author     : Jan-Peter.Schmidt
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>WS Client</title>
        <script>
            
            var socket;

            function restartSocket(url) {
                
                console.clear();
                
                console.log("Create WS for " + url);
                socket = new WebSocket(url);

                // callback-Funktion wird gerufen, wenn die Verbindung erfolgreich aufgebaut werden konnte
                socket.onopen = function () {
                    console.log("Verbindung wurde erfolgreich aufgebaut: \"" + url + "\"");
                };

                // callback-Funktion wird gerufen, wenn eine neue Websocket-Nachricht eintrifft
                socket.onmessage = function (messageEvent) {
                    
                    console.log("onMessage");
                    console.log(messageEvent.data);
                };

                // callback-Funktion wird gerufen, wenn ein Fehler auftritt
                socket.onerror = function (errorEvent) {
                    console.log("Error! Die Verbindung wurde unerwartet geschlossen.");
                };

                socket.onclose = function (closeEvent) {
                    console.log('Die Verbindung wurde geschlossen --- Code: ' + closeEvent.code + ' --- Grund: ' + closeEvent.reason);
                };

                console.log("Ready.. ");                
                
            }            
        </script>
    </head>
    <body>
        <h1>WS Client.</h1>
        <form>
            Adresse: <input type="text" id="addr" value="ws://localhost:8080/avatars/10011234/123" style="width:300px;"><br><br>
            <input type="button" onClick="restartSocket(document.getElementById('addr').value);" value="Websocket öffnen">
        </form>
        <div id="console">
        </div>
    </body>
</html>