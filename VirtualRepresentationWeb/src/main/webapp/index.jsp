<%-- 
    Document   : index
    Created on : 20.02.2018, 10:35:59
    Author     : Jan-Peter.Schmidt
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

        <!-- Optional theme -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
        
        <!-- JQueryUI theme -->
        <link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        
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
        
        <title>Virtual Representation Framework - Home</title>
    </head>
    <body>
        <div class="container">
            <h1>Virtual Representation Framework</h1>
            <div class="row">
                <div class="col-sm">
                    <h2><a href="/console.jsp">Console</a></h2>
                    <span class="icon"><i class="far fa-edit"></i></span>
                    <p>
                        This tool is a client implementation for testting purposes.
                        You can perform crud operations for HTTP, WebSocket or
                        OPC UA communication.
                    </p>
                </div>
                <div class="col-sm">
                    <h2><a href="/">GitHub</a></h2>
                    <span class="icon"><i class="fab fa-github"></i></span>
                    <p>
                        This framework is available on GitHub for easy usage and
                        development. There is also a wiki available where first 
                        steps are described.
                    </p>
                </div>
                <div class="col-sm">
                    <h2><a href="">Placeholder</a></h2>
                    <span class="icon"><i class="far fa-edit"></i></span>
                    <p>
                        Lorem ipsum dolor sit amet, consetetur sadipscing elitr, 
                        sed diam nonumy eirmod tempor invidunt ut labore et 
                        dolore magna aliquyam erat, sed diam voluptua. At vero 
                        eos et accusam et                        
                    </p>
                </div>
            </div>
            <footer>
                Jan-Peter Schmidt, Master-Thesis, 2018
            </footer>
        </div>
    </body>
</html>