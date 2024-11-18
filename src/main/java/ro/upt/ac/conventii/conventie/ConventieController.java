package ro.upt.ac.conventii.conventie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import ro.upt.ac.conventii.cadruDidactic.CadruDidacticRepository;
import ro.upt.ac.conventii.companie.CompanieRepository;

@Controller
public class ConventieController
{
	@Autowired
	ConventieRepository conventieRepository;
	@Autowired
	CompanieRepository companieRepository;
	@Autowired
	CadruDidacticRepository cadruDidacticRepository;

	@GetMapping("/")
	public String root()
	{
		return "index";
	}
	
	@GetMapping("/conventie-create")
	public String create(Conventie conventie, Model model)
	{
		model.addAttribute("conventie", new Conventie());
		return "conventie-create";
	}

	@PostMapping("/conventie-create-save")
	public String createSave(@Validated Conventie conventie, BindingResult result, Model model)
	{
		if(result.hasErrors())
		{
			return "conventie-create";
		}
		
		conventieRepository.save(conventie);
		return "redirect:/conventie-read";
	}
	
	@GetMapping("/conventie-read")
	public String read(Model model) 
	{
	    model.addAttribute("conventii", conventieRepository.findAll());
	    return "conventie-read";
	}
	
	@GetMapping("/conventie-edit/{id}")
	public String edit(@PathVariable("id") int id, Model model) 
	{
	    Conventie conventie = conventieRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid conventie Id:" + id));
	    	    
	    model.addAttribute("conventie", conventie);
	    model.addAttribute("companii", companieRepository.findAll());
	    model.addAttribute("cadreDidactice", cadruDidacticRepository.findAll());
	    return "conventie-update";
	}
	
	@PostMapping("/conventie-update/{id}")
	public String update(@PathVariable("id") int id, @Validated Conventie conventie, BindingResult result, Model model) 
	{
	    if(result.hasErrors()) 
	    {
	        conventie.setId(id);
	        return "conventie-update";
	    }
	        
	    conventieRepository.save(conventie);
	    return "redirect:/conventie-read";
	}
	
	@GetMapping("/conventie-delete/{id}")
	public String delete(@PathVariable("id") int id, Model model) 
	{
	    Conventie conventie = conventieRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid conventie Id:" + id));
	    
	    conventieRepository.delete(conventie);
	    return "redirect:/conventie-read";
	}	
}
