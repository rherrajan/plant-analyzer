function loadPlants(){

	var statusURL = createBackendURL("database");
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {

		if (this.readyState == 4) {

			document.querySelector('#plantsspinner').remove();

			var plantResults = document.querySelector(".result-greeting");

			if(this.status == 200){

				data = JSON.parse(xhttp.responseText);

				for (i = 0; i < data.plants.length; i++) {
				    appendLoadedResult(plantResults, data.plants[i]);
				}

			} else {
				console.log("got status '" + this.status + "' from this url: " + statusURL);
				plantResults.innerHTML = "could not connect to database. http status: " + this.status;
			}
		};
	}

	xhttp.open("GET", statusURL, true);
	xhttp.send();
};

function appendLoadedResult(plantResults, plant){

	var resultContainerTag = document.createElement('div');
	resultContainerTag.setAttribute('class', "row analyseResult");
	plantResults.parentNode.appendChild(resultContainerTag);

	var resultContainerTag2 = document.createElement('div');
	resultContainerTag2.setAttribute('class', "col-md-6");
	resultContainerTag.appendChild(resultContainerTag2);

	var uploadResultTag = document.createElement('div');
	uploadResultTag.setAttribute('class', "thumbnail");
	resultContainerTag2.appendChild(uploadResultTag);

	var imgTag = document.createElement('img');
	imgTag.setAttribute('src', plant.planturl);
	imgTag.setAttribute('style', 'width:100%');
	uploadResultTag.appendChild(imgTag);

	var resultContainerTag22 = document.createElement('div');
	resultContainerTag22.setAttribute('class', "col-md-6");
	resultContainerTag.appendChild(resultContainerTag22);

	var analyseResultTag = document.createElement('div');
	analyseResultTag.setAttribute('class', "thumbnail");
	resultContainerTag22.appendChild(analyseResultTag);

	var imgTag = document.createElement('img');
	imgTag.setAttribute('src', plant.matchURL);
	imgTag.setAttribute('style', 'width:100%');
	analyseResultTag.appendChild(imgTag);

	//var wikiHref = "https://de.wikipedia.org/w/index.php?search=" + plant.plantname;
	var wikiHref = "https://pfaf.org/user/Plant.aspx?LatinName=" + plant.plantname;

	var name = plant.folkname ? plant.folkname : plant.plantname;
	var decoration = plant.uuid == getUUID() ? '<i class="fa fa-star" aria-hidden="true" style="font-size:24px;color:yellow"></i>' : '';

	var conclusion = document.createElement('div');
	conclusion.setAttribute('class', 'alert alert-success');
	conclusion.innerHTML = '<strong>'+name+'</strong> <i>(<a href="'+wikiHref+'">'+plant.plantname+'</a>)</i>' + decoration + ' <br/> Propability: <strong>'+plant.score+'%</strong>';

	plantResults.parentNode.appendChild(conclusion);

}