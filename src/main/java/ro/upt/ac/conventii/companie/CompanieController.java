package ro.upt.ac.conventii.companie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CompanieController
{
	@Autowired
	CompanieRepository companieRepository;

	@GetMapping("/companie-create")
	public String create(Companie companie)
	{
		return "companie-create";
	}

	@PostMapping("/companie-create-save")
	public String createSave(@Validated Companie companie, BindingResult result, Model model)
	{
		if(result.hasErrors())
		{
			return "companie-create";
		}
		companieRepository.save(companie);
		return "redirect:/companie-read";
	}
	
	@GetMapping("/companie-read")
	public String read(Model model) 
	{
	    model.addAttribute("companii", companieRepository.findAll());
	    return "companie-read";
	}
	
	@GetMapping("/companie-edit/{id}")
	public String edit(@PathVariable("id") int id, Model model) 
	{
	    Companie companie = companieRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid companie Id:" + id));
	    
	    model.addAttribute("companie", companie);
	    return "companie-update";
	}
	
	@PostMapping("/companie-update/{id}")
	public String update(@PathVariable("id") int id, @Validated Companie companie, BindingResult result, Model model) 
	{
	    if(result.hasErrors()) 
	    {
	        companie.setId(id);
	        return "companie-update";
	    }
	        
	    companieRepository.save(companie);
	    return "redirect:/companie-read";
	}
	
	@GetMapping("/companie-delete/{id}")
	public String delete(@PathVariable("id") int id, Model model) 
	{
	    Companie companie = companieRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid companie Id:" + id));
	    
	    companieRepository.delete(companie);
	    return "redirect:/companie-read";
	}	
}
