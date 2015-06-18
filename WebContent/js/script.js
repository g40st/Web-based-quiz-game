var request;
var playerSocket;
var clientID = -1;
var countCatalog = 0;
var countPlayer = 0;
var catalogChosen = false;
var catalogName;
var gameStarted = false;

function init() {
	// Eventlistener fuer Button
	var submitButton = window.document.getElementById("btnSubmit");
	submitButton.addEventListener("click", btnSubmitListener, false);
	
	console.log("Init wird aufgerufen");
	request = new XMLHttpRequest();
	// function die die Antwort verarbeitet
	request.onreadystatechange=CatalogResponseListener;
	request.open("POST", "catalogList", true);
	
	// CatalogRequest vorbereiten (RFC)
	var catalogRequest ="Type=3&Length=0";
	request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    request.setRequestHeader("Content-length", catalogRequest.length);
    request.send(catalogRequest);
    
    // PlayerSocket anlegen
	var url = 'ws://' + window.location.hostname + ':8080/Aufgabe4/player';
	playerSocket=new WebSocket(url);
	playerSocket.onopen=playerOpened;
	playerSocket.onclose=playerClosed;
	playerSocket.onerror=playerErrorHandler;
	playerSocket.onmessage=playerReceiveMessage;
}

function CatalogResponseListener() {
	// Kataloge in die HTML einfuegen
	if(request.readyState == 4) {
		var CatalogList = JSON.parse(request.responseText);
		while(CatalogList[countCatalog].Length > 0) {
			if(CatalogList[countCatalog].Type != 4) {
				i++;
				continue;
			} 
			var CatalogElementDiv = document.getElementById("rowCatalog");
			var para = document.createElement("p");
			para.setAttribute("class","pCatalog");
			para.setAttribute("id", "catalog" + countCatalog);
			var t = document.createTextNode(CatalogList[countCatalog].Name);
			para.appendChild(t);
			CatalogElementDiv.appendChild(para);
			para.addEventListener("click", clickCatalog, false);
			countCatalog++;
		}
	}
}

// Reaktion auf die Katalog Elemente bei Klick
function clickCatalog(Event) {
	if(clientID == 0 && gameStarted == false) {
		for(var i = 0; i < countCatalog; i++) {
			var tmpCatalog = document.getElementById("catalog" + i);
			tmpCatalog.style.color = "white";
		} 
		var activeCatalog = Event.target;
		activeCatalog.style.color = "red";
	
		var catalogChange = {
				"Type": "5",
				"Length" : activeCatalog.innerHTML.length,
				"Filename" : activeCatalog.innerHTML + ".xml"
			};
		
		playerSocket.send(JSON.stringify(catalogChange));
	}
}

// Login Button Listener
function btnSubmitListener(Event) {
	// Falls es ein FehlerTag bereits gibt -> entfernen
	var error = document.getElementById("errorTag");
	if(error != null) {
		error.remove();
	}
	var username = window.document.getElementById("username").value;
	var catalogRequest = {
		"Type": "1",
		"Length" : username.length,
		"Name" : username
	};
	
	playerSocket.send(JSON.stringify(catalogRequest));
}


// Funktion ueberprueft ob min. 2 Spieler angemeldet sind wenn ja dann "Spiel starten"-Button anzeigen
function checkEnoughPlayer () {
	if(clientID == 0) { // Nur Spielleiter kann das Spiel starten
		if(document.getElementById("divEnoughPlayer") != null){
			var startGame = document.getElementById("divEnoughPlayer");
			startGame.parentNode.removeChild(startGame);
		}
		
		if(countPlayer > 1) {
			var enoughPlayer = document.getElementById("startGame");
			var div = document.createElement("div");
			div.setAttribute("role","alert");
			div.setAttribute("class","alert alert-success");
			div.setAttribute("id","divEnoughPlayer");
			var button = document.createElement("button");
			button.setAttribute("class", "btn btn-primary");
			button.setAttribute("type", "button");
			var t = document.createTextNode("Spiel starten");
		
			button.appendChild(t);
			div.appendChild(button);
			enoughPlayer.appendChild(div);
			button.addEventListener("click", btnEnoughPlayerListener, false);
			
		}
	}
}

