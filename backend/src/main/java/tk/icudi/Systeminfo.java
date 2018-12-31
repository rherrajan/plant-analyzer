package tk.icudi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class Systeminfo {

	@Value("classpath:properties/systeminfo.json")
	private Resource systeminfo;
	
	@RequestMapping(value="/systeminfo", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	String getSysteminfo() throws IOException {
		return IOUtils.toString(systeminfo.getInputStream(), StandardCharsets.UTF_8); 
	}

	@ModelAttribute
	public void setVaryResponseHeader(HttpServletResponse response) {
	    response.setHeader("Access-Control-Allow-Origin", "*");	    
	}   
	
}
