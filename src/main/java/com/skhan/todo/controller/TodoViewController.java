package com.skhan.todo.controller;

import com.skhan.todo.service.TodoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TodoViewController {

    private final TodoService todoService;

    public TodoViewController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping({"/", "/todos"})
    public String index(Model model) {
        long total = todoService.getTotalCount();
        long completed = todoService.getCompletedCount();
        long pending = todoService.getPendingCount();
        int progressPercent = total > 0 ? (int) Math.round(((double) completed / total) * 100) : 0;

        model.addAttribute("todos", todoService.getAllTodos(null, null));
        model.addAttribute("totalCount", total);
        model.addAttribute("completedCount", completed);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("progressPercent", progressPercent);

        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        long total = todoService.getTotalCount();
        long completed = todoService.getCompletedCount();
        long pending = todoService.getPendingCount();
        int progressPercent = total > 0 ? (int) Math.round(((double) completed / total) * 100) : 0;

        model.addAttribute("totalCount", total);
        model.addAttribute("completedCount", completed);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("progressPercent", progressPercent);
        model.addAttribute("version", "1.0.0");
        model.addAttribute("javaVersion", System.getProperty("java.version"));
        model.addAttribute("springBootVersion", "4.1.0");

        return "about";
    }
}