// Spiel starten Button Listener
function btnEnoughPlayerListener(Event) {
	if(catalogChosen) {
		var startGame = {
			"Type": "7",
			"Length" : catalogName.length,
			"Filename" : catalogName 
		};
		playerSocket.send(JSON.stringify(startGame));
	} else {
		
		var errorElement = document.getElementById("Error");
		var div = document.createElement("div");
		div.setAttribute("id", "errorTag")
		div.setAttribute("class","alert alert-danger");
		if(document.getElementById("errorTag") != null){
			console.log("ErrorTag vorhanden");
			var loginF = document.getElementById("errorTag");
			loginF.parentNode.removeChild(loginF);	
			
		}
		var t = document.createTextNode("Choose Catalog");
		div.appendChild(t);
		errorElement.appendChild(div);
		
		
	}
}

function playerOpened(){}

function playerClosed(){playerSocket.close();}

function playerErrorHandler(Event){alert("Fehler bei den Websockets "+ Event.data);}

function playerReceiveMessage(message){
	var msgServer = JSON.parse(message.data);
	console.log(msgServer);
	
	// LoginResponseOK
	if(msgServer.Type == 2) {
		// Speichern der Client ID, um den Spielleiter zu bestimmen
		clientID = msgServer.ClientID;
		// Die LoginForm entfernen
		removeLoginForm();
		// Text anfügen
		addGameText()
	}
	// PlayerList
	if(msgServer.Type == 6) {
		countPlayer = msgServer.Length / 37;
		if(gameStarted == false) {
			checkEnoughPlayer();
		}
		// Alle bisherigen Spieler löschen
		for(var i = 0; i < 6; i++) {
			var player = document.getElementById("rowPlayer"+ i);
			while(player.firstChild) {
				player.removeChild(player.firstChild);
			}
		}
		
		for(var i = 0; i < countPlayer; i++) {
			var player = document.getElementById("rowPlayer"+ i);
			while(player.firstChild) {
				player.removeChild(player.firstChild);
			}
			var para = document.createElement("p");
			para.setAttribute("class","player" + i);
			var t = document.createTextNode(msgServer.Players[i].Spielername + "   "+ msgServer.Players[i].Punktestand);
			para.appendChild(t);
			player.appendChild(para);
		}
	} else if(msgServer.Type == 5) { // CatalogChange
		var tmpCatalog = null;
		for(var i = 0; i < countCatalog; i++) {
			tmpCatalog = document.getElementById("catalog" + i);
			tmpCatalog.style.color = "white";
			if(tmpCatalog.innerHTML + ".xml" === msgServer.Message) {
				tmpCatalog.style.color = "red";
				catalogName = msgServer.Message;
			}
		}
		catalogChosen = true; // Flag setzen, dass ein Katalog ausgewaehlt wurde
	} else if(msgServer.Type == 7) { // startGame message
		gameStarted = true;
		// Falls ein Fehler aufgetreten ist -> loeschen
		if(document.getElementById("errorTag") != null){
			console.log("ErrorTag vorhanden");
			var loginF = document.getElementById("errorTag");
			loginF.parentNode.removeChild(loginF);
		}
		// Den "Spiel starten"-Button entfernen nur Spielleiter
		if(clientID == 0) {
			var startGame = document.getElementById("divEnoughPlayer");
			startGame.parentNode.removeChild(startGame);
		}
		// Den Login Text entfernen
		var idLoginOKText = document.getElementById("idLoginOKText");
		idLoginOKText.parentNode.removeChild(idLoginOKText);
		// questionRequest senden
		var questionRequest = {
			"Type": "8",
			"Length" : 0
		};
		playerSocket.send(JSON.stringify(questionRequest));
	
	} else if(msgServer.Type == 9) { // Question
		
		// Loeschen
		if(document.getElementById("startGame") != null) {
			var startGameBanner = document.getElementById("startGame");
			startGameBanner.parentNode.removeChild(startGameBanner);
		}
		if(document.getElementById("startButton") != null) {
			var startGameBanner = document.getElementById("startButton");
			startGameBanner.parentNode.removeChild(startGameBanner);
		}
		
		if(msgServer.Length == 769) {
			// Frage anzeigen
			if(document.getElementById("idIssue") == null) {
				var question = document.getElementById("questions");
				var div = document.createElement("div");
				div.setAttribute("class","col-md-12 issue");
				div.setAttribute("id", "idIssue");
				var t = document.createTextNode(msgServer.Frage);
				div.appendChild(t);
				question.appendChild(div);
			} else {
				var myNode = document.getElementById("idIssue");
				while (myNode.firstChild) {
				    myNode.removeChild(myNode.firstChild);
				}
				var div = document.getElementById("idIssue");
				var t = document.createTextNode(msgServer.Frage);
				div.appendChild(t);
			}
			
			for(var i = 0; i < 4; i++) {
				if(document.getElementById(i) == null) {
					var question = document.getElementById("questions");
					var div = document.createElement("div");
					if(i == 0 || i == 2) {
						div.setAttribute("class","pull-left col-md-5 answer");
					} else {
						div.setAttribute("class","pull-right col-md-5 answer");
					}
					div.setAttribute("id", i);
					var t = document.createTextNode(msgServer.arrAnswer[i]);
					div.appendChild(t);
					div.addEventListener("mouseover", mouseOverListener);
					div.addEventListener("mouseout", mouseOutListener);
					div.addEventListener("click", mouseClickListener);
					question.appendChild(div);
				} else {
					var myNode = document.getElementById(i);
					while (myNode.firstChild) {
					    myNode.removeChild(myNode.firstChild);
					}
					var div = document.getElementById(i);
					div.addEventListener("mouseover", mouseOverListener);
					div.addEventListener("mouseout", mouseOutListener);
					div.addEventListener("click", mouseClickListener);
					var t = document.createTextNode(msgServer.arrAnswer[i]);
					div.appendChild(t);
				}
			}
		} else { // Keine Fragen mehr vorhanden
			if(document.getElementById("idIssue") != null) {
				var issue = document.getElementById("idIssue");
				issue.parentNode.removeChild(issue);
			}
			for(var i = 0; i < 4; i++) {
				if(document.getElementById(i) != null) {
					var answer = document.getElementById(i);
					answer.parentNode.removeChild(answer);
				}
			}
			
			var append = document.getElementById("questions");
			var div = document.createElement("div");
			div.setAttribute("class", "col-md-8 form-horizontal");
			var para = document.createElement("p");
			para.setAttribute("class", "gameEnd");
			para.setAttribute("id", "gameEnd");
			var t1 = document.createTextNode("Warten auf andere Spieler!");
			para.appendChild(t1);
			div.appendChild(para);
			append.appendChild(div);
			
		}
			
	} else if(msgServer.Type == 11) { // QuestionResult
		// Listener entfernen
		for(var i = 0; i < 4; i++) {
			var div = document.getElementById(i);
			div.removeEventListener("mouseover", mouseOverListener);
			div.removeEventListener("mouseout", mouseOutListener);
			div.removeEventListener("click", mouseClickListener);
		}
		if(msgServer.TimedOut == 1) { // Wenn die Frage timedOut
			for(var i = 0; i < 4; i++) {
				var answers = document.getElementById(i);
				answers.style.backgroundColor = "red";
			}
		} else { // Frage innerhalb des Timeouts beantwortet
			for(var i = 0; i < 4; i++) {
				var answers = document.getElementById(i);
				if(msgServer.Correct == i) {
					answers.style.backgroundColor = "green";
				} else {
					answers.style.backgroundColor = "red";
				}
			}
		}
		
		// 3 Sekunden warten -> die Hintergrundfarbe auf Standard
		setTimeout(function() {
			for(var i = 0; i < 4; i++) {
				var answers = document.getElementById(i);
				answers.style.backgroundColor = "#4F5445";
			}
			// eine neue Frage holen
			var questionRequest = {
					"Type": "8",
					"Length" : 0
				};
			playerSocket.send(JSON.stringify(questionRequest));
		}, 3000);
		
	} else if(msgServer.Type == 12) { // Spielende
		playerSocket.close();
		alert("Spielende\nPlatzierung: " + msgServer.Rank);
		location.reload();
	}
		
	// Spielelogik meldet Fehler
	if(msgServer.Type == 255) {
		if(msgServer.Subtype == 1 || msgServer.Subtype == 0) {
			if(msgServer.Message == "Superuser left") {
				if((clientID > 0) && (document.getElementById("loginF") != null)) {
					// Text "Auf Spielleiter warten!" entfernen
					var loginF = document.getElementById("idLoginOKText");
					loginF.parentNode.removeChild(loginF);
				} else {
					removeLoginForm();
				}
				// Alle bisherigen Spieler löschen
				for(var i = 0; i < 6; i++) {
					var player = document.getElementById("rowPlayer"+ i);
					while(player.firstChild) {
						player.removeChild(player.firstChild);
					}
				}
				playerSocket.close();
				alert("Spiel wird beendet!\n Ursache: " + msgServer.Message);
				location.reload();
			} else {
				var errorElement = document.getElementById("Error");
				var div = document.createElement("div");
				div.setAttribute("id", "errorTag")
				div.setAttribute("class","alert alert-danger");
				if(document.getElementById("errorTag") != null){
					console.log("ErrorTag vorhanden");
					var loginF = document.getElementById("errorTag");
					loginF.parentNode.removeChild(loginF);	
					
				}
				var t = document.createTextNode(msgServer.Message);
				div.appendChild(t);
				errorElement.appendChild(div);
			}
		}
	}
}

