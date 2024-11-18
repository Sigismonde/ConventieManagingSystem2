package ro.upt.ac.conventii.student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class StudentController
{
	@Autowired
	StudentRepository studentRepository;
		
	@GetMapping("/student-create")
	public String create(Student student)
	{
		return "student-create";
	}

	@PostMapping("/student-create-save")
	public String createSave(@Validated Student student, BindingResult result, Model model)
	{
		if(result.hasErrors())
		{
			return "student-create";
		}
		studentRepository.save(student);
		return "redirect:/student-read";
	}
	
	@GetMapping("/student-read")
	public String read(Model model) 
	{
	    model.addAttribute("studenti", studentRepository.findAll());
	    return "student-read";
	}
	
	@GetMapping("/student-edit/{id}")
	public String edit(@PathVariable("id") int id, Model model) 
	{
	    Student student = studentRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
	    
	    model.addAttribute("student", student);
	    return "student-update";
	}
	
	@PostMapping("/student-update/{id}")
	public String update(@PathVariable("id") int id, @Validated Student student, BindingResult result, Model model) 
	{
	    if(result.hasErrors()) 
	    {
	        student.setId(id);
	        return "student-update";
	    }
	        
	    studentRepository.save(student);
	    return "redirect:/student-read";
	}
	
	@GetMapping("/student-delete/{id}")
	public String delete(@PathVariable("id") int id, Model model) 
	{
	    Student student = studentRepository.findById(id);
	    //.orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
	    
	    studentRepository.delete(student);
	    return "redirect:/student-read";
	}	
}
