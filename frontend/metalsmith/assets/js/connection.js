function createBackendURL(path){

	return "http://plant-detection.herokuapp.com/" + path;
	
//	if(location.hostname==="localhost"){
//		return "http://localhost:5000/" + path;
//	} else {
//		return location.protocol
//		    + '//'+subdomain(location.hostname)
//		    + '.herokuapp.com/'
//		    + path;
//	}

}

function subdomain(host) {
    var part = host.split('.').reverse(),
        index = 0;

    while (part[index].length === 2 || !index) {
        ++index;
    }
    ++index;

    return part.length > index && part[index] !== 'www' ? part[index] : '';
}
