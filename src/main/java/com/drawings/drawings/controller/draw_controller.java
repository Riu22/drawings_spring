package com.drawings.drawings.controller;

import com.drawings.drawings.dto.draw_dto;
import com.drawings.drawings.model.draw;
import com.drawings.drawings.service.save_service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.NoSuchElementException;

@Controller
public class draw_controller {
    @GetMapping("/home")
    public String drawing(HttpSession session, Model model){
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);
        return "drawing";
    }

    @PostMapping("/save")
    public String saveDrawing(HttpSession session, @RequestBody draw_dto drawDto) {

        String author = (String) session.getAttribute("username");
        //hacer el dao para pillar el id por el author
        try {
            // 1 Obtener los datos del DTO
            draw saved_draw = save_service.save_draw(
                    drawDto.getTitle(),
                    drawDto.isIspublic(),
                    author,
                    drawDto.getDrawContent() // Asumo que save_draw acepta el drawContent
            );

            return "redirect:/gallery";

        } catch (NoSuchElementException e) {
            System.err.println("Error al guardar el dibujo: " + e.getMessage());
            return "redirect:/error?message=Datos Inválidos";
        } catch (Exception e) {
            System.err.println("Error interno del servidor: " + e.getMessage());
            return "redirect:/error?message=Fallo al guardar el dibujo";
        }
    }

}
