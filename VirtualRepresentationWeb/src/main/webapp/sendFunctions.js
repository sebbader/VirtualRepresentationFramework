var sockets = new Map();
var methods  = ["CREATE", "READ", "UPDATE", "DELETE"];
var init = true;
var lastConnectionState = 2;
var idButtonRefreshInterval;
var statusCodes = {
    
    200 : "OK",
    201 : "Created",
    204 : "No Content",
    404 : "Resource not found.",
    500 : "Internal Server error"
    
};

function sendHTTP() {
    
    var domain = $("#http-tab #inputGroupPrepend").html();
    var name = $("#http-tab #avaName").val();
    var uri = domain + name;    
    var method = $('#http-tab #method').val();
    var accept = $('#http-tab #accept').val();
    var acceptName = $('#http-tab #accept option:selected').text().toLowerCase();
    var formData = new FormData();
    var contentType = false;                

    var file = $('#http-tab input[type=file]')[0].files[0];

    if(file!==undefined) {
        formData.append('file', file);                
    } else {
        formData.append('dummy', null);
    }

    jQuery.support.cors = true;
    $.ajax({
        method: method,
        url: uri,
        data:formData,
        processData: false,
        contentType: contentType,
        crossDomain:true,
        headers: {Accept : accept},
        beforeSend: function() {
            $('#load-http').button('loading');
        },
        success: function(data, textStatus, jqxhr) {

            var href = jqxhr.getResponseHeader('Content-Location');
            var hrefName = href;
            var download = "";
            var hideLink = false;
            
            console.log(jqxhr);
            
            //RDF Daten sollen heruntergeladen nund nicht angezeigt werden. 
            //Packe Daten in Datei und stelle Download Link bereit
            if(method==="GET" && data && 
                    jqxhr.status===200 && jqxhr.getResponseHeader("Content-Type")!=="text/html" &&
                    jqxhr.getResponseHeader("Content-Disposition")!==null) {

                //Taken from: https://stackoverflow.com/questions/17273110/convert-an-xml-jquery-object-to-string
                var all = $(data).find("*").get(0);
                var serializer = new XMLSerializer(); 
                var dataString = serializer.serializeToString(all);

                var file = new Blob([dataString], {type: jqxhr.getResponseHeader("Content-Type")});

                href = window.URL.createObjectURL(file);                       
                var dispoHeader = jqxhr.getResponseHeader("Content-Disposition");
                var fileName = dispoHeader.substring(dispoHeader.indexOf("=")+1,dispoHeader.length);

                hrefName = jqxhr.getResponseHeader("Content-Location") + fileName;
                download = 'download="' + fileName + '"';

            } else if((data!==null || data!==undefined || data!=="") && method==="GET" && 
                    jqxhr.getResponseHeader("Content-Disposition")!==null) {
                
                alert("Empty file was returned.");
                
            }

            var container = $('<div id="result" class="alert alert-success"><b>' + jqxhr.status + ':</b> ' + jqxhr.statusText + 
                                ' - <a href="' + href + '" ' + download + ' target="_blank">' + hrefName + '</a></div>')
                                .hide().appendTo("#respondContainer").fadeIn("fast").delay(3000).fadeOut("fast");

            //Hide Link
            if(method!=="GET") {
                $('div#result a').hide();
            }

            $('#load-http').button("reset");

        },
        error: function(header, textStatus, error) {
            
            console.log("error");

            $(`<div id="result" class="alert alert-danger alert-dismissible">
                    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                    <b>` +
                    header.status + `</b>: ` + header.statusText + `<br><br> ` +
                    //<button type="button" class="btn btn-default" data-toggle="collapse" data-target="#demo">Details</button>
                    //<div id="demo" class="collapse">` +                                 
                    //header.responseText + `</div>
                    `</div>`)
                    .hide().appendTo("#respondContainer").fadeIn("fast")
                    .delay(20000).fadeOut("fast");

            $('#load-http').button("reset");

        }

    });
}

