function checkStatus(){

	var statusURL = createBackendURL("systeminfo");
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
		if (this.readyState == 4) {
			var statusElement = document.querySelector('#led-status');
			if(this.status == 200){
				statusElement.classList.add("led-green");
				statusElement.title = xhttp.responseText;
			} else {
				statusElement.classList.add("led-red");
				statusElement.title = "could not conect to backend via url: " + statusURL;
			}
			statusElement.classList.remove("led-blue");
		};
	}

	xhttp.open("GET", statusURL, true);
	xhttp.send();
}