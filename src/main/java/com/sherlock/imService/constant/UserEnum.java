package com.sherlock.imService.constant;

public class UserEnum {

	public enum SexEnum{
		
		man(1),
		woman(2);
		private int sex;
		
		SexEnum(int sex){
			this.sex = sex;
		}
		
		public static SexEnum getEnum(int sex) {
			SexEnum[] l = SexEnum.values();
			for (SexEnum e: l){
				if (e.sex==sex) {
					return e;
				}
			}
			return null;
		}		
	}
}
