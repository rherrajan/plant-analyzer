// Initialize Firebase
var config = {
	apiKey: "AIzaSyCXFqn2fUOOiBMijzFmqt3j2kavxE5uizQ",
	authDomain: "alchemy-dfebf.firebaseapp.com",
	databaseURL: "https://alchemy-dfebf.firebaseio.com",
	projectId: "alchemy-dfebf",
	storageBucket: "alchemy-dfebf.appspot.com",
	messagingSenderId: "725435985112"
};
firebase.initializeApp(config);

// Create a root reference
var storageRef = firebase.storage().ref();

function uploadPlant(images) {
	document.querySelector('.analyseProgress').style="display:block";
	document.querySelector('#progress-files').setAttribute('style', 'width: ' + (100*(0/images.length)) + '%');

	analyseAndUploadNext(images, 0);
}

function analyseAndUploadNext(images, uploadCounter) {
	if(uploadCounter < images.length){
		var file = images[uploadCounter];


		document.querySelector('.analyseResults').style="display:block";

		var mydiv = document.querySelector(".result-greeting");

		var resultContainerTag = document.createElement('div');
		resultContainerTag.setAttribute('class', "row analyseResult");
		mydiv.parentNode.insertBefore(resultContainerTag, mydiv.nextSibling);

		var resultContainerTag2 = document.createElement('div');
		resultContainerTag2.setAttribute('class', "col-md-6");
		resultContainerTag.appendChild(resultContainerTag2);

		var uploadResultTag = document.createElement('div');
		uploadResultTag.setAttribute('class', "thumbnail");
		resultContainerTag2.appendChild(uploadResultTag);

		var spinner = document.createElement('i');
		spinner.setAttribute('class', "fa fa-spinner fa-spin");
		spinner.setAttribute('style', "font-size:24px;color:#207412");
		uploadResultTag.appendChild(spinner);


		var resultContainerTag22 = document.createElement('div');
		resultContainerTag22.setAttribute('class', "col-md-6");
		resultContainerTag.appendChild(resultContainerTag22);

		var analyseResultTag = document.createElement('div');
		analyseResultTag.setAttribute('class', "thumbnail");
		resultContainerTag22.appendChild(analyseResultTag);


		uploadResizedFile(file, function(downloadURL) {
			EXIF.getData(file, function() {
				
				console.log(" --- metadata1: " + JSON.stringify(this));
				console.log(" --- metadata2: ", this);
				
				onUploadFinished(downloadURL, uploadResultTag, analyseResultTag, JSON.stringify(this), function(downloadURL) {
					setTimeout(function() {
						analyseAndUploadNext(images, uploadCounter+1);
					}, 10 * 1000 * Math.min(uploadCounter, 6));

					document.querySelector('#progress-files').setAttribute('style', 'width: ' + (100*((uploadCounter+1)/images.length)) + '%');
				} );
			});
		});
	} else {
		document.querySelector('#progress-files').setAttribute('style', 'width: 100%');
		document.querySelector('#progress-files').setAttribute('class', 'progress-bar bg-info'); // remove striped
	}
}

function uploadResizedFile(file, callback){
	ImageTools.resize(file, {
        width: 800, // maximum width
        height: 800 // maximum height
    }, function(blob, didItResize) {

    	if(!didItResize){
    		console.log("image could not be resized")
    	}

		uploadFile(blob, callback, file.name);
    });
}

function uploadFile(file, callback, filename, metadata){

	// Create the file metadata
	var fileMetadata = {
		contentType: 'image/jpeg'
	};

	let elem = document.querySelector('.uploadStatus');
	elem.innerHTML = "upload starting...";

	var uploadTask = storageRef.child('images/' + filename).put(file, fileMetadata);

	// Listen for state changes, errors, and completion of the upload.
	uploadTask.on(firebase.storage.TaskEvent.STATE_CHANGED, // or 'state_changed'
		function(snapshot) {
		// Get task progress, including the number of bytes uploaded and the total number of bytes to be uploaded
		var progress = (snapshot.bytesTransferred / snapshot.totalBytes) * 100;
		console.log('Upload is ' + progress + '% done');

		document.querySelector('#progress-upload').setAttribute('style', 'width: ' + progress + '%');

		switch (snapshot.state) {
		  case firebase.storage.TaskState.PAUSED: // or 'paused'
		  console.log('Upload is paused');
		  break;
		  case firebase.storage.TaskState.RUNNING: // or 'running'
		  console.log('Upload is running');
		  break;
		}
	}, function(error) {

	  // A full list of error codes is available at
	  // https://firebase.google.com/docs/storage/web/handle-errors
	  switch (error.code) {
	  	case 'storage/unauthorized':
		  // User doesn't have permission to access the object
		  break;

		  case 'storage/canceled':
		  // User canceled the upload
		  break;

		  case 'storage/unknown':
		  // Unknown error occurred, inspect error.serverResponse
		  break;
		}
	}, function() {

		// Upload completed successfully, now we can get the download URL
		callback(uploadTask.snapshot.downloadURL);
	});
}

