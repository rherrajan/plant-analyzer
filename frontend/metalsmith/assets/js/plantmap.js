function loadPlantMap(){

	var mapURL = createBackendURL("databaseMap");
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {

		if (this.readyState == 4) {

			if(this.status == 200){
				drawMap(JSON.parse(xhttp.responseText));
			} else {
				alert("could not connect to database. http status: " + this.status);
			}
		};
	}

	xhttp.open("GET", mapURL, true);
	xhttp.send();
};

function drawMap(data) {

	if(data.plants.length < 1){
		alert("no plants to show");
		return;
	}


  //const plant1 = {lat: 51.508742, lng: -0.120850};
  //const plant2 = {lat: 50, lng: 8};

  // position we will use later
  var lat = data.plants[0].lat;
  var lon = data.plants[0].lng;
  // initialize map
  map = L.map('mapDiv').setView([lat, lon], 18);

  // set map tiles source

	var plantIcon = L.icon({
	    iconUrl: '/assets/icons/strawberry-plant.svg',

			iconSize:     [50, 50], // size of the icon
			iconAnchor:   [25, 25], // point of the icon which will correspond to marker's location
			popupAnchor:  [0, 0] // point from which the popup should open relative to the iconAnchor

	});

	
	L.tileLayer( 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
	    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
	    subdomains: ['a','b','c']
	}).addTo( map );
	
  //L.tileLayer('https://b.tile.thunderforest.com/cycle/{z}/{x}/{y}.png?apikey=a5dd6a2f1c934394bce6b0fb077203eb', {
  //L.tileLayer('http://b.tile.stamen.com/watercolor/{z}/{x}/{y}.png', {
  //  attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors'
  // }).addTo(map);

	for (i = 0; i < data.plants.length; i++) {
		marker = L.marker([data.plants[i].lat, data.plants[i].lng], {icon: plantIcon}).addTo(map);
		marker.bindPopup("<img src='" + data.plants[i].planturl + "' width='320' height='240'> " + "<br /> <b>" + data.plants[i].plantname + "</b>");
	}

}