package tk.icudi;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Home {
	
	@RequestMapping("/")
	protected String redirect() {
		return "redirect:systeminfo";
	}

}
