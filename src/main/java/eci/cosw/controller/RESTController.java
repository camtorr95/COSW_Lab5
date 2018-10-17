package eci.cosw.controller;

import com.mongodb.client.gridfs.model.GridFSFile;
import eci.cosw.data.TodoRepository;
import eci.cosw.data.model.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@RequestMapping("api")
@RestController
public class RESTController {

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    TodoRepository todoRepository;

    @CrossOrigin("*")
    @RequestMapping("/files/{filename}")
    public ResponseEntity<InputStreamResource> getFileByName(@PathVariable String filename) {
        GridFSFile file = gridFsTemplate.findOne(new Query().addCriteria(Criteria.where("filename").is(filename)));
        if (file != null) {
            try {
                GridFsResource resource = gridFsTemplate.getResource(file.getFilename());
                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf(resource.getContentType()))
                        .body(new InputStreamResource(resource.getInputStream()));
            } catch (IOException e) {
                return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @CrossOrigin("*")
    @PostMapping("/files")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
            return String.format("/%s", file.getOriginalFilename());
        } catch (IOException e){
            return HttpStatus.INTERNAL_SERVER_ERROR.toString();
        }
    }

    @CrossOrigin("*")
    @PostMapping("/todo")
    public Todo createTodo(@RequestBody Todo todo) {
        todoRepository.save(todo);
        System.out.println(todo);
        return todo;
    }

    @CrossOrigin("*")
    @GetMapping("/todo")
    public List<Todo> getTodoList() {
        return todoRepository.findAll();
    }
}