// Antwortenlistener
function mouseClickListener(event) {
	var questionAnswered = {
			"Type": "10",
			"Length" : "1",
			"Selection" : event.target.id
		};
		playerSocket.send(JSON.stringify(questionAnswered));
	
}

function mouseOverListener(event) {
	event.target.style.background = "grey";
}

function mouseOutListener(event) {
	event.target.style.background = "#4F5445";
}
	

//Aufruf nach LoginResponseOK
function removeLoginForm() {
	if(document.getElementById("loginForm") != null) {
		var loginF = document.getElementById("loginForm");
		loginF.parentNode.removeChild(loginF);	
	}
}

// Aufruf nach LoginResponseOK
function addGameText() {
	if(clientID == 0) {
		var append = document.getElementById("startButton");
		var div = document.createElement("div");
		div.setAttribute("class", "col-md-8 form-horizontal");
		var para = document.createElement("p");
		para.setAttribute("class", "loginOKText");
		para.setAttribute("id", "idLoginOKText");
		var t1 = document.createTextNode("Sie sind Spielleiter!");
		para.appendChild(t1);
		div.appendChild(para);
		append.appendChild(div);
	} else {
		var append = document.getElementById("startButton");
		var div = document.createElement("div");
		div.setAttribute("class", "col-md-8 form-horizontal");
		var para = document.createElement("p");
		para.setAttribute("class", "loginOKText");
		para.setAttribute("id", "idLoginOKText");
		var t = document.createTextNode("Auf Spielleiter warten!");
		para.appendChild(t);
		div.appendChild(para);
		append.appendChild(div);
	}
	
}