function openWebSockets() {
    
    $('#connect-ws').button('loading');
    
    $('#websocket-tab #form #uri').attr('disabled', true);
    
    var domain = $("#websocket-tab #inputGroupPrepend").html();
    var name = $("#websocket-tab #avaName").val();
    var uri = domain + name;
    
    for(var i=0; i<4; i++) {
        
        sockets.set(methods[i], new WebSocket(uri, methods[i]));
        
        var socket = sockets.get(methods[i]);    
        socket.binaryType = "blob";
        
        socket.onopen = function() {
            console.log('opened ' + this.protocol + " channel");            
        };

        socket.onerror = function(onErrorEvent) {
            console.log('error on ' + this.protocol);
            changeButtonState(0);
            
            var container = $('<div id="result" class="alert alert-danger"><b>Error:</b> Channel ' + this.protocol + ": " + onErrorEvent.message + '</div>')
                                    .hide().appendTo("#respondContainer").fadeIn("fast").delay(5000).fadeOut("fast");            
            
        };

        socket.onclose = function(onCloseEvent) {

            console.log('Session closed for channel ' + this.protocol);
            changeButtonState(0);
            
            var container = $('<div id="result" class="alert alert-warning"><b>Closed:</b> Session closed for channel ' + this.protocol + '</div>')
                                    .hide().appendTo("#respondContainer").fadeIn("fast").delay(3000).fadeOut("fast");                        
            

        };

        socket.onmessage = function(msg) {
            
            var domain = $("#websocket-tab #inputGroupPrepend").html();
            var name = $("#websocket-tab #avaName").val();
            var uri = domain + name;        
            var download = "";
            var isFile = false; 
            var href =uri.replace("ws://", "http://");
            var hrefName = href;
            
            var statusCode = msg.data;
            
            if(msg.data instanceof Blob) {
                
                console.log("blob -> show file");
                
                isFile = true;
                
                var accept = $('#websocket-tab #accept').val();                
                var suffix = "";
                var download = "";
                
                //pack blob as downloadable file
                suffix = (accept==="text/html") ? ".html" : ".rdf";
                var fileName = "file" + suffix;                    
                download = 'download="' + fileName + '"';
                var data = msg.data;      
                href = window.URL.createObjectURL(data);                    
                hrefName = uri + "/" + fileName;

                statusCode = 200 + "";                                                               

            }
            
            var type="danger";
            if(statusCode.startsWith("2")) {
                type = "success";
            }
            
            var container = $('<div id="result" class="alert alert-' + type + '"><b>' + statusCode + ':</b> ' + statusCodes[statusCode] + 
                                    ' - <a href="' + href + '" ' + download + ' target="_blank">' + hrefName + '</a></div>')
                                    .hide().appendTo("#respondContainer").fadeIn("fast").delay(5000).fadeOut("fast");

            //Hide Link
            if(!isFile) {
                $('div#result a').hide();
            }               
            
        };  
    }
    
    idButtonRefreshInterval = setInterval(function() {
        changeButtonState(areWebSocketsAlive());
    }, 1000);    
    
}

function sendWS() {
    
    var method = $('#websocket-tab #method').val();
    var accept = $('#websocket-tab #accept').val();
    var acceptName = $('#websocket-tab #accept option:selected').text().toLowerCase();    
    var file = $('#websocket-tab input[type=file]')[0].files[0];
    var formData = new FormData();
    
    var msg = "";
    
    var socket = sockets.get(method);
    
    if(socket.protocol==="READ") {
        
        msg=accept;
        
    }

    if((socket.protocol==="CREATE" || socket.protocol==="UPDATE") && file!==undefined) {
        
        //Taken from https://stackoverflow.com/a/21819862
        var reader = new FileReader();
        var rawData = new ArrayBuffer();            
        //alert(file.name);
        
        socket.binaryType = "arraybuffer";
        
        reader.onloadstart = function() {
            console.log("Start upload");
            $('#iconUpload').show();
        };

        reader.onload = function(e) {
            rawData = e.target.result;
            socket.send(rawData);
            $('#iconUpload').removeClass('fa-upload')
                    .addClass('fa-check')
                    .delay(3000)
                    .hide()
                    .removeClass('fa-check')
                    .addClass('fa-upload');
        };

        reader.readAsArrayBuffer(file);        
        
    } else {
        socket.send(msg);
    }
    
}

//Taken from: https://stackoverflow.com/questions/6507293/convert-xml-to-string-with-jquery, 26.02.2018, 15:04
function xmlToString(xmlData) { // this functions waits jQuery XML 

    var xmlString = undefined;

    if (window.ActiveXObject){
        xmlString = xmlData[0].xml;
    }

    if (xmlString === undefined)
    {
        var oSerializer = new XMLSerializer();
        xmlString = oSerializer.serializeToString(xmlData[0]);
    }

    return xmlString;
}

function refreshVirtrepTable() {

    $('#reloadButtonManager').button('loading');
    $('#virtrepTable').load('virtrepOverviewTable.jsp').fadeIn("slow", function() {
        $('#reloadButtonManager').button('reset');
    });
    
} 

function areWebSocketsAlive() {
    
    var connection = 2; //0 = at least 1 error, 1 = all connected, 2 = init
    
    for(var i=0; i<4; i++) {
        
        socket = sockets.get(methods[i]);
        //console.log("Socket " + methods[i] + " has state" + socket.readyState);
        //One connection does not exist
        if(socket.readyState!==1 && !init) {
            connection=0;
            break;
        } else if(socket.readyState!==1 && init) {
            //All connections have been established -> no init phase.
            connection = 2;
        } else {
            connection = 1;
        }
        
    }
    //console.log("Return state " + connection);
    return connection;
    
}

