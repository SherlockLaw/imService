package com.sherlock.imService.service;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sherlock.imService.constant.UserEnum.SexEnum;
import com.sherlock.imService.dao.UserMapper;
import com.sherlock.imService.entity.param.UserParam;
import com.sherlock.imService.entity.po.User;
import com.sherlock.imService.entity.vo.UserVO;
import com.sherlock.imService.exception.ServiceException;
import com.sherlock.imService.redis.RedisService;
import com.sherlock.imService.utils.MD5Util;
import com.sherlock.imService.utils.ValidateUtil;

@Service
public class UserService {
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private RedisService redisService;
	
	@Autowired
	private FileService fileService;
	
	private String pwdHandler(String account, String pwd){
		String str = pwd + ":" + account;
		return MD5Util.getMD5(str);
	}
	
	public UserVO register(String account, String pwd, String name, int sex,MultipartFile headImage) {
		if (StringUtils.isBlank(account)) {
			throw new ServiceException("账号不能为空");
		}
		if (StringUtils.isBlank(pwd)) {
			throw new ServiceException("密码不能为空");
		}
		if (StringUtils.isBlank(name)) {
			throw new ServiceException("名称不能为空");
		}
		SexEnum e = SexEnum.getEnum(sex);
		if (e==null) {
			throw new ServiceException("性别不正确");
		}
		if (headImage==null || headImage.isEmpty()) {
			throw new ServiceException("请添加头像");
		}
		ValidateUtil.validateAccount(account);
		ValidateUtil.validatePassword(pwd);
		//1、查询用户是否存在
		User existsUser = userMapper.getByAccount(account);
		if (existsUser != null) {
			 throw new ServiceException("账号已存在");
		}
		User user = new User();
		user.setAccount(account);
		user.setName(name);
		user.setPwd(pwdHandler(account, pwd));
		user.setSex(sex);
		user.setHeadPic(fileService.genHeadPic(headImage));
		//2、持久化数据
		userMapper.insert(user);
		UserVO vo = getVOByPO(user);
		redisService.setToken(vo);
		return vo;
	}

	public UserVO login(String account, String pwd) {
		User existsUser = userMapper.getByAccount(account);
		if (existsUser == null) {
			 throw new ServiceException("账号不存在");
		}
		
		String pwdEnter = pwdHandler(account, pwd);
		if (!pwdEnter.equals(existsUser.getPwd())) {
			throw new ServiceException("密码不正确"); 
		}
		UserVO vo = getVOByPO(existsUser);
		redisService.setToken(vo);
		return vo;
	}
	
	private UserVO getVOByPO(User po){
		UserVO vo = new UserVO();
		BeanUtils.copyProperties(po, vo);
		vo.setPwd(null);
		return vo;
	}
	
	public List<User> search(String keyword) {
		return userMapper.search(keyword);
	}
	public User getUserById(int userId){
		return userMapper.getById(userId);
	}
	
	public List<User> getUserListByIds(List<Integer> userIds){
		return userMapper.getUserList(userIds);
	}
	/**
	 * 更新用户信息
	 * @param account
	 * @param pwd
	 * @param name
	 * @param sex
	 * @param headImage
	 * @return
	 */
	public UserVO update(int id, String name, Integer sex,MultipartFile headImage){
		if (sex!=null) {
			SexEnum e = SexEnum.getEnum(sex);
			if (e==null) {
				throw new ServiceException("性别不正确");
			}
		}
		UserParam param = new UserParam();
		param.setId(id);
		param.setName(name);
		param.setSex(sex);
		//头像
		if (headImage!=null && !headImage.isEmpty()) {
			param.setHeadPic(fileService.genHeadPic(headImage));
		}
		int affect = userMapper.update(param);
		User po = getUserById(param.getId());
		UserVO vo = getVOByPO(po);
		return vo;
	}
	public boolean updatePwd(String account, String oldPwd, String newPwd) {
		if (oldPwd.equals(newPwd)) {
			throw new ServiceException("请输入不同的密码");
		}
		//新密码的校验
		ValidateUtil.validatePassword(newPwd);
		//旧密码的校验
		User user = userMapper.getByAccount(account);
		if (user==null) {
			throw new ServiceException("账号不存在");
		}
		String oldPwdHandler = pwdHandler(user.getAccount(), oldPwd);
		if (!oldPwdHandler.equals(user.getPwd())) {
			throw new ServiceException("密码不正确");
		}
		UserParam param = new UserParam();
		param.setId(user.getId());
		param.setPwd(pwdHandler(user.getAccount(), newPwd));
		int affect = userMapper.updatePwd(param);
		//token失效
		redisService.clearTokenAndUserInfo(user.getId());
		return true;
	}
	
	/**
	 * 用户是否存在
	 * @param userId
	 * @return
	 */
	public boolean existUser(int userId) {
		return userMapper.existUser(userId);
	}
	
	public List<User> getUserList(List<Integer> userIdList) {
		return userMapper.getUserList(userIdList);
	}
}
