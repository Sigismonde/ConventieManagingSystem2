package ro.upt.ac.conventii.tutore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TutoreController
{
	@Autowired
	TutoreRepository tutoreRepository;

	@GetMapping("/tutore-create")
	public String create(Tutore tutore)
	{
		return "tutore-create";
	}

	@PostMapping("/tutore-create-save")
	public String createSave(@Validated Tutore tutore, BindingResult result, Model model)
	{
		if(result.hasErrors())
		{
			return "tutore-create";
		}
		tutoreRepository.save(tutore);
		return "redirect:/tutore-read";
	}
	
	@GetMapping("/tutore-read")
	public String read(Model model) 
	{
	    model.addAttribute("tutores", tutoreRepository.findAll());
	    return "tutore-read";
	}
	
	@GetMapping("/tutore-edit/{id}")
	public String edit(@PathVariable("id") int id, Model model) 
	{
	    Tutore tutore = tutoreRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid tutore Id:" + id));
	    
	    model.addAttribute("tutore", tutore);
	    return "tutore-update";
	}
	
	@PostMapping("/tutore-update/{id}")
	public String update(@PathVariable("id") int id, @Validated Tutore tutore, BindingResult result, Model model) 
	{
	    if(result.hasErrors()) 
	    {
	        tutore.setId(id);
	        return "tutore-update";
	    }
	        
	    tutoreRepository.save(tutore);
	    return "redirect:/tutore-read";
	}
	
	@GetMapping("/tutore-delete/{id}")
	public String delete(@PathVariable("id") int id, Model model) 
	{
	    Tutore tutore = tutoreRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid tutore Id:" + id));
	    
	    tutoreRepository.delete(tutore);
	    return "redirect:/tutore-read";
	}	
}
