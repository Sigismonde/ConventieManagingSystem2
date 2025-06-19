package ro.upt.ac.conventii.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController
{
	@Autowired
	UserRepository userRepository;

	@GetMapping("/user-create")
	public String create(User user)
	{
		return "user-create";
	}

	@PostMapping("/user-create-save")
	public String createSave(@Validated User user, BindingResult result, Model model)
	{
		if(result.hasErrors())
		{
			return "user-create";
		}
		userRepository.save(user);
		return "redirect:/user-read";
	}
	
	@GetMapping("/user-read")
	public String read(Model model) 
	{
	    model.addAttribute("users", userRepository.findAll());
	    return "user-read";
	}
	
	@GetMapping("/user-edit/{id}")
	public String edit(@PathVariable("id") int id, Model model) 
	{
	    User user = userRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
	    
	    model.addAttribute("user", user);
	    return "user-update";
	}
	
	@PostMapping("/user-update/{id}")
	public String update(@PathVariable("id") int id, @Validated User user, BindingResult result, Model model) 
	{
	    if(result.hasErrors()) 
	    {
	        user.setId(id);
	        return "user-update";
	    }
	        
	    userRepository.save(user);
	    return "redirect:/user-read";
	}
	
	@GetMapping("/user-delete/{id}")
	public String delete(@PathVariable("id") int id, Model model) 
	{
	    User user = userRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
	    
	    userRepository.delete(user);
	    return "redirect:/user-read";
	}	
}
