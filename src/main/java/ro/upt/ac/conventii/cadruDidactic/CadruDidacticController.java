package ro.upt.ac.conventii.cadruDidactic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CadruDidacticController
{
	@Autowired
	CadruDidacticRepository cadruDidacticRepository;
		
	@GetMapping("/cadruDidactic-create")
	public String create(CadruDidactic cadruDidactic)
	{
		return "cadruDidactic-create";
	}

	@PostMapping("/cadruDidactic-create-save")
	public String createSave(@Validated CadruDidactic cadruDidactic, BindingResult result, Model model)
	{
		if(result.hasErrors())
		{
			return "cadruDidactic-create";
		}
		cadruDidacticRepository.save(cadruDidactic);
		return "redirect:/cadruDidactic-read";
	}
	
	@GetMapping("/cadruDidactic-read")
	public String read(Model model) 
	{
	    model.addAttribute("cadreDidactice", cadruDidacticRepository.findAll());
	    return "cadruDidactic-read";
	}
	
	@GetMapping("/cadruDidactic-edit/{id}")
	public String edit(@PathVariable("id") int id, Model model) 
	{
	    CadruDidactic cadruDidactic = cadruDidacticRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid cadruDidactic Id:" + id));
	    
	    model.addAttribute("cadruDidactic", cadruDidactic);
	    return "cadruDidactic-update";
	}
	
	@PostMapping("/cadruDidactic-update/{id}")
	public String update(@PathVariable("id") int id, @Validated CadruDidactic cadruDidactic, BindingResult result, Model model) 
	{
	    if(result.hasErrors()) 
	    {
	        cadruDidactic.setId(id);
	        return "cadruDidactic-update";
	    }
	        
	    cadruDidacticRepository.save(cadruDidactic);
	    return "redirect:/cadruDidactic-read";
	}
	
	@GetMapping("/cadruDidactic-delete/{id}")
	public String delete(@PathVariable("id") int id, Model model) 
	{
	    CadruDidactic cadruDidactic = cadruDidacticRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid cadruDidactic Id:" + id));
	    
	    cadruDidacticRepository.delete(cadruDidactic);
	    return "redirect:/cadruDidactic-read";
	}	
}