function onUploadFinished(downloadURL, uploadResultTag, analyseResultTag, metadata, callback){

	var plantURL = escape(downloadURL);

	let elem = document.querySelector('.uploadStatus');
	elem.innerHTML = "analysing...";

	// remove all child inclusive spinner
	while (uploadResultTag.firstChild) {
		uploadResultTag.removeChild(uploadResultTag.firstChild);
	}

	var imgTag = document.createElement('img');
	imgTag.setAttribute('src', downloadURL);
	imgTag.setAttribute('style', 'width:100%');
	uploadResultTag.appendChild(imgTag);

	var spinner = document.createElement('i');
	spinner.setAttribute('class', "fa fa-spinner fa-spin");
	spinner.setAttribute('style', "font-size:24px");
	analyseResultTag.appendChild(spinner);

	var analyzeURL = createBackendURL("/analyze?plantURL=" + plantURL + "&uuid=" + getUUID());
	analysePlant(analyzeURL, spinner, callback, metadata);
}

function analysePlant(analyzeURL, analyseButton, callback, metadata){

	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
		if (this.readyState == 4) {
			if(this.status != 200){
				console.log("got status '" + this.status + "' from this url: " + analyzeURL);
				alert("server error. Status=" + this.status);
			} else {

				var resultContainerTag = analyseButton.parentNode;
				analyseButton.remove();
				var analzyeResult = JSON.parse(this.responseText);

				writeAnalyzeResult(analzyeResult, resultContainerTag);

				let elem = document.querySelector('.uploadStatus');
				elem.innerHTML = "waiting for next upload";

				// start the next analyzing
				callback();
			}
		}
	};

	xhttp.open("POST", analyzeURL, true);
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhttp.send("&metadata=" + escape(metadata));

	return false;
}

function writeAnalyzeResult(analzyeResult, resultContainerTag){

	var row2Tag = resultContainerTag.parentNode.parentNode;


	if(analzyeResult.status != "OK"){

		var imgTag = document.createElement('img');
		imgTag.setAttribute('src', "images/death-with-flower.png");
		imgTag.setAttribute('style', 'width:100%');
		resultContainerTag.appendChild(imgTag);


		var findigs = document.createElement('div');
		findigs.setAttribute('class', 'alert alert-danger');
		findigs.innerHTML = "<strong>" + analzyeResult.error_results[0].message + "</strong>";
		row2Tag.parentNode.insertBefore(findigs, row2Tag.nextSibling);

	} else {
		// alert(" --- name: " + analzyeResult.results[0].binomial);

		var imgTag = document.createElement('img');
		imgTag.setAttribute('src', analzyeResult.results[0].images[0].m_url);
		imgTag.setAttribute('style', 'width:100%');
		resultContainerTag.appendChild(imgTag);

		var name;
		if(analzyeResult.results[0].cn){
			name = analzyeResult.results[0].cn;
		} else {
			name = analzyeResult.results[0].binomial;
		}
		var judgingClass;
		if(analzyeResult.results[0].score > 20){
			judgingClass = 'alert-success';
		} else {
			judgingClass = 'alert-warning';
		}
		//var wikiHref = "https://de.wikipedia.org/w/index.php?search=" + analzyeResult.results[0].binomial;
		var wikiHref = "https://pfaf.org/user/Plant.aspx?LatinName=" + analzyeResult.results[0].binomial;


		var findigs = document.createElement('div');
		findigs.setAttribute('class', 'alert ' + judgingClass);
		findigs.innerHTML = '<strong>'+name+'</strong> <i>(<a href="'+wikiHref+'">'+analzyeResult.results[0].binomial+'</a>)</i> <br/> Propability: <strong>'+analzyeResult.results[0].score+'%</strong>';
		row2Tag.parentNode.insertBefore(findigs, row2Tag.nextSibling);
	}
}

