package com.sherlock.imService.service;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sherlock.imService.configure.FtpConfigure;
import com.sherlock.imService.entity.vo.UploadVO;
import com.sherlock.imService.exception.ServiceException;
import com.sherlock.imService.utils.FtpUtil;
import com.sherlock.imService.utils.UploadUtil;

@Service
public class FileService {
	private final Logger logger = LoggerFactory.getLogger(FileService.class);

	@Autowired
	private FtpConfigure ftpConfigure;

	public UploadVO uploadImage(MultipartFile image) {
		BufferedImage bufferImg;
		try {
			bufferImg = ImageIO.read(image.getInputStream());
			if (bufferImg == null) {
				throw new ServiceException("上传文件不是图片");
			}			
		} catch (IOException e) {
			throw new ServiceException("读取失败");
		}
		int width = bufferImg.getWidth();
		int heigth = bufferImg.getHeight();
		String url = uploadImage0(null, image);
		UploadVO vo = new UploadVO();
		vo.setUrl(url);
		vo.setWidth(width);
		vo.setHeigth(heigth);	
		return vo;
	}
	public String uploadImage0(String parentPath, MultipartFile image) {
		String oldName = image.getOriginalFilename();
		String picNewName = UploadUtil.generateRandonFileName(oldName);// 通过工具类产生新图片名称，防止重名

		String picSavePath = UploadUtil.generateRandomDir(picNewName);// 通过工具类把图片目录分级
		if (!StringUtils.isBlank(parentPath)) {
			picSavePath = new StringBuilder().append("/")
					.append(parentPath).append(picSavePath).toString();
		}
		String path = null;
		try {
			path = FtpUtil.pictureUploadByConfig(ftpConfigure, picNewName, picSavePath, image.getInputStream());
		} catch (IOException e) {
			String error = "图片上传出错";
			logger.error(error, e);
			throw new ServiceException(error);
		}
		return path;
	} 
	
	public String genHeadPic(MultipartFile headImage){
		String str = uploadImage0("headPic", headImage);
		int dotIdx = str.lastIndexOf('.');
		if (dotIdx==-1) {
			throw new ServiceException("图片上传有误");
		}
		String headPic = str.substring(0, dotIdx) + "_100x100" + str.substring(dotIdx, str.length());
		return headPic;
	}
}