function changeButtonState(status) {
    
    //console.log(status);
    
    if(status!==lastConnectionState) {
        lastConnectionState = status;
        if(status===1) {

            $('#connect-ws').button('reset')
                    .addClass('btn-success')                    
                    .removeClass('btn-primary');            
            $('#disconnect-ws').fadeIn('fast');
            $('#websocket-tab #avaName').prop("disabled", true);
            

        } else if(status===0){
            
            //console.log("No connection");
            $('#connect-ws').button('reset')
                    .removeClass('btn-success')
                    .addClass('btn-danger');

            setTimeout(function () {
                $('#connect-ws').removeClass('btn-danger');
                $('#connect-ws').addClass('btn-primary');
                //$('#connect-ws').html('Connect');
            }, 2500);    

            for(var i=0; i<4; i++) {

                var socket = sockets.get(methods[i])
                if(socket.readyState===1) {
                    socket.close();
                }

            }
            
            $('#websocket-tab #avaName').prop("disabled", false);            
            $('#disconnect-ws').fadeOut('fast');
            clearInterval(idButtonRefreshInterval);            

        }
    }    
}

function closeWebSockets() {
    
    for(var i=0; i<4; i++) {
        
        if(sockets.get(methods[i]) instanceof WebSocket) {
            
            sockets.get(methods[i]).close();
            
        }
        
    }        
    
}

function sendOPCUA() {
    
    var domain = $("#opc-ua-tab #inputGroupPrepend").html();
    var name = $("#opc-ua-tab #avaName").val();
    var uri = domain + name;
    var method = $('#opc-ua-tab #method').val();
    var file = $('#opc-ua-tab input[type=file]')[0].files[0];
    var formData = new FormData(); 
    formData.append('name', uri);
    formData.append('method', method);

    var file = $('#opc-ua-tab input[type=file]')[0].files[0];

    if(file!=undefined) {
        formData.append('file', file);                
    } else {
        formData.append('file', "dummy");                    
    }
    
    //alert(formData.getAll('name'));
    
    $.ajax({
        method: "POST",
        url: "/opcuaclient",
        data: formData,
        processData: false,
        contentType: false,                    
        beforeSend: function() {
            $('#load-opc-ua').button('loading');
        },
        success: function(data, textStatus, jqxhr) {

            var href = jqxhr.getResponseHeader('Content-Location');
            var hrefName = href;
            var download = "";
            var message = "";
            var noDownload = false;
            
            if((data==="" || data===null) && 
                    jqxhr.getResponseHeader("Content-Type")!=="text/html" &&
                    jqxhr.getResponseHeader("Content-Disposition")!==null) {
                
                alert('Returned file is empty.');
                noDownload = true;
                
            }

            //RDF Daten sollen heruntergeladen nund nicht angezeigt werden. 
            //Packe Daten in Datei und stelle Download Link bereit
            if(!noDownload && jqxhr.status===200 && data!=="" && data!==null && jqxhr.getResponseHeader("Content-Type")!=="text/html" && 
                    jqxhr.getResponseHeader("Content-Disposition")!==null) {

                //Taken from: https://stackoverflow.com/questions/17273110/convert-an-xml-jquery-object-to-string
                var all = $(data).find("*").get(0);
                var serializer = new XMLSerializer(); 
                var dataString = serializer.serializeToString(all);

                var data = new Blob([dataString], {type: jqxhr.getResponseHeader("Content-Type")});
                //var data = new Blob([data], {type: "octet/stream"});

                href = window.URL.createObjectURL(data);                       
                var dispoHeader = jqxhr.getResponseHeader("Content-Disposition");
                var fileName = dispoHeader.substring(dispoHeader.indexOf("=")+1,dispoHeader.length).replace("\"","/").replace("\"","");

                hrefName = jqxhr.getResponseHeader("Content-Location") + fileName;
                download = 'download="' + fileName + '"';

            } else {
                
                noDownload = true;
                message = jqxhr.responseText;
                
            }

            var container = $('<div id="result" class="alert alert-success"><b>' + jqxhr.status + ':</b> ' + message + 
                                ' - <a href="' + href + '" ' + download + ' target="_blank">' + hrefName + '</a></div>')
                                .hide().appendTo("#respondContainer").fadeIn("fast").delay(6000).fadeOut("fast");

            //Hide Link
            if(noDownload) {
                $('div#result a').hide();
            }

            $('#load-opc-ua').button("reset");

        },
        error: function(header, textStatus, error) {
            
            console.log("error");

            $(`<div id="result" class="alert alert-danger alert-dismissible">
                    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                    <b>` +
                    header.status + `</b>: ` + header.responseText + `<br><br> `+
                    //<button type="button" class="btn btn-default" data-toggle="collapse" data-target="#demo">Details</button>
                    //<div id="demo" class="collapse">` +                                 
                    //header.responseText + `</div>
                    `</div>`)
                    .hide().appendTo("#respondContainer").fadeIn("fast")
                    .delay(20000).fadeOut("fast");

            $('#load-opc-ua').button("reset");

        }

    });    
    
    
}
