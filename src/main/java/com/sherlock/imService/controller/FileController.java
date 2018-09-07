package com.sherlock.imService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sherlock.imService.common.Result;
import com.sherlock.imService.service.FileService;

@RestController
@RequestMapping("/file")
public class FileController {

	@Autowired
	private FileService fileService;
	
	@RequestMapping(value="/uploadImage",method=RequestMethod.POST)
	public Result uploadImage(MultipartFile image){
		return Result.success(fileService.uploadImage(image));
	}
	
}
