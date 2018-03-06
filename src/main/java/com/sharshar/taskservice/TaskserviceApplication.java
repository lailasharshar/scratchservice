package com.sharshar.taskservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableScheduling
public class TaskserviceApplication {

	@Autowired
	TaskDAO taskDAO;

	public static void main(String[] args) {
		SpringApplication.run(TaskserviceApplication.class, args);
	}
}
